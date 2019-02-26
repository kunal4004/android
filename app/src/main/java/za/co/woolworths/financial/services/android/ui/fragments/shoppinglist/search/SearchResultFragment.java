package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.GridLayoutBinding;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.models.rest.product.GetProductDetail;
import za.co.woolworths.financial.services.android.models.rest.product.ProductRequest;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddToList;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.models.service.event.ShopState;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.SearchResultShopAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CANCEL_DIALOG_TAPPED;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.SHOW_ADDED_TO_SHOPPING_LIST_TOAST;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment.INDEX_ADD_TO_SHOPPING_LIST;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment.INDEX_SEARCH_FROM_LIST;

public class SearchResultFragment extends BaseFragment<GridLayoutBinding, SearchResultViewModel> implements SearchResultNavigator, View.OnClickListener, NetworkChangeListener {

    private final String TAG = this.getClass().getSimpleName();
    private SearchResultViewModel mGridViewModel;
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
    private GetProductDetail mGetProductDetail;
    private ProductList mSelectedProduct;
    private int mAddToListSize = 0;
    private boolean mToggleAddToList;
    private PostAddToList mPostAddToList;
    private BroadcastReceiver connectionBroadcast;
    private boolean addToListLoadFail = false;

    @Override
    public SearchResultViewModel getViewModel() {
        return mGridViewModel;
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.grid_layout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mGridViewModel = ViewModelProviders.of(this).get(SearchResultViewModel.class);
        mGridViewModel.setNavigator(this);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mSearchText = bundle.getString("searchTEXT");
            mListId = bundle.getString("listID");
        }
        setProductBody();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setTranslationZ(getView(), 100.f);
        showSearchResultToolbar();
        showBackNavigationIcon(true);
        setToolbarBackgroundDrawable(R.drawable.appbar_background);
        mProgressLimitStart = getViewDataBinding().incCenteredProgress.progressCreditLimit;
        RelativeLayout relNoConnectionLayout = getViewDataBinding().incNoConnectionHandler.noConnectionLayout;
        assert getViewDataBinding().incNoConnectionHandler != null;
        mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
        mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
        setTitle();
        startProductRequest();
        onBottomReached();
        getViewDataBinding().incNoConnectionHandler.btnRetry.setOnClickListener(this);
        setUpAddToListButton();
        final Activity activity = getActivity();
        if (activity != null) {
            observableOn(new Consumer() {
                @Override
                public void accept(Object object) throws Exception {
                    if (object != null) {
                        if (object instanceof ProductState) {
                            ProductState productState = (ProductState) object;
                            switch (productState.getState()) {
                                case ProductState.INDEX_SEARCH_FROM_LIST:
                                    if (getProductAdapter() != null) {
                                        getProductAdapter().setSelectedSku(getSelectedProduct(), getGlobalState().getSelectedSKUId());
                                    }
                                    toggleAddToListBtn(true);
                                    minOneItemSelected(mProductList);
                                    break;

                                case SHOW_ADDED_TO_SHOPPING_LIST_TOAST:
                                    RelativeLayout rlAddToList = getViewDataBinding().incConfirmButtonLayout.rlCheckOut;
                                    ToastUtils toastUtils = new ToastUtils();
                                    toastUtils.setActivity(getActivity());
                                    toastUtils.setCurrentState(TAG);
                                    String shoppingList = activity.getResources().getString(R.string.shopping_list);
                                    toastUtils.setCartText((productState.getCount() > 1) ? shoppingList + "s" : shoppingList);
                                    // Set Toast above button if add to list is visible
                                    toastUtils.setPixel(mToggleAddToList ? rlAddToList.getHeight() * 2 - Utils.dp2px(activity, 8) : 0);
                                    toastUtils.setView(rlAddToList);
                                    toastUtils.setMessage(R.string.added_to);
                                    toastUtils.build();
                                    break;

                                case CANCEL_DIALOG_TAPPED:
                                    cancelColorSizeSelection();
                                    break;

                                default:
                                    break;
                            }
                        }
                    }
                }
            });
        }

        connectionBroadcast();
    }

    private void setUpAddToListButton() {
        getViewDataBinding().incConfirmButtonLayout.btnCheckOut.setOnClickListener(this);
        setText(getViewDataBinding().incConfirmButtonLayout.btnCheckOut, getString(R.string.add_to_list));
        toggleAddToListBtn(false);
    }

    private void setTitle() {
        if (mSearchText != null)
            setTitle(mSearchText);
    }


    @Override
    public void onLoadProductSuccess(List<ProductList> productLists, boolean loadMoreData) {
        if (productLists != null) {
            if (productLists.size() == 1) {
                getBottomNavigator().popFragmentNoAnim();
                getBottomNavigator().openProductDetailFragment(mSearchText, productLists.get(0));
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
        Activity activity = getBaseActivity();
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
        if (mGridViewModel != null) {
            mGridViewModel.cancelRequest(mGridViewModel.getSearchProductRequest());
        }
    }

    @Override
    public void bindRecyclerViewWithUI(List<ProductList> productList) {
        this.mProductList = productList;

        if (!listContainHeader()) {
            ProductList headerProduct = new ProductList();
            headerProduct.viewTypeHeader = true;
            headerProduct.numberOfItems = getViewModel().getNumItemsInTotal();
            productList.add(0, headerProduct);
        }

        mProductAdapter = new SearchResultShopAdapter(mProductList, this);
        mRecyclerViewLayoutManager = new LinearLayoutManager(getActivity());
        getViewDataBinding().productList.setLayoutManager(mRecyclerViewLayoutManager);
        getViewDataBinding().productList.setNestedScrollingEnabled(false);
        getViewDataBinding().productList.setAdapter(getProductAdapter());
        getViewDataBinding().productList.setItemAnimator(null);
        getViewDataBinding().productList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
            if (getViewModel().productIsLoading())
                return;
            int Total = getViewModel().getNumItemsInTotal() + Utils.PAGE_SIZE;
            int start = mProductList.size();
            int end = start + Utils.PAGE_SIZE;
            isLoading = (Total < end);
            if (isLoading) {
                return;
            }
            if (!listContainFooter()) {
                ProductList footerItem = new ProductList();
                footerItem.viewTypeFooter = true;
                mProductList.add(footerItem);
                getProductAdapter().notifyItemInserted(mProductList.size() - 1);
            }
            startProductRequest();
        }
    }

    private boolean listContainFooter() {
        if (mProductList == null) return false;
        for (ProductList pl : mProductList) {
            if (pl.viewTypeFooter) {
                return true;
            }
        }
        return false;
    }

    private void removeFooter() {
        int index = 0;
        for (ProductList pl : mProductList) {
            if (pl.viewTypeFooter) {
                mProductList.remove(pl);
                getProductAdapter().notifyItemRemoved(index);
                return;
            }
            index++;
        }
    }

    private boolean listContainHeader() {
        for (ProductList pl : mProductList) {
            if (pl.viewTypeHeader) {
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
    public void onBottomReached() {
    }

    @Override
    public void startProductRequest() {
        getViewModel().executeSearchProduct(getActivity(), getViewModel().getProductRequestBody());
    }

    @Override
    public void loadMoreData(List<ProductList> productLists) {
        int actualSize = mProductList.size() + 1;
        mProductList.addAll(productLists);
        int sizeOfList = mProductList.size();
        getProductAdapter().notifyItemChanged(actualSize, sizeOfList);
        getViewModel().canLoadMore(getViewModel().getNumItemsInTotal(), sizeOfList);
    }

    @Override
    public void setProductBody() {
        getViewModel().setProductRequestBody(ProductsRequestParams.SearchType.SEARCH, mSearchText);
    }

    @Override
    public void onLoadStart(boolean isLoadMore) {
        getViewModel().setIsLoading(true);
        if (!isLoadMore) {
            showView(mProgressLimitStart);
            mProgressLimitStart.bringToFront();
        }
    }

    @Override
    public void onLoadComplete(boolean isLoadMore) {
        if (listContainFooter()) {
            removeFooter();
        }
        getViewModel().setIsLoading(false);
        if (!isLoadMore) {
            hideView(mProgressLimitStart);
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
                        addToList.setSkuID(list.productId);
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
        mPostAddToList = getViewModel().addToList(addToListRequests, mListId);
        mPostAddToList.execute();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            showSearchResultToolbar();
        }
    }


    @Override
    public void onFoodTypeSelect(ProductList productList) {
        getBottomNavigator().openProductDetailFragment(mSearchText, productList);
    }

    @Override
    public void onClothingTypeSelect(ProductList productList) {
        getBottomNavigator().openProductDetailFragment(mSearchText, productList);
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
        ProgressBar progressBar = getViewDataBinding().incConfirmButtonLayout.pbLoadingIndicator;
        WButton btnCheck0ut = getViewDataBinding().incConfirmButtonLayout.btnCheckOut;
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnCheck0ut.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onAddToListLoadComplete(List<ShoppingListItem> listItems) {
        addToListLoadFail = false;
        ProgressBar progressBar = getViewDataBinding().incConfirmButtonLayout.pbLoadingIndicator;
        WButton btnCheck0ut = getViewDataBinding().incConfirmButtonLayout.btnCheckOut;
        progressBar.setVisibility(View.GONE);
        btnCheck0ut.setVisibility(View.VISIBLE);
        sendBus(new ShopState(new ArrayList<ShoppingListItem>(), mAddToListSize));
        popFragmentSlideDown();
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
    public void onLoadStart() {
    }

    @Override
    public void responseFailureHandler(Response response) {
    }

    @Override
    public void onSuccessResponse(WProduct product) {
        getGlobalState().saveButtonClicked(INDEX_SEARCH_FROM_LIST);
        getProductAdapter().setCheckedProgressBar(getSelectedProduct());
        if (isNetworkConnected()) {
            ArrayList<OtherSkus> otherSkuList = getViewModel().getOtherSkus();
            ArrayList<OtherSkus> colorList = getViewModel().getColorList();
            ArrayList<OtherSkus> sizeList = getViewModel().getSizeList();

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
                    intentSizeList(color, colorList.get(0), otherSkuList, colorList, objProduct);  // open size intent with color as filter
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

    public void twoOrMoreSizeIntent(String colour, ArrayList<OtherSkus> otherSkuList, ArrayList<OtherSkus> colorList, WProductDetail objProduct) {
        getGlobalState().setColourSKUArrayList(colorList);
        Intent mIntent = new Intent(getBaseActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("SELECTED_COLOUR", colour);
        mIntent.putExtra("OTHERSKU", Utils.toJson(otherSkuList));
        mIntent.putExtra("PRODUCT_HAS_COLOR", false);
        mIntent.putExtra("PRODUCT_HAS_SIZE", true);
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra("PRODUCT_NAME", objProduct.productName);
        startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
        getBaseActivity().overridePendingTransition(0, 0);
    }

    private void intentSizeList(String color, OtherSkus otherSku, ArrayList<OtherSkus> otherSkuList, ArrayList<OtherSkus> colorList, WProductDetail objProduct) {
        ArrayList<OtherSkus> sizeList = getViewModel().commonSizeList(otherSku);
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
        getGlobalState().setSelectedSKUId(otherSkus);
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
        OtherSkus selectedSku = getGlobalState().getSelectedSKUId();
        AddToListRequest item = new AddToListRequest();
        item.setCatalogRefId(selectedSku.sku);
        item.setSkuID(selectedSku.sku);
        item.setGiftListId(selectedSku.sku);
        item.setQuantity("1");
        ArrayList<AddToListRequest> addToListRequests = new ArrayList<>();
        addToListRequests.add(item);
        NavigateToShoppingList navigateToShoppingList = new NavigateToShoppingList();
        navigateToShoppingList.openShoppingList(activity, addToListRequests, "", false);
    }

    private void twoOrMoreColorIntent(ArrayList<OtherSkus> otherSkuList, ArrayList<OtherSkus> colorList, WProductDetail objProduct) {
        getGlobalState().setColourSKUArrayList(colorList);
        Intent mIntent = new Intent(getBaseActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("COLOR_LIST", Utils.toJson(colorList));
        mIntent.putExtra("OTHERSKU", Utils.toJson(otherSkuList));
        mIntent.putExtra("PRODUCT_HAS_COLOR", true);
        mIntent.putExtra("PRODUCT_HAS_SIZE", true);
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra("PRODUCT_NAME", objProduct.productName);
        startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
        getBaseActivity().overridePendingTransition(0, 0);
    }

    public void twoOrMoreSizeIntent(ArrayList<OtherSkus> otherSkuList, ArrayList<OtherSkus> colorList, WProductDetail objProduct) {
        getGlobalState().setColourSKUArrayList(colorList);
        Intent mIntent = new Intent(getBaseActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("COLOR_LIST", Utils.toJson(colorList));
        mIntent.putExtra("OTHERSKU", Utils.toJson(otherSkuList));
        mIntent.putExtra("PRODUCT_HAS_COLOR", false);
        mIntent.putExtra("PRODUCT_HAS_SIZE", true);
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra("PRODUCT_NAME", objProduct.productName);
        startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
        getBaseActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onLoadComplete() {
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
        Activity activity = getActivity();
        if (activity != null) {
            if (shoppingCartResponse.response != null) {
                if (shoppingCartResponse.response.stsParams != null) {
                    SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, shoppingCartResponse.response.stsParams, getActivity());
                }
            }
        }
    }

    private void productDetailRequest(ProductRequest productRequest) {
        mGetProductDetail = getViewModel().getProductDetail(productRequest);
        mGetProductDetail.execute();
    }

    public void toggleAddToListBtn(boolean enable) {
        mToggleAddToList = enable;
        RelativeLayout rlAddToList = getViewDataBinding().incConfirmButtonLayout.rlCheckOut;
        WButton btnAddToList = getViewDataBinding().incConfirmButtonLayout.btnCheckOut;
        rlAddToList.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnAddToList.setEnabled(enable);
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
            showSearchResultToolbar();
            activity.registerReceiver(connectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }

    public void showSearchResultToolbar() {
        showToolbar();
        showBackNavigationIcon(true);
        setToolbarBackgroundDrawable(R.drawable.appbar_background);
        setTitle();
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
                        getViewDataBinding().incConfirmButtonLayout.btnCheckOut.performClick();
                    }
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showSearchResultToolbar();
    }
}
