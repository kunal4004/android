package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.PagingResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductRequest;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.SearchResultShopAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType;
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity.CLOSE_ICON_TAPPED_RESULT_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity.SELECTED_SHOPPING_LIST_ITEM_RESULT_CODE;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.INDEX_ADD_TO_SHOPPING_LIST;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment.INDEX_SEARCH_FROM_LIST;

public class SearchResultFragment extends Fragment implements SearchResultNavigator, View.OnClickListener, NetworkChangeListener {

    private static final int COLOR_SIZE_SELECTION_REQUEST_CODE = 3012;
    private ErrorHandlerView mErrorHandlerView;
    private SearchResultShopAdapter mProductAdapter;
    private List<ProductList> mProductList;
    private ProgressBar mProgressLimitStart;
    private LinearLayoutManager mRecyclerViewLayoutManager;
    private String mSearchText;
    private int totalItemCount;
    private int lastVisibleItem;
    private boolean isLoading;
    private String mListId;
    private Call<ProductDetailResponse> mGetProductDetail;
    private ProductList mSelectedProduct;
    private int mAddToListSize = 0;
    private Call<ShoppingListItemsResponse> mPostAddToList;
    private BroadcastReceiver connectionBroadcast;
    private boolean addToListLoadFail = false;
    private int mNumItemsInTotal;
    private boolean loadMoreData = false;
    private int pageOffset = 0;
    private boolean mIsLoading = false;
    private ArrayList<OtherSkus> otherSkus;
    private boolean productIsLoading;
    private ProductsRequestParams productsRequestParams;
    private Call<ProductView> mGetProductsRequest;
    private WButton btnCheckOut;
    private RelativeLayout relNoConnectionLayout;
    private WButton btnRetry;
    private RelativeLayout rlAddToList;
    private ProgressBar pbLoadingIndicator;
    private RecyclerView rclProductList;
    public static final int ADDED_TO_SHOPPING_LIST_RESULT_CODE = 1312;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mSearchText = bundle.getString("searchTerm");
            mListId = bundle.getString("listID");
        }
        setProductBody();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_result_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setTranslationZ(getView(), 100.f);
        initUI(view);
        mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
        mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
        startProductRequest();
        btnRetry.setOnClickListener(this);
        setUpAddToListButton();
        connectionBroadcast();
    }

    private void initUI(View view) {
        mProgressLimitStart = view.findViewById(R.id.pbLoadProduct);
        btnCheckOut = view.findViewById(R.id.btnCheckOut);
        relNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
        btnRetry = view.findViewById(R.id.btnRetry);
        rlAddToList = view.findViewById(R.id.rlCheckOut);
        pbLoadingIndicator = view.findViewById(R.id.pbLoadingIndicator);
        rclProductList = view.findViewById(R.id.productList);
    }

    private WGlobalState getGlobalState() {
        WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
        if (woolworthsApplication != null)
            return woolworthsApplication.getWGlobalState();
        else
            return null;
    }

    private void setUpAddToListButton() {
        btnCheckOut.setOnClickListener(this);
        btnCheckOut.setText(getString(R.string.add_to_list));
        toggleAddToListBtn(false);
    }

    @Override
    public void onLoadProductSuccess(List<ProductList> productLists, boolean loadMoreData) {
        if (productLists != null) {
            if (productLists.size() == 1) {
                ScreenManager.presentProductDetails(getActivity(), mSearchText, productLists.get(0));
            } else {
                if (!loadMoreData) {
                    bindRecyclerViewWithUI(productLists);
                } else {
                    loadMoreData(productLists);
                }
            }
        }
    }

    @Override
    public void unhandledResponseCode(Response response) {
    }

    @Override
    public void failureResponseHandler(final String e) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mErrorHandlerView.networkFailureHandler(e);
                }
            });
        }
    }

    private void cancelColorSizeSelection() {
        try {
            OtherSkus otherSkus = new OtherSkus();
            otherSkus.sku = getSelectedProduct().sku;
            if (getProductAdapter() != null)
                getProductAdapter().onDeselectSKU(getSelectedProduct(), otherSkus);
        } catch (NullPointerException ex) {
            Log.d("cancelColorSize", ex.getMessage());
        }
    }

    private SearchResultShopAdapter getProductAdapter() {
        return mProductAdapter;
    }

    @Override
    public void cancelAPIRequest() {
        cancelRequest(mGetProductsRequest);
    }

    @Override
    public void bindRecyclerViewWithUI(List<ProductList> productList) {
        this.mProductList = productList;

        if (!listContainHeader()) {
            ProductList headerProduct = new ProductList();
            headerProduct.rowType = ProductListingViewType.HEADER;
            headerProduct.numberOfItems = getNumItemsInTotal();
            productList.add(0, headerProduct);
        }

        mProductAdapter = new SearchResultShopAdapter(mProductList, this);
        mRecyclerViewLayoutManager = new LinearLayoutManager(getActivity());
        rclProductList.setLayoutManager(mRecyclerViewLayoutManager);
        rclProductList.setNestedScrollingEnabled(false);
        rclProductList.setAdapter(getProductAdapter());
        rclProductList.setItemAnimator(null);
        rclProductList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = mRecyclerViewLayoutManager.getItemCount();
                lastVisibleItem = mRecyclerViewLayoutManager.findLastVisibleItemPosition();
                loadData();
            }
        });
    }

    private void loadData() {
        int visibleThreshold = 5;
        if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
            if (productIsLoading())
                return;
            int Total = getNumItemsInTotal() + Utils.PAGE_SIZE;
            int start = mProductList.size();
            int end = start + Utils.PAGE_SIZE;
            isLoading = (Total < end);
            if (isLoading) {
                return;
            }
            if (!listContainFooter()) {
                ProductList footerItem = new ProductList();
                footerItem.rowType = ProductListingViewType.FOOTER;
                mProductList.add(footerItem);
                getProductAdapter().notifyItemInserted(mProductList.size() - 1);
            }
            startProductRequest();
        }
    }

    private boolean listContainFooter() {
        if (mProductList == null) return false;
        for (ProductList pl : mProductList) {
            if (pl.rowType == ProductListingViewType.FOOTER) {
                return true;
            }
        }
        return false;
    }

    private void removeFooter() {
        int index = 0;
        for (ProductList pl : mProductList) {
            if (pl.rowType == ProductListingViewType.FOOTER) {
                mProductList.remove(pl);
                getProductAdapter().notifyItemRemoved(index);
                return;
            }
            index++;
        }
    }

    private boolean listContainHeader() {
        for (ProductList pl : mProductList) {
            if (pl.rowType == ProductListingViewType.HEADER) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelAPIRequest();
    }

    @Override
    public void startProductRequest() {
        executeSearchProduct(getActivity(), getProductRequestBody());
    }

    @Override
    public void loadMoreData(List<ProductList> productLists) {
        int actualSize = mProductList.size() + 1;
        mProductList.addAll(productLists);
        int sizeOfList = mProductList.size();
        getProductAdapter().notifyItemChanged(actualSize, sizeOfList);
        canLoadMore(getNumItemsInTotal(), sizeOfList);
    }

    @Override
    public void setProductBody() {
        setProductRequestBody(ProductsRequestParams.SearchType.SEARCH, mSearchText);
    }

    @Override
    public void onLoadStart(boolean isLoadMore) {
        setIsLoading(true);
        if (!isLoadMore) {
            mProgressLimitStart.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadComplete(boolean isLoadMore) {
        if (listContainFooter()) {
            removeFooter();
        }
        setIsLoading(false);
        if (!isLoadMore) {
            mProgressLimitStart.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        MultiClickPreventer.preventMultiClick(view);
        switch (view.getId()) {
            case R.id.btnRetry:
                if (isNetworkConnected()) {
                    mErrorHandlerView.hideErrorHandler();
                    startProductRequest();
                }
                break;

            case R.id.btnCheckOut:
                cancelRequest(mGetProductDetail);
                if (mProductAdapter == null) return;
                for (ProductList productList : mProductAdapter.getProductList()) {
                    if (productList.viewIsLoading) {
                        productList.viewIsLoading = false;
                        productList.itemWasChecked = false;
                    }
                    mProductAdapter.notifyDataSetChanged();
                }
                List<AddToListRequest> addToListRequests = new ArrayList<>();
                for (ProductList list : mProductAdapter.getProductList()) {
                    if (list.itemWasChecked) {
                        AddToListRequest addToList = new AddToListRequest();
                        addToList.setCatalogRefId(list.sku);
                        addToList.setQuantity("1");
                        addToList.setGiftListId(list.sku);
                        addToList.setSkuID(list.sku);
                        addToListRequests.add(addToList);
                    }
                }
                mAddToListSize = addToListRequests.size();
                postAddToList(addToListRequests);
                break;

            default:
                break;
        }
    }

    private void postAddToList(List<AddToListRequest> addToListRequests) {
        mPostAddToList = addToList(addToListRequests, mListId);
    }


    @Override
    public void onFoodTypeSelect(ProductList productList) {
        ScreenManager.presentProductDetails(getActivity(), mSearchText, productList);
    }

    @Override
    public void onClothingTypeSelect(ProductList productList) {
        ScreenManager.presentProductDetails(getActivity(), mSearchText, productList);
    }

    @Override
    public void minOneItemSelected(List<ProductList> prodList) {
        this.mProductList = prodList;
        boolean productWasChecked = false;
        for (ProductList productList : prodList) {
            if (productList.itemWasChecked) {
                productWasChecked = true;
                toggleAddToListBtn(true);
            }
        }
        // hide checkbox when no item selected
        if (!productWasChecked) {
            toggleAddToListBtn(false);
        }
    }

    @Override
    public void onAddToListFailure(String e) {
        Log.e("onAddToListFailure", e);
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onAddToListLoad(false);
                    addToListLoadFail = true;
                }
            });
        }
    }

    @Override
    public void onAddToListLoad(boolean isLoading) {
        addToListLoadFail = false;
        pbLoadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnCheckOut.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    public void onAddToListLoadComplete() {
        addToListLoadFail = false;
        pbLoadingIndicator.setVisibility(View.GONE);
        btnCheckOut.setVisibility(View.VISIBLE);
        Intent addedToListIntent = new Intent();
        addedToListIntent.putExtra("listItems", mAddToListSize);
        Activity activity = getActivity();
        if (activity != null) {
            activity.setResult(ADDED_TO_SHOPPING_LIST_RESULT_CODE, addedToListIntent);
            activity.onBackPressed();
        }
    }

    @Override
    public void onCheckedItem(List<ProductList> productLists, ProductList selectedProduct, boolean viewIsLoading) {
        setSelectedProduct(selectedProduct);
        mProductList = productLists;
        if (viewIsLoading) {
            ProductRequest productRequest = new ProductRequest(selectedProduct.productId, selectedProduct.sku);
            productDetailRequest(productRequest);
        } else {
            if (getProductAdapter() != null) {
                OtherSkus otherSkus = new OtherSkus();
                otherSkus.sku = selectedProduct.sku;
                getProductAdapter().onDeselectSKU(getSelectedProduct(), otherSkus);
            }
        }
    }

    @Override
    public void responseFailureHandler(Response response) {
    }

    @Override
    public void onSuccessResponse(WProduct product) {
        Objects.requireNonNull(getGlobalState()).saveButtonClicked(INDEX_SEARCH_FROM_LIST);
        getProductAdapter().setCheckedProgressBar(getSelectedProduct());
        if (isNetworkConnected()) {
            ArrayList<OtherSkus> otherSkuList = getOtherSkus();
            ArrayList<OtherSkus> colorList = getColorList();
            ArrayList<OtherSkus> sizeList = getSizeList();

            WProductDetail objProduct = product.product;

            int colorSize = colorList.size();
            boolean productContainColor = colorSize > 1;
            boolean onlyOneColor = colorSize == 1;

            int sizeSize = sizeList.size();
            boolean productContainSize = sizeSize > 1;
            boolean onlyOneSize = sizeSize == 1;

            if (productContainColor) { // contains one or more color
                //show picker dialog
                twoOrMoreColorIntent(otherSkuList, colorList, objProduct);
            } else {
                if (onlyOneColor) { // contains one color only
                    String color = colorList.get(0) != null ? colorList.get(0).colour : "";
                    // contains more than one size
                    intentSizeList(color, colorList.get(0), otherSkuList, objProduct);  // open size intent with color as filter
                } else {  // no color found
                    if (productContainSize) {
                        if (onlyOneSize) {
                            noSizeColorIntent(TextUtils.isEmpty(sizeList.get(0).sku) ? objProduct.sku : sizeList.get(0).sku);
                        } else {
                            twoOrMoreSizeIntent(otherSkuList, colorList, objProduct);
                        }
                    } else {
                        // no size found
                        noSizeColorIntent(objProduct.sku);
                    }
                }
            }
        }
    }

    private boolean isNetworkConnected() {
        return NetworkManager.getInstance().isConnectedToNetwork(getActivity());
    }

    public void twoOrMoreSizeIntent(String colour, ArrayList<OtherSkus> otherSkuList, ArrayList<OtherSkus> colorList, WProductDetail objProduct) {
        Objects.requireNonNull(getGlobalState()).setColourSKUArrayList(colorList);
        Intent mIntent = new Intent(getActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("SELECTED_COLOUR", colour);
        mIntent.putExtra("OTHERSKU", Utils.toJson(otherSkuList));
        mIntent.putExtra("PRODUCT_HAS_COLOR", false);
        mIntent.putExtra("PRODUCT_HAS_SIZE", true);
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra("PRODUCT_NAME", objProduct.productName);
        getActivity().startActivityForResult(mIntent, COLOR_SIZE_SELECTION_REQUEST_CODE);
        getActivity().overridePendingTransition(0, 0);
    }

    private void intentSizeList(String color, OtherSkus otherSku, ArrayList<OtherSkus> otherSkuList, WProductDetail objProduct) {
        ArrayList<OtherSkus> sizeList = commonSizeList(otherSku);
        int sizeListSize = sizeList.size();
        if (sizeListSize > 0) {
            if (sizeListSize == 1) {
                // one size only
                OtherSkus otherSkus = sizeList.get(0);
                getProductAdapter().setSelectedSku(getSelectedProduct(), otherSkus);
                noSizeColorIntent(otherSkus.sku);
                minOneItemSelected(mProductList);
            } else {
                // size > 1
                twoOrMoreSizeIntent(color, otherSkuList, sizeList, objProduct);
            }
        } else {
            // no size
            noSizeColorIntent(otherSku.sku);
        }
    }

    public void noSizeColorIntent(String mSkuId) {
        OtherSkus otherSkus = new OtherSkus();
        otherSkus.sku = mSkuId;
        Objects.requireNonNull(getGlobalState()).setSelectedSKUId(otherSkus);
        Activity activity = getActivity();
        if (activity != null) {
            switch (getGlobalState().getSaveButtonClick()) {
                case INDEX_ADD_TO_SHOPPING_LIST:
                    openAddToListFragment(activity);
                    break;
                default:
                    break;
            }
        }
    }

    private void openAddToListFragment(Activity activity) {
        OtherSkus selectedSku = Objects.requireNonNull(getGlobalState()).getSelectedSKUId();
        AddToListRequest item = new AddToListRequest();
        item.setCatalogRefId(selectedSku.sku);
        item.setSkuID(selectedSku.sku);
        item.setGiftListId(selectedSku.sku);
        item.setQuantity("1");
        ArrayList<AddToListRequest> addToListRequests = new ArrayList<>();
        addToListRequests.add(item);
        NavigateToShoppingList.Companion.openShoppingList(activity, addToListRequests, "", false);
    }

    private void twoOrMoreColorIntent(ArrayList<OtherSkus> otherSkuList, ArrayList<OtherSkus> colorList, WProductDetail objProduct) {
        Objects.requireNonNull(getGlobalState()).setColourSKUArrayList(colorList);
        Intent mIntent = new Intent(getActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("COLOR_LIST", Utils.toJson(colorList));
        mIntent.putExtra("OTHERSKU", Utils.toJson(otherSkuList));
        mIntent.putExtra("PRODUCT_HAS_COLOR", true);
        mIntent.putExtra("PRODUCT_HAS_SIZE", true);
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra("PRODUCT_NAME", objProduct.productName);
        getActivity().startActivityForResult(mIntent, COLOR_SIZE_SELECTION_REQUEST_CODE);
        getActivity().overridePendingTransition(0, 0);
    }

    public void twoOrMoreSizeIntent(ArrayList<OtherSkus> otherSkuList, ArrayList<OtherSkus> colorList, WProductDetail objProduct) {
        Objects.requireNonNull(getGlobalState()).setColourSKUArrayList(colorList);
        Intent mIntent = new Intent(getActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("COLOR_LIST", Utils.toJson(colorList));
        mIntent.putExtra("OTHERSKU", Utils.toJson(otherSkuList));
        mIntent.putExtra("PRODUCT_HAS_COLOR", false);
        mIntent.putExtra("PRODUCT_HAS_SIZE", true);
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra("PRODUCT_NAME", objProduct.productName);
        getActivity().startActivityForResult(mIntent, COLOR_SIZE_SELECTION_REQUEST_CODE);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onLoadDetailFailure(String e) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cancelColorSizeSelection();
                }
            });
        }
    }

    @Override
    public void onFoodTypeChecked(List<ProductList> productLists, ProductList selectedProduct) {
        this.mProductList = productLists;
        toggleAddToListBtn(true);
    }

    @Override
    public void unknownErrorMessage(ShoppingListItemsResponse shoppingCartResponse) {
        onAddToListLoad(false);
        Activity activity = getActivity();
        if (activity != null)
            if (shoppingCartResponse != null) {
                Response response = shoppingCartResponse.response;
                if (response.desc != null) {
                    Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
                }
            }

    }

    @Override
    public void accountExpired(ShoppingListItemsResponse shoppingCartResponse) {
        if (shoppingCartResponse != null && shoppingCartResponse.response != null)
            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, shoppingCartResponse.response.stsParams, getActivity());

    }

    private void productDetailRequest(ProductRequest productRequest) {
        mGetProductDetail = getProductDetail(productRequest);
    }

    public void toggleAddToListBtn(boolean enable) {
        rlAddToList.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnCheckOut.setEnabled(enable);
    }

    public void setSelectedProduct(ProductList mSelectedProduct) {
        this.mSelectedProduct = mSelectedProduct;
    }

    public ProductList getSelectedProduct() {
        return mSelectedProduct;
    }

    private void connectionBroadcast() {
        connectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cancelRequest(mGetProductDetail);
        cancelRequest(mPostAddToList);
    }

    private void cancelRequest(Call call) {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        Activity activity = getActivity();
        if (activity != null) {
            activity.unregisterReceiver(connectionBroadcast);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.SHOPPING_LIST_SEARCH_RESULTS);
        Activity activity = getActivity();
        if (activity != null) {
            activity.registerReceiver(connectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }

    @Override
    public void onConnectionChanged() {
        retryConnect();
    }

    private void retryConnect() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (addToListLoadFail) {
                        btnCheckOut.performClick();
                    }
                }
            });
        }
    }

    Call<ProductView> searchProduct(final Context context, final ProductsRequestParams requestParams) {
        onLoadStart(getLoadMoreData());
        setProductIsLoading(true);

        Call<ProductView> productListCall =  OneAppService.INSTANCE.getProducts(requestParams);
        productListCall.enqueue(new CompletionHandler<>(new RequestListener<ProductView>() {
            @Override
            public void onSuccess(ProductView productView) {
                switch (productView.httpCode) {
                    case 200:
                        List<ProductList> productLists = productView.products;
                        if (productLists != null) {
                            numItemsInTotal(productView);
                            calculatePageOffset();
                            onLoadProductSuccess(productLists, getLoadMoreData());
                            onLoadComplete(getLoadMoreData());
                            setLoadMoreData(true);
                        }
                        break;

                    default:
                        if (productView.response != null) {
                            onLoadComplete(getLoadMoreData());
                            unhandledResponseCode(productView.response);
                        }
                        break;
                }
                setProductIsLoading(false);
            }

            @Override
            public void onFailure(final Throwable error) {
                if (error == null) return;
                if (context != null) {
                    Activity activity = (Activity) context;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setProductIsLoading(false);
                            failureResponseHandler(error.getMessage());
                            onLoadComplete(getLoadMoreData());
                        }
                    });
                }
            }
        },ProductView.class));

        return productListCall;
    }

    public Call<ShoppingListItemsResponse> addToList(List<AddToListRequest> addToListRequest, String listId) {
        onAddToListLoad(true);

        Call<ShoppingListItemsResponse> shoppingListItemsResponseCall = OneAppService.INSTANCE.addToList(addToListRequest,listId);
        shoppingListItemsResponseCall.enqueue(new CompletionHandler<>(new RequestListener<ShoppingListItemsResponse>() {
            @Override
            public void onSuccess(ShoppingListItemsResponse shoppingListItemsResponse) {
                switch (shoppingListItemsResponse.httpCode) {
                    case 200:
                        onAddToListLoadComplete();
                        break;

                    case 440:
                        accountExpired(shoppingListItemsResponse);
                        onAddToListLoad(false);
                        break;

                    default:
                        unknownErrorMessage(shoppingListItemsResponse);
                        break;
                }
            }

            @Override
            public void onFailure(Throwable error) {
                if (error == null) return;
                onAddToListFailure(error.getMessage());
            }
        },ShoppingListItemsResponse.class));

            return shoppingListItemsResponseCall;
    }

    public boolean getLoadMoreData() {
        return loadMoreData;
    }

    public void setIsLoading(boolean mIsLoading) {
        this.mIsLoading = mIsLoading;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public void setProductRequestBody(ProductsRequestParams.SearchType searchType, String searchTerm) {
        this.productsRequestParams = new ProductsRequestParams(searchTerm, searchType, ProductsRequestParams.ResponseType.DETAIL, pageOffset);
    }

    public ProductsRequestParams getProductRequestBody() {
        return productsRequestParams;
    }

    public void executeSearchProduct(Context context, ProductsRequestParams lp) {
        this.mGetProductsRequest = searchProduct(context, lp);
    }


    public void setLoadMoreData(boolean loadMoreData) {
        this.loadMoreData = loadMoreData;
    }

    private void numItemsInTotal(ProductView productView) {
        PagingResponse pagingResponse = productView.pagingResponse;
        if (pagingResponse.numItemsInTotal != null) {
            mNumItemsInTotal = pagingResponse.numItemsInTotal;
        }
    }

    public void canLoadMore(int totalItem, int sizeOfList) {
        if (sizeOfList >= totalItem) {
            setLoadMoreData(false);
        }
    }

    public int getNumItemsInTotal() {
        return mNumItemsInTotal;
    }

    private void calculatePageOffset() {
        pageOffset = pageOffset + Utils.PAGE_SIZE;
        getProductRequestBody().setPageOffset(pageOffset);
    }


    public ArrayList<OtherSkus> getColorList() {
        Collections.sort(getOtherSkus(), new Comparator<OtherSkus>() {
            @Override
            public int compare(OtherSkus lhs, OtherSkus rhs) {
                return lhs.colour.compareToIgnoreCase(rhs.colour);
            }
        });

        ArrayList<OtherSkus> commonColorSku = new ArrayList<>();
        for (OtherSkus sku : getOtherSkus()) {
            if (colourValueExist(commonColorSku, sku.colour)) {
                commonColorSku.add(sku);
            }
        }
        return commonColorSku;
    }

    public ArrayList<OtherSkus> getSizeList() {
        Collections.sort(getOtherSkus(), new Comparator<OtherSkus>() {
            @Override
            public int compare(OtherSkus lhs, OtherSkus rhs) {
                return lhs.size.compareToIgnoreCase(rhs.size);
            }
        });

        ArrayList<OtherSkus> commonColorSku = new ArrayList<>();
        for (OtherSkus sku : getOtherSkus()) {
            if (colourValueExist(commonColorSku, sku.size)) {
                commonColorSku.add(sku);
            }
        }
        return commonColorSku;
    }

    public boolean colourValueExist(ArrayList<OtherSkus> list, String name) {
        for (OtherSkus item : list) {
            if (item.colour.equals(name)) {
                return false;
            }
        }
        return true;
    }


    public void setOtherSkus(ArrayList<OtherSkus> otherSkus) {
        this.otherSkus = otherSkus;
    }

    public ArrayList<OtherSkus> getOtherSkus() {
        return otherSkus;
    }

    public ArrayList<OtherSkus> commonSizeList(OtherSkus otherSku) throws NullPointerException {
        ArrayList<OtherSkus> commonSizeList = new ArrayList<>();
        // filter by colour
        ArrayList<OtherSkus> sizeList = new ArrayList<>();
        for (OtherSkus sku : getOtherSkus()) {
            if (sku.colour != null) {
                if (sku.colour.equalsIgnoreCase(otherSku.colour)) {
                    sizeList.add(sku);
                }
            }
        }

        //remove duplicates
        for (OtherSkus os : sizeList) {
            if (!sizeValueExist(commonSizeList, os.size)) {
                commonSizeList.add(os);
            }
        }
        return commonSizeList;
    }

    private boolean sizeValueExist(ArrayList<OtherSkus> list, String name) {
        for (OtherSkus item : list) {
            if (item.size.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void setProductIsLoading(boolean productIsLoading) {
        this.productIsLoading = productIsLoading;
    }

    public boolean productIsLoading() {
        return productIsLoading;
    }

    public Call<ProductDetailResponse> getProductDetail(ProductRequest productRequest) {
       Call<ProductDetailResponse> productDetailRequest = OneAppService.INSTANCE.productDetail(productRequest.getProductId(), productRequest.getSkuId());
        productDetailRequest.enqueue(new CompletionHandler<>(new RequestListener<ProductDetailResponse>() {
            @Override
            public void onSuccess(ProductDetailResponse productDetailResponse) {
                String detailProduct = Utils.objectToJson(productDetailResponse);
                if (productDetailResponse.httpCode == 200) {
                    if (productDetailResponse.product != null) {
                        setOtherSkus(productDetailResponse.product.otherSkus);
                    }
                    WProduct product = (WProduct) Utils.strToJson(detailProduct, WProduct.class);
                    onSuccessResponse(product);
                } else {
                    if (productDetailResponse.response != null) {
                        responseFailureHandler(productDetailResponse.response);
                    }
                }
            }

            @Override
            public void onFailure(Throwable error) {
                if (error== null) return;
                onLoadDetailFailure(error.getMessage());
            }
        },ProductDetailResponse.class));

       return productDetailRequest;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COLOR_SIZE_SELECTION_REQUEST_CODE) {
            switch (resultCode) {
                case SELECTED_SHOPPING_LIST_ITEM_RESULT_CODE:
                    if (getProductAdapter() != null) {
                        getProductAdapter().setSelectedSku(getSelectedProduct(), Objects.requireNonNull(getGlobalState()).getSelectedSKUId());
                        toggleAddToListBtn(true);
                        minOneItemSelected(mProductList);
                    }
                    break;

                case CLOSE_ICON_TAPPED_RESULT_CODE:
                    cancelColorSizeSelection();
                    break;
                default:
                    break;
            }
        }
    }
}
