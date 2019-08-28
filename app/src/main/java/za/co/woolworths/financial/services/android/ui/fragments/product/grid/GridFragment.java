package za.co.woolworths.financial.services.android.ui.fragments.product.grid;

import android.app.Activity;
import android.app.Dialog;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.GridLayoutBinding;
import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IProductListing;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.AddToCartDaTum;
import za.co.woolworths.financial.services.android.models.dto.FormException;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams;
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.SkuInventory;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.dto.SortOption;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity;
import za.co.woolworths.financial.services.android.ui.activities.product.refine.ProductsRefineActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ProductListingAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.SortOptionsAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewType;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.views.ProductListingProgressBar;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SelectYourQuantityFragment;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.PostItemToCart;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragmentNew.SET_DELIVERY_LOCATION_REQUEST_CODE;

public class GridFragment extends BaseFragment<GridLayoutBinding, GridViewModel> implements GridNavigator, IProductListing, View.OnClickListener, SortOptionsAdapter.OnSortOptionSelected, WMaterialShowcaseView.IWalkthroughActionListener {

    private GridViewModel mGridViewModel;
    private ErrorHandlerView mErrorHandlerView;
    private String mSubCategoryId;
    private String mSubCategoryName;
    private String mSearchProduct;
    private ProductListingAdapter mProductAdapter;
    private List<ProductList> mProductList;
    private GridLayoutManager mRecyclerViewLayoutManager;
    private int lastVisibleItem;
    int totalItemCount;
    private boolean isLoading;
    private ProductView productView;
    public static final String REFINEMENT_DATA = "REFINEMENT_DATA";
    public static final String PRODUCTS_REQUEST_PARAMS = "PRODUCTS_REQUEST_PARAMS";
    public static final int REFINE_REQUEST_CODE = 77;
    private Dialog sortOptionDialog;
    private ProductListingProgressBar mProgressListingProgressBar = new ProductListingProgressBar();
    private static final int QUERY_INVENTORY_FOR_STORE_REQUEST_CODE = 3343;
    private String mStoreId;
    private AddItemToCart mAddItemToCart;

    @Override
    public GridViewModel getViewModel() {
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
        mGridViewModel = ViewModelProviders.of(this).get(GridViewModel.class);
        mGridViewModel.setNavigator(this);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mSubCategoryId = bundle.getString("sub_category_id");
            mSubCategoryName = bundle.getString("sub_category_name");
            mSearchProduct = bundle.getString("str_search_product");
        }
        setProductBody();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showToolbar();
        showBackNavigationIcon(true);
        setToolbarBackgroundDrawable(R.drawable.appbar_background);

        Activity activity = getActivity();
        if (activity != null) {
            BottomNavigationActivity bottomNavigationActivity = (BottomNavigationActivity) activity;
            bottomNavigationActivity.getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popFragment();
                }
            });
        }

        RelativeLayout relNoConnectionLayout = getViewDataBinding().incNoConnectionHandler.noConnectionLayout;
        assert getViewDataBinding().incNoConnectionHandler != null;
        mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
        mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
        setTitle();
        startProductRequest();
        getViewDataBinding().incNoConnectionHandler.btnRetry.setOnClickListener(this);
        getViewDataBinding().sortAndRefineLayout.refineProducts.setOnClickListener(this);
        getViewDataBinding().sortAndRefineLayout.sortProducts.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_SEARCH_RESULTS);
    }

    private void setTitle() {
        if (isEmpty(mSearchProduct)) {
            setTitle(mSubCategoryName);
        } else {
            setTitle(mSearchProduct);
        }
    }

    @Override
    public void onLoadProductSuccess(ProductView response, boolean loadMoreData) {
        List<ProductList> productLists = response.products;
        if (mProductList == null) {
            mProductList = new ArrayList<>();
        }
        if (productLists.isEmpty()) {
            getViewDataBinding().sortAndRefineLayout.parentLayout.setVisibility(View.GONE);
            if (!listContainHeader()) {
                ProductList headerProduct = new ProductList();
                headerProduct.rowType = ProductListingViewType.HEADER;
                headerProduct.numberOfItems = getViewModel().getNumItemsInTotal();
                productLists.add(0, headerProduct);
            }
            bindRecyclerViewWithUI(productLists);
        } else if (productLists.size() == 1) {
            getBottomNavigator().popFragmentNoAnim();
            getBottomNavigator().openProductDetailFragment(mSubCategoryName, productLists.get(0));

        } else {
            this.productView = response;
            hideFooterView();
            if (!loadMoreData) {
                getViewDataBinding().sortAndRefineLayout.parentLayout.setVisibility(View.VISIBLE);
                setRefinementViewState(getRefinementViewState(productView.navigation));
                bindRecyclerViewWithUI(productLists);
                showFeatureWalkthrough();
            } else {
                loadMoreData(productLists);
            }
        }

    }

    @Override
    public void unhandledResponseCode(Response response) {
        Activity activity = getActivity();
        if (activity == null) return;
        if (response.desc == null) return;
        hideFooterView();
        FragmentTransaction fm = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        // check if sortOptionDialog is being displayed
        if (hasOpenedDialogs((AppCompatActivity) activity)) return;

        // show sortOptionDialog
        SingleButtonDialogFragment singleButtonDialogFragment = SingleButtonDialogFragment.newInstance(response.desc);
        singleButtonDialogFragment.show(fm, SingleButtonDialogFragment.class.getSimpleName());

    }

    private boolean hasOpenedDialogs(AppCompatActivity activity) {
        List<Fragment> fragments = activity.getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof DialogFragment) {
                    return true;
                }
            }
        }

        return false;
    }

    private void hideFooterView() {
        if (listContainFooter())
            removeFooter();
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

    @Override
    public void cancelAPIRequest() {
        if (mGridViewModel != null) {
            mGridViewModel.cancelRequest(mGridViewModel.getLoadProductRequest());
        }
    }

    @Override
    public void bindRecyclerViewWithUI(final List<ProductList> productList) {
        this.mProductList = productList;
        if (!listContainHeader()) {
            ProductList headerProduct = new ProductList();
            headerProduct.rowType = ProductListingViewType.HEADER;
            headerProduct.numberOfItems = getViewModel().getNumItemsInTotal();
            mProductList.add(0, headerProduct);
        }

        mProductAdapter = new ProductListingAdapter(this, mProductList);

        mRecyclerViewLayoutManager = new GridLayoutManager(getActivity(), 2);
        // Set up a GridLayoutManager to change the SpanSize of the header and footer
        mRecyclerViewLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position > mProductList.size() - 1) {
                    //this is a failsafe to prevent ever getting
                    //the IndexOutOfBoundsException
                    return 1;
                }

                //header should have span size of 2, and regular item should have span size of 1
                boolean isHeader = mProductList.get(position).rowType == ProductListingViewType.HEADER;
                boolean isFooter = mProductList.get(position).rowType == ProductListingViewType.FOOTER;

                return (isHeader || isFooter) ? 2 : 1;
            }
        });


        final RecyclerView rcvProductList = getViewDataBinding().productList;
        if (rcvProductList.getVisibility() == View.INVISIBLE)
            rcvProductList.setVisibility(View.VISIBLE);

        rcvProductList.setLayoutManager(mRecyclerViewLayoutManager);
        rcvProductList.setAdapter(mProductAdapter);

        rcvProductList.clearOnScrollListeners();
        rcvProductList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = mRecyclerViewLayoutManager.getItemCount();
                lastVisibleItem = mRecyclerViewLayoutManager.findLastVisibleItemPosition();

                // Detect scrolling up
                if (dy > 0)
                    loadData();
            }
        });

        //for some reason, when we change the visibility
        //before setting the updated Adapter, the adapter still remembers
        //the results from the previous listed data. This of course may be different in sizes
        //and therefor we can most likely expect a IndexOutOfBoundsExeption
        if (rcvProductList.getVisibility() == View.INVISIBLE)
            rcvProductList.setVisibility(View.VISIBLE);
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
                footerItem.rowType = ProductListingViewType.FOOTER;
                mProductList.add(footerItem);
                mProductAdapter.notifyItemInserted(mProductList.size() - 1);
            }
            startProductRequest();
        }
    }

    private boolean listContainFooter() {
        try {
            for (ProductList pl : mProductList) {
                if (pl.rowType == ProductListingViewType.FOOTER) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private void removeFooter() {
        int index = 0;
        for (ProductList pl : mProductList) {
            if (pl.rowType == ProductListingViewType.FOOTER) {
                mProductList.remove(pl);
                mProductAdapter.notifyItemRemoved(index);
                return;
            }
            index++;
        }
    }

    private boolean listContainHeader() {
        if (mProductList != null) {
            for (ProductList pl : mProductList) {
                if (pl.rowType == ProductListingViewType.HEADER) {
                    return true;
                }
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
    public void onDetach() {
        super.onDetach();
        if (getBottomNavigationActivity() != null && getBottomNavigationActivity().walkThroughPromtView != null) {
            getBottomNavigationActivity().walkThroughPromtView.removeFromWindow();
        }
    }

    @Override
    public void startProductRequest() {
        if (isEmpty(mSearchProduct)) {
            getViewModel().executeLoadProduct(getActivity(), getViewModel().getProductRequestBody());
        } else {
            getViewModel().executeLoadProduct(getActivity(), getViewModel().getProductRequestBody());
        }
    }

    @Override
    public void loadMoreData(List<ProductList> productLists) {
        int actualSize = mProductList.size() + 1;
        mProductList.addAll(productLists);
        int sizeOfList = mProductList.size();
        try {
            hideFooterView();
        } catch (Exception ex) {
            Log.e("containFooter", ex.getMessage());
        }
        mProductAdapter.notifyItemChanged(actualSize, sizeOfList);
        getViewModel().canLoadMore(getViewModel().getNumItemsInTotal(), sizeOfList);
    }

    @Override
    public void setProductBody() {
        if (isEmpty(mSearchProduct)) {
            getViewModel().setProductRequestBody(ProductsRequestParams.SearchType.NAVIGATE, mSubCategoryId);
        } else {
            getViewModel().setProductRequestBody(ProductsRequestParams.SearchType.SEARCH, mSearchProduct);
        }
    }

    @Override
    public void onLoadStart(boolean isLoadMore) {
        getViewModel().setIsLoading(true);
        if (!isLoadMore) {
            showProgressBar();
        }
    }

    @Override
    public void onLoadComplete(boolean isLoadMore) {
        getViewModel().setIsLoading(false);
        if (!isLoadMore) {
            dismissProgressBar();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.drill_down_category_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_drill_search:
                Intent openSearchActivity = new Intent(getBaseActivity(), ProductSearchActivity.class);
                startActivity(openSearchActivity);
                getBaseActivity().overridePendingTransition(0, 0);
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnRetry:
                if (isNetworkConnected()) {
                    mErrorHandlerView.hideErrorHandler();
                    startProductRequest();
                }
                break;
            case R.id.refineProducts:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.REFINE_EVENT_APPEARED);
                Intent intent = new Intent(getActivity(), ProductsRefineActivity.class);
                intent.putExtra(REFINEMENT_DATA, Utils.toJson(productView));
                intent.putExtra(PRODUCTS_REQUEST_PARAMS, Utils.toJson(getViewModel().getProductRequestBody()));
                startActivityForResult(intent, REFINE_REQUEST_CODE);
                getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
                break;
            case R.id.sortProducts:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SORTBY_EVENT_APPEARED);
                this.showShortOptions(productView.sortOptions);
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            showToolbar();
            showBackNavigationIcon(true);
            setToolbarBackgroundDrawable(R.drawable.appbar_background);
            setTitle();
        }
    }

    @Override
    public void onSortOptionSelected(@NotNull SortOption sortOption) {
        if (sortOptionDialog != null && sortOptionDialog.isShowing()) {
            sortOptionDialog.dismiss();
            Map<String, String> arguments = new HashMap<>();
            arguments.put(FirebaseManagerAnalyticsProperties.PropertyNames.SORT_OPTION_NAME, sortOption.getLabel());
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SORTBY_EVENT_APPLIED, arguments);
            getViewModel().updateProductRequestBodyForSort(sortOption.getSortOption());
            reloadProductsWithSortAndFilter();
        }
    }

    public void showShortOptions(ArrayList<SortOption> sortOptions) {
        sortOptionDialog = new Dialog(getActivity());
        sortOptionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = getLayoutInflater().inflate(R.layout.sort_options_view, null);
        RecyclerView rcvSortOptions = view.findViewById(R.id.sortOptionsList);
        rcvSortOptions.setLayoutManager(new LinearLayoutManager(getActivity()));
        rcvSortOptions.setAdapter(new SortOptionsAdapter(getActivity(), sortOptions, this));
        sortOptionDialog.setContentView(view);
        Window window = sortOptionDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(R.color.transparent);
            window.setGravity(Gravity.TOP);
        }
        sortOptionDialog.setTitle(null);
        sortOptionDialog.setCancelable(true);
        sortOptionDialog.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case QUERY_INVENTORY_FOR_STORE_REQUEST_CODE:
                if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue())
                    queryInventoryForStore(mStoreId, mAddItemToCart);
                break;

            case REFINE_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    String navigationState = data.getStringExtra(ProductsRefineActivity.NAVIGATION_STATE);
                    getViewModel().updateProductRequestBodyForRefinement(navigationState);
                    reloadProductsWithSortAndFilter();
                }
                break;
            default:
                break;
        }
    }

    public void reloadProductsWithSortAndFilter() {
        getViewDataBinding().productList.setVisibility(View.INVISIBLE);
        getViewDataBinding().sortAndRefineLayout.parentLayout.setVisibility(View.GONE);
        startProductRequest();
    }

    private void showFeatureWalkthrough() {
        if (!isAdded() || !AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.refineProducts)
            return;
        Crashlytics.setString(getString(R.string.crashlytics_materialshowcase_key), this.getClass().getCanonicalName());
        getBottomNavigationActivity().walkThroughPromtView = new WMaterialShowcaseView.Builder(getActivity(), WMaterialShowcaseView.Feature.REFINE)
                .setTarget(getViewDataBinding().sortAndRefineLayout.refineDownArrow)
                .setTitle(R.string.walkthrough_refine_title)
                .setDescription(R.string.walkthrough_refine_desc)
                .setActionText(R.string.walkthrough_refine_action)
                .setImage(R.drawable.tips_tricks_ic_refine)
                .setShapePadding(48)
                .setAction(this)
                .setAsNewFeature()
                .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_RIGHT)
                .setMaskColour(getResources().getColor(R.color.semi_transparent_black)).build();
        getBottomNavigationActivity().walkThroughPromtView.show(getActivity());

    }

    @Override
    public void onWalkthroughActionButtonClick() {
        if (getViewDataBinding().sortAndRefineLayout.refineProducts.isClickable())
            onClick(getViewDataBinding().sortAndRefineLayout.refineProducts);
    }

    @Override
    public void onPromptDismiss() {

    }

    public boolean getRefinementViewState(ArrayList<RefinementNavigation> navigationList) {
        if (navigationList.size() == 0)
            return false;
        for (RefinementNavigation navigation : navigationList) {
            if (navigation.getDisplayName().equalsIgnoreCase("On Promotion"))
                return true;
            else if (navigation.getRefinements().size() > 0 || navigation.getRefinementCrumbs().size() > 0)
                return true;
        }

        return false;
    }

    private void setRefinementViewState(boolean refinementViewState) {
        getViewDataBinding().sortAndRefineLayout.refineProducts.setEnabled(refinementViewState);
        getViewDataBinding().sortAndRefineLayout.refineDownArrow.setEnabled(refinementViewState);
        getViewDataBinding().sortAndRefineLayout.refinementText.setEnabled(refinementViewState);
    }

    @Override
    public void openProductDetailView(@NotNull ProductList productList) {
        if (!isEmpty(mSearchProduct)) {
            mSubCategoryName = mSearchProduct;
        }
        getBottomNavigator().openProductDetailFragment(mSubCategoryName, productList);
    }

    @Override
    public void queryInventoryForStore(@NotNull String storeId, @NotNull final AddItemToCart addItemToCart) {
        mStoreId = storeId;
        mAddItemToCart = addItemToCart;
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        if (!SessionUtilities.getInstance().isUserAuthenticated()) {
            ScreenManager.presentSSOSignin(activity, QUERY_INVENTORY_FOR_STORE_REQUEST_CODE);
            return;
        }

        showProgressBar();
        OneAppService.INSTANCE.getInventorySkuForStore(storeId, addItemToCart.getCatalogRefId()).enqueue(new CompletionHandler<>(new RequestListener<SkusInventoryForStoreResponse>() {
            @Override
            public void onSuccess(SkusInventoryForStoreResponse skusInventoryForStoreResponse) {
                dismissProgressBar();
                switch (skusInventoryForStoreResponse.httpCode) {
                    case 200:
                        List<SkuInventory> skuInventory = skusInventoryForStoreResponse.skuInventory;
                        if (skuInventory.size() == 0 || skuInventory.get(0).quantity == 0) {
                            ProductListingFindInStoreNoQuantityFragment productListingFindInStoreNoQuantityFragment = ProductListingFindInStoreNoQuantityFragment.Companion.newInstance();
                            productListingFindInStoreNoQuantityFragment.show(activity.getSupportFragmentManager().beginTransaction(), SelectYourQuantityFragment.class.getSimpleName());
                        } else {
                            AddItemToCart cartItem = new AddItemToCart(addItemToCart.getProductId(), addItemToCart.getCatalogRefId(), skuInventory.get(0).quantity);
                            SelectYourQuantityFragment selectYourQuantityFragment = SelectYourQuantityFragment.Companion.newInstance(cartItem, GridFragment.this);
                            selectYourQuantityFragment.show(activity.getSupportFragmentManager().beginTransaction(), SelectYourQuantityFragment.class.getSimpleName());
                        }
                        break;

                    case 440:
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(Throwable error) {
                dismissProgressBar();
            }
        }, SkusInventoryForStoreResponse.class));
    }

    private void showProgressBar() {
        // Show progress bar
        mProgressListingProgressBar.show(getActivity());
    }

    private void dismissProgressBar() {
        // hide progress bar
        mProgressListingProgressBar.dialog.dismiss();
    }

    @Override
    public void addFoodProductTypeToCart(@NotNull AddItemToCart addItemToCart) {
        showProgressBar();
        ArrayList<AddItemToCart> addItemToCarts = new ArrayList<>();
        addItemToCarts.add(addItemToCart);
        PostItemToCart postItemToCart = new PostItemToCart();
        postItemToCart.make(addItemToCarts, new RequestListener<AddItemToCartResponse>() {
            @Override
            public void onSuccess(AddItemToCartResponse addItemToCartResponse) {
                final Activity activity = getActivity();
                if (!isAdded() || activity == null) return;
                dismissProgressBar();
                switch (addItemToCartResponse.httpCode) {
                    case 200:
                        // Preferred Delivery Location has been reset on server
                        // As such, we give the user the ability to set their location again
                        List<AddToCartDaTum> addToCartList = addItemToCartResponse.data;
                        if (addToCartList != null && addToCartList.size() > 0 && addToCartList.get(0).formexceptions != null) {
                            FormException formException = addToCartList.get(0).formexceptions.get(0);
                            if (formException != null) {
                                if (formException.message.toLowerCase().contains("some of the products chosen are out of stock")) {
                                    addItemToCartResponse.response.desc = "Unfortunately this item is currently out of stock.";
                                } else {
                                    addItemToCartResponse.response.desc = formException.message;
                                }
                                Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, addItemToCartResponse.response.desc);
                                return;



                            }
                        }
                        break;

                    case 417:
                        Utils.displayValidationMessageForResult(GridFragment.this,
                                getActivity(),
                                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                                null,
                                addItemToCartResponse.response.desc, getResources().getString(R.string.set_delivery_location_button), SET_DELIVERY_LOCATION_REQUEST_CODE);
                        break;

                    case 440:
                        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE);
                        ScreenManager.presentSSOSignin(activity);
                        break;

                    default:
                        Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, addItemToCartResponse.response.desc);
                        break;
                }
            }

            @Override
            public void onFailure(Throwable error) {
                Activity activity = getActivity();
                if (activity == null) return;
                activity.runOnUiThread(() -> dismissProgressBar());
            }
        });
    }

}
