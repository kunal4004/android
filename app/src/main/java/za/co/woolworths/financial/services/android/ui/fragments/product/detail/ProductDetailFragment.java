package za.co.woolworths.financial.services.android.ui.fragments.product.detail;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ProductDetailViewBinding;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.functions.Consumer;
import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.ILocationProvider;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.AddToCartDaTum;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.FormException;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductRequest;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.SetDeliveryLocationSuburbResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.SkuInventory;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity;
import za.co.woolworths.financial.services.android.ui.activities.MultipleImageActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ProductUtils;
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FuseLocationAPISingleton;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static android.app.Activity.RESULT_OK;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CANCEL_DIALOG_TAPPED;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CLOSE_PDP_FROM_ADD_TO_LIST;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.DETERMINE_LOCATION_POPUP;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.OPEN_ADD_TO_SHOPPING_LIST_VIEW;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.POST_ADD_ITEM_TO_CART;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.SET_SUBURB;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.SET_SUBURB_API;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.USE_MY_LOCATION;
import static za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity.RESULT_LOADING_INVENTORY_FAILURE;
import static za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity.RESULT_TAP_FIND_INSTORE_BTN;
import static za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity.DELIVERY_LOCATION_CLOSE_CLICKED;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.BOTTOM_FRAGMENT_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailViewModel.CLOTHING_PRODUCT;
import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailViewModel.FOOD_PRODUCT;


/**
 * TODO:: ProductDetailFragment class must be deleted. It is still being used in other section of the app.
 */
public class ProductDetailFragment extends BaseFragment<ProductDetailViewBinding, ProductDetailViewModel> implements ProductDetailNavigator, ProductViewPagerAdapter.MultipleImageInterface, View.OnClickListener, NetworkChangeListener, ToastUtils.ToastInterface, ILocationProvider {

    public static final int INDEX_STORE_FINDER = 1;
    public static final int INDEX_ADD_TO_CART = 2;
    public static final int INDEX_ADD_TO_SHOPPING_LIST = 3;
    public static final int INDEX_SEARCH_FROM_LIST = 4;
    private int DEFAULT_PICKER = 0;

    public static final int RESULT_FROM_ADD_TO_CART_PRODUCT_DETAIL = 4002;
    private ProductDetailViewModel productDetailViewModel;
    private List<String> mAuxiliaryImage = new ArrayList<>();
    private String mSubCategoryTitle;
    private String TAG = this.getClass().getSimpleName();

    private ImageView[] ivArrayDotsPager;
    private Call<LocationResponse> mLocationItemTask;

    private boolean mProductHasColour;
    private boolean mProductHasSize;
    private boolean mProductHasOneColour;
    private boolean mProductHasOneSize;
    private OtherSkus mSkuId;

    private BroadcastReceiver mConnectionBroadcast;
    private ErrorHandlerView mErrorHandlerView;
    private boolean mFetchFromJson;
    private String mDefaultProductResponse;
    private Call<CartSummaryResponse> mGetCartSummary;
    private AddItemToCart mApiAddItemToCart;
    private Call<AddItemToCartResponse> mPostAddItemToCart;
    private ArrayList<OtherSkus> mSizeSkuList;
    private ArrayList<OtherSkus> mSkuColorList;
    private boolean activate_location_popup = false;
    private ToastUtils mToastUtils;
    private int mNumberOfListSelected = 0;
    private final int ACCESS_FINE_LOCATION_REQUEST_CODE = 1;
    private LinearLayout llStoreFinder;
    private OtherSkus selectedFindInStoreOtherSkus;
    public static int DELIVERY_LOCATION_FROM_PDP_REQUEST = 2553;
    private InventoryForStore mInventoryForStore;
    private FuseLocationAPISingleton mFuseLocationAPISingleton;

    @Override
    public ProductDetailViewModel getViewModel() {
        return productDetailViewModel;
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.product_detail_view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        productDetailViewModel = ViewModelProviders.of(this).get(ProductDetailViewModel.class);
        getViewModel().setNavigator(this);
        mConnectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
        final Bundle bundle = this.getArguments();
        if (bundle != null) {
            getViewModel().setDefaultProduct(bundle.getString("strProductList"));
            mSubCategoryTitle = bundle.getString("strProductCategory");
            mDefaultProductResponse = bundle.getString("productResponse");
            mFetchFromJson = bundle.getBoolean("fetchFromJson");
        }

        mToastUtils = new ToastUtils(this);
        observableOn(new Consumer<Object>() {

            @Override
            public void accept(Object object) throws Exception {
                Activity activity = getActivity();
                if (activity != null) {
                    if (object instanceof ProductDetailFragment) {
                        onPermissionGranted();
                    } else if (object instanceof ConfirmColorSizeActivity) {
                        startLocationUpdates();
                    } else if (object instanceof ProductState) {
                        ProductState productState = (ProductState) object;
                        switch (productState.getState()) {
                            case POST_ADD_ITEM_TO_CART:
                                String productId = getViewModel().getProductId();
                                //Parse skuId to catalogRefId if productType is of type CLOTHING_PRODUCT
                                mApiAddItemToCart = new AddItemToCart(productId
                                        , getGlobalState().getSelectedSKUId().sku
                                        , productState.getQuantity());
                                ShoppingDeliveryLocation deliveryLocation = Utils.getPreferredDeliveryLocation();
                                if (deliveryLocation.suburb == null) {
                                    deliverySelectionIntent(activity);
                                } else {
                                    apiAddItemToCart();
                                }
                                break;

                            case SET_SUBURB:
                                onAddToCartLoadComplete();
                                deliverySelectionIntent(activity);
                                break;

                            case USE_MY_LOCATION:
                                apiIdentifyTokenValidation();
                                break;

                            case SET_SUBURB_API:
                                cartSummaryAPI();
                                break;

                            case CANCEL_DIALOG_TAPPED:
                                onAddToCartLoadComplete();
                                break;

                            case OPEN_ADD_TO_SHOPPING_LIST_VIEW:
                                openAddToListFragment(activity);
                                break;

                            case CLOSE_PDP_FROM_ADD_TO_LIST:
                                if (activity instanceof BottomNavigationActivity) {
                                    BottomNavigationActivity bottomNavigationActivity = (BottomNavigationActivity) activity;
                                    showToastMessage(activity, productState, bottomNavigationActivity);
                                }

                                if (activity instanceof CartActivity) {
                                    showCartToastMessage(activity, productState);
                                }
                                break;

                            default:
                                break;
                        }
                    }
                }
            }
        });
    }

    private void showToastMessage(Activity activity, ProductState productState, BottomNavigationActivity bottomNavigationActivity) {
        switch (bottomNavigationActivity.getCurrentSection()) {
            case R.id.navigate_to_account:
                closeSlideUpPanel();
                getBottomNavigator().closeSlideUpPanelFromList(productState.getCount());
                break;
            default:
                mToastUtils.setActivity(activity);
                mToastUtils.setCurrentState(TAG);
                String shoppingList = getString(R.string.shopping_list);
                mNumberOfListSelected = productState.getCount();
                // shopping list vs shopping lists
                mToastUtils.setCartText((mNumberOfListSelected > 1) ? shoppingList + "s" : shoppingList);
                mToastUtils.setPixel(getViewDataBinding().llStoreFinder.getHeight() * 2);
                mToastUtils.setView(getViewDataBinding().llStoreFinder);
                mToastUtils.setMessage(R.string.added_to);
                mToastUtils.setViewState(true);
                mToastUtils.build();
                break;
        }
    }

    private void showCartToastMessage(Activity activity, ProductState productState) {
        mToastUtils.setActivity(activity);
        mToastUtils.setCurrentState(TAG);
        String shoppingList = getString(R.string.shopping_list);
        mNumberOfListSelected = productState.getCount();
        // shopping list vs shopping lists
        mToastUtils.setCartText((mNumberOfListSelected > 1) ? shoppingList + "s" : shoppingList);
        mToastUtils.setPixel(getViewDataBinding().llStoreFinder.getHeight() * 2);
        mToastUtils.setView(getViewDataBinding().llStoreFinder);
        mToastUtils.setMessage(R.string.added_to);
        mToastUtils.setViewState(true);
        mToastUtils.build();
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
        NavigateToShoppingList.Companion.openShoppingList(activity, addToListRequests, "", false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFuseLocationAPISingleton = FuseLocationAPISingleton.INSTANCE;
        getGlobalState().setColorWasPopup(false);
        getGlobalState().setColorPickerSku(null);
        getGlobalState().setSizeWasPopup(false);
        getGlobalState().setSizePickerSku(null);
        renderView();
    }

    @Override
    public void renderView() {
        slideBottomPanel();
        nestedScrollViewHelper();
        defaultProduct();
        setUpImageViewPager();
        getViewDataBinding().imClose.setOnClickListener(this);
        getViewDataBinding().imClose.bringToFront();
        getViewDataBinding().llAddToCart.setOnClickListener(this);
        getViewDataBinding().incProductColor.linSize.setOnClickListener(this);
        getViewDataBinding().incProductColor.linColour.setOnClickListener(this);
        getViewDataBinding().llColorSize.relColorSelector.setOnClickListener(this);
        getViewDataBinding().llColorSize.relSizeSelector.setOnClickListener(this);
        getViewDataBinding().llStoreFinder.setOnClickListener(this);

        mErrorHandlerView = new ErrorHandlerView(getActivity());
    }

    @Override
    public void closeSlideUpPanel() {
        if (getActivity() instanceof CartActivity) {
            ((CartActivity) getActivity()).closeSlideUpPanel();
        }

        if (getBottomNavigator() != null)
            getBottomNavigator().closeSlideUpPanel();
    }

    @Override
    public void nestedScrollViewHelper() {
        if (getBottomNavigator() != null)
            getBottomNavigator().scrollableViewHelper(getViewDataBinding().scrollProductDetail);
    }

    @Override
    public void setUpImageViewPager() {
        ViewPager mImageViewPager = getViewDataBinding().mProductDetailPager;
        ProductViewPagerAdapter mProductViewPagerAdapter = new ProductViewPagerAdapter(getActivity(), getAuxiliaryImage(), this);
        mImageViewPager.setAdapter(mProductViewPagerAdapter);
        mProductViewPagerAdapter.notifyDataSetChanged();
        mImageViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (ImageView anIvArrayDotsPager : ivArrayDotsPager) {
                    anIvArrayDotsPager.setImageResource(R.drawable.unselected_drawable);
                }
                ivArrayDotsPager[position].setImageResource(R.drawable.selected_drawable);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void defaultProduct() {
        final ProductList mDefaultProduct = getViewModel().getDefaultProduct();
        String externalImageRef = getImageByWidth(mDefaultProduct.externalImageRef, getActivity());
        mAuxiliaryImage.add(externalImageRef);
        getViewModel().setAuxiliaryImage(mAuxiliaryImage);

        //set product name
        setProductName();

        //set sub category title
        setSubCategoryTitle(mSubCategoryTitle);

        try {
            // set price list
            ProductUtils.displayPrice(getViewDataBinding().textPrice,
                    getViewDataBinding().textActualPrice, String.valueOf(mDefaultProduct.fromPrice),
                    getViewModel().maxWasPrice(mDefaultProduct.otherSkus));
        } catch (Exception ignored) {
        }

        //set promotional Images
        PromotionImages promotionalImage = mDefaultProduct.promotionImages;
        if (promotionalImage != null) {
            ProductUtils.showPromotionalImages(getViewDataBinding().imSave,
                    getViewDataBinding().imReward, getViewDataBinding().imVitality,
                    getViewDataBinding().imVitality, promotionalImage);
        }

        //set promotional text
        setText(mDefaultProduct.saveText, getViewDataBinding().tvSaveText);

        getBtnAddShoppingList().setOnClickListener(this);

        if (mFetchFromJson) { // display product through json string
            getViewModel().setProduct(mDefaultProductResponse);
            //onSuccessResponse(Utils.stringToJson(getActivity(), mDefaultProductResponse)); =================
            onLoadComplete();
        } else {
            getViewModel().productDetail(new ProductRequest(mDefaultProduct.productId, mDefaultProduct.sku));
        }
    }

    public WButton getBtnAddShoppingList() {
        return getViewDataBinding().btnAddShoppingList;
    }

    private void setSubCategoryTitle(String mSubCategoryTitle) {
        try {
            setText(getViewDataBinding().tvSubCategoryTitle, mSubCategoryTitle);
        } catch (NullPointerException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public List<String> getAuxiliaryImage() {
        return getViewModel().getAuxiliaryImage();
    }

    @Override
    public void onSuccessResponse(final WProduct product) {
        WProductDetail newProductList = product.product;
        enableFindInStoreButton(newProductList);

        if (mFetchFromJson) {
            getViewModel().setProduct(product.product);
        }
        List<OtherSkus> otherSkuList = getViewModel().otherSkuList();

        //display ingredient info
        getViewModel().displayIngredient();

        setProductCode(getViewModel().getProductId());

        setSubCategoryTitle(newProductList.categoryName);

        setProductDescription(getViewModel().getProductDescription(getActivity()));

        // use highest sku as default price
        OtherSkus highestPriceSku = getViewModel().highestSKUPrice(newProductList.fromPrice);

        setSelectedSize(highestPriceSku);

        setSelectedTextColor(highestPriceSku);
        setPrice(highestPriceSku);

        colorSizeContainerVisibility(otherSkuList);

        //set promotional Images
        ProductUtils.showPromotionalImages(getViewDataBinding().imSave, getViewDataBinding().imReward, getViewDataBinding().imVitality, getViewDataBinding().imVitality, product.product.promotionImages);

        try {
            setAuxiliaryImages(getViewModel().getAuxiliaryImageList(highestPriceSku));
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        try {
            setColorList(getViewModel().commonColorList(highestPriceSku));
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        try {
            setSizeList(getViewModel().commonSizeList(highestPriceSku));
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onFailureResponse(String s) {
        onAddToCartLoadComplete();
        mErrorHandlerView.showToast();
    }

    @Override
    public void disableStoreFinder() {
        setLayoutWeight(getViewDataBinding().llAddToCart, 1.0f);
        hideView(getViewDataBinding().llStoreFinder);
    }

    @Override
    public void responseFailureHandler(Response response) {
        onLoadComplete();
        enableAddToListBtn(false);
        hideView(getViewDataBinding().productCode);
        hideView(getViewDataBinding().llLoadingColorSize);
        Activity activity = getActivity();
        if (activity == null) return;
        if (response.desc == null) return;
        Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
    }

    @Override
    public void SelectedImage(String image) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent openMultipleImage = new Intent(getActivity(), MultipleImageActivity.class);
            openMultipleImage.putExtra("auxiliaryImages", image);
            startActivity(openMultipleImage);
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    @Override
    public void setProductName() {
        try {
            setText(getViewDataBinding().tvProductName, getViewModel().getDefaultProduct().productName);
        } catch (NullPointerException ex) {
            Log.i(TAG, ex.toString());
        }
    }

    @Override
    public void onLoadStart() {
        disableStoreFinder();

        enableAddToListBtn(false);
        //disable shop online button
        getViewDataBinding().llAddToCart.setAlpha(0.55f);
        getViewDataBinding().llAddToCart.setEnabled(false);

        // hide color and size view
        hideView(getViewDataBinding().incColorSize);
        hideView(getViewDataBinding().incProductColorBottomLine);

        // hide ingredient
        hideView(getViewDataBinding().ingredientLine);
        hideView(getViewDataBinding().linIngredient);

        // load product info
        setText(getViewDataBinding().productCode, getString(R.string.loading_product_info));
    }

    private void enableAddToListBtn(boolean enable) {
        getBtnAddShoppingList().setEnabled(enable);
    }

    @Override
    public void onLoadComplete() {
        enableAddToListBtn(true);
        showView(getViewDataBinding().llAddToCart);
        hideView(getViewDataBinding().productLoadDot);
    }

    @Override
    public void addToShoppingList() {
        Activity activity = getActivity();
        if (activity != null) {
            if (!SessionUtilities.getInstance().isUserAuthenticated()) {
                ScreenManager.presentSSOSignin(activity);
                return;
            }
            onPermissionGranted();
        }
    }

    @Override
    public String getImageByWidth(String imageUrl, Context context) {
        WindowManager display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        assert display != null;
        if (imageUrl == null) return "";
        Display deviceHeight = display.getDefaultDisplay();
        Point size = new Point();
        deviceHeight.getSize(size);
        int width = size.x;
        imageUrl = (imageUrl.contains("jpg")) ? Utils.getExternalImageRef() + imageUrl : imageUrl;
        return imageUrl + "" + ((imageUrl.contains("jpg")) ? "" : "?w=" + width + "&q=" + 85);
    }

    @Override
    public void onClick(View view) {
        MultiClickPreventer.preventMultiClick(view);
        Activity activity = getActivity();
        if (activity != null) {
            switch (view.getId()) {
                case R.id.imClose:
                    closeSlideUpPanel();
                    break;
                case R.id.btnAddShoppingList:
                    getGlobalState().saveButtonClicked(INDEX_ADD_TO_SHOPPING_LIST);
                    smoothScrollToTop();
                    WProductDetail product = getViewModel().getProduct();
                    // called when shopping list call has time out or user not authenticated
                    if (!SessionUtilities.getInstance().isUserAuthenticated()) {
                        ScreenManager.presentSSOSignin(activity);
                        return;
                    }
                    //activates when product detail and shopping list loading incomplete
                    if (product == null) return;
                    switch (getViewModel().getProductType()) {
                        case CLOTHING_PRODUCT:
                            addToShoppingList();
                            break;

                        case FOOD_PRODUCT:
                            if (product == null) return;
                            getGlobalState().setSelectedSKUId(createOtherSkus(TextUtils.isEmpty(product.sku) ? product.productId : product.sku));
                            openAddToListFragment(activity);
                            break;

                        default:
                            if (getViewModel().otherSkuList().size() > 1) addToShoppingList();
                            break;
                    }
                    //One time biometricsWalkthrough
                    ScreenManager.presentBiometricWalkthrough(getActivity());
                    break;

                case R.id.relColorSelector:
                    getGlobalState().saveButtonClicked(DEFAULT_PICKER);
                    if (!mSizeSkuList.isEmpty()) {
                        colorSizePicker(mSizeSkuList, true, false);
                    }
                    break;

                case R.id.relSizeSelector:
                    getGlobalState().saveButtonClicked(DEFAULT_PICKER);
                    colorSizePicker(mSkuColorList, false, true);
                    break;

                case R.id.llStoreFinder:
                    getGlobalState().saveButtonClicked(INDEX_STORE_FINDER);
                    smoothScrollToTop();
                    if (Utils.isLocationEnabled(getActivity())) {
                        if (activity instanceof BottomNavigationActivity) {
                            BottomNavigator bottomNavigator = getBottomNavigator();
                            checkLocationPermission(bottomNavigator, bottomNavigator.getPermissionType(android.Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION_REQUEST_CODE);
                        } else {
                            CartActivity cartActivity = (CartActivity) activity;
                            checkLocationPermission(cartActivity, cartActivity.getPermissionType(android.Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION_REQUEST_CODE);
                        }
                    } else {
                        Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.LOCATION_OFF, "");
                    }
                    break;

                case R.id.llAddToCart:
                    if (isNetworkConnected()) {
                        getGlobalState().saveButtonClicked(INDEX_ADD_TO_CART);
                        apiIdentifyTokenValidation();
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private void smoothScrollToTop() {
        /**
         * Combination of fling and fullScroll
         * create a smooth animation during scrolling to top
         */
        getViewDataBinding().scrollProductDetail.fling(0);  // Sets mLastScrollerY for next command
        getViewDataBinding().scrollProductDetail.fullScroll(ScrollView.FOCUS_UP);  // Starts a scroll itself
    }

    @Override
    public void setLayoutWeight(View v, float weight) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = weight;
        params.setMarginStart((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        params.setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        v.setLayoutParams(params);
    }

    @Override
    public void setIngredients(String ingredient) {
        setText(getViewDataBinding().ingredientList, ingredient);
        if (isEmpty(ingredient)) {
            hideView(getViewDataBinding().linIngredient);
            hideView(getViewDataBinding().ingredientLine);
        } else {
            showView(getViewDataBinding().linIngredient);
            showView(getViewDataBinding().ingredientLine);
        }
    }

    @Override
    public void setProductCode(String productCode) {
        if (getViewDataBinding().productCode != null) {
            try {
                setText(getViewDataBinding().productCode, getString(R.string.product_code) + ": " + productCode);
            } catch (IllegalStateException ex) {
                Log.d("setProductCode", ex.getMessage());
            }
        }
    }

    @Override
    public void setProductDescription(String productDescription) {
        getViewDataBinding().webDescription.loadDataWithBaseURL("file:///android_res/drawable/",
                productDescription, "text/html; charset=UTF-8", "UTF-8", null);
    }

    @Override
    public void setSelectedSize(OtherSkus sku) {
        if (sku != null) {
            String size = sku.size;
            getGlobalState().setSizePickerSku(sku);
            WTextView tvTextSize = getViewDataBinding().incProductColor.textProductSize;
            setText(tvTextSize, size);
            tvTextSize.setTextColor(Color.BLACK);
        }
    }

    @Override
    public void setPrice(OtherSkus otherSkus) {
        if (otherSkus != null) {
            String wasPrice = otherSkus.wasPrice;
            if (isEmpty(wasPrice)) {
                wasPrice = "";
            }
            String price = otherSkus.price;
            WTextView tvPrice = getViewDataBinding().textPrice;
            WTextView tvWasPrice = getViewDataBinding().textActualPrice;
            switch (getViewModel().getProductType()) {
                case CLOTHING_PRODUCT:
                    if (TextUtils.isEmpty(wasPrice)) {
                        setText(tvPrice, WFormatter.formatAmount(price));
                        tvPrice.setPaintFlags(0);
                        tvWasPrice.setText("");
                    } else {
                        if (wasPrice.equalsIgnoreCase(price)) {
                            //wasPrice equals currentPrice
                            if (!isEmpty(price)) {
                                setText(tvPrice, WFormatter.formatAmount(price));
                            }
                            setText(tvWasPrice, "");
                            tvPrice.setPaintFlags(0);
                        } else {
                            setText(tvPrice, WFormatter.formatAmount(wasPrice));
                            tvPrice.setPaintFlags(tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            setText(tvWasPrice, WFormatter.formatAmount(price));
                        }
                    }
                    break;

                default:
                    if (TextUtils.isEmpty(wasPrice)) {
                        if (Utils.isLocationEnabled(tvPrice.getContext())) {
                            ArrayList<Double> priceList = new ArrayList<>();
                            for (OtherSkus os : getViewModel().otherSkuList()) {
                                if (!TextUtils.isEmpty(os.price)) {
                                    priceList.add(Double.valueOf(os.price));
                                    priceList.add(Double.valueOf(os.price));
                                }
                            }
                            if (priceList.size() > 0) {
                                price = String.valueOf(Collections.max(priceList));
                            }
                        }
                        tvPrice.setText(WFormatter.formatAmount(price));
                        tvPrice.setPaintFlags(0);
                        tvWasPrice.setText("");
                    } else {
                        if (Utils.isLocationEnabled(tvPrice.getContext())) {
                            ArrayList<Double> priceList = new ArrayList<>();
                            for (OtherSkus os : getViewModel().otherSkuList()) {
                                if (!TextUtils.isEmpty(os.price)) {
                                    priceList.add(Double.valueOf(os.price));
                                }
                            }
                            if (priceList.size() > 0) {
                                price = String.valueOf(Collections.max(priceList));
                            }
                        }

                        if (wasPrice.equalsIgnoreCase(price)) { //wasPrice equals currentPrice
                            tvPrice.setText(WFormatter.formatAmount(price));
                            tvWasPrice.setText("");
                        } else {
                            tvPrice.setText(WFormatter.formatAmount(wasPrice));
                            tvPrice.setPaintFlags(tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            tvWasPrice.setText(WFormatter.formatAmount(price));
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void setAuxiliaryImages(ArrayList<String> auxiliaryImages) {
        if (!auxiliaryImages.isEmpty()) {
            mAuxiliaryImage = auxiliaryImages;
            getViewModel().setAuxiliaryImage(mAuxiliaryImage);
            setUpImageViewPager();
            setupPagerIndicatorDots(auxiliaryImages.size());
        }
    }

    @Override
    public void setSelectedTextColor(OtherSkus otherSkus) {
        if (otherSkus == null) return;
        if (otherSkus.externalColourRef == null) return;
        selectedColor(otherSkus.externalColourRef);

        /***
         * set size textField to "NO SZ"
         * and disable click event when otherSku size field contains NO SZ
         */
        if (!TextUtils.isEmpty(otherSkus.size)) {
            String size = otherSkus.size;
            boolean noSizeFound = size.equalsIgnoreCase("NO SZ");
            if (noSizeFound) {
                getViewDataBinding().llColorSize.tvSelectedSizeValue.setText(size.toUpperCase());
                getViewDataBinding().llColorSize.tvSelectedSizeValue.setTextColor(getResources().getColor(R.color.black));
                getViewDataBinding().llColorSize.relSizeSelector.setAlpha(0.3f);
            }
            getViewDataBinding().llColorSize.relSizeSelector.setEnabled(!noSizeFound);
        }

        /***
         * disable click event when otherSku color field is empty
         */
        if (!TextUtils.isEmpty(otherSkus.colour)) {
            String colour = otherSkus.colour;
            boolean noColourFound = colour.equalsIgnoreCase("N/A");
            if (noColourFound) {
                getViewDataBinding().llColorSize.relColorSelector.setAlpha(0.3f);
            }
            getViewDataBinding().llColorSize.relColorSelector.setEnabled(!noColourFound);
        }
    }

    @Override
    public void removeAllDots() {
        getViewDataBinding().pagerDots.removeAllViews();
    }

    @Override
    public void enableFindInStoreButton(WProductDetail productList) {
        if (productList != null) {
            try {
                llStoreFinder = getViewDataBinding().llStoreFinder;
                LinearLayout llAddToCart = getViewDataBinding().llAddToCart;
                /***
                 * isnAvailable:true show find in store button
                 * isnAvailable:false do not show find in store button
                 */
                if (productList.isnAvailable) {
                    llAddToCart.setAlpha(1f);
                    llAddToCart.setEnabled(true);
                    setLayoutWeight(llAddToCart, 0.5f);
                    setLayoutWeight(llStoreFinder, 0.5f);
                    showView(llStoreFinder);
                } else {
                    llAddToCart.setAlpha(1.0f);
                    llAddToCart.setEnabled(true);
                    setLayoutWeight(llAddToCart, 1.0f);
                    hideView(llStoreFinder);
                }
            } catch (IllegalStateException ex) {
                Log.d(TAG, ex.toString());
            }
        }
    }

    private void selectedColor(String url) {
        WrapContentDraweeView mImSelectedColor = getViewDataBinding().llColorSize.imSelectedColor;
        mImSelectedColor.setImageAlpha(TextUtils.isEmpty(url) ? 0 : 255);
        DrawImage drawImage = new DrawImage(getActivity());
        drawImage.displayImage(mImSelectedColor, url);
    }

    @Override
    public void setupPagerIndicatorDots(int size) {
        removeAllDots();
        if (size > 1) {
            ivArrayDotsPager = new ImageView[size];
            for (int i = 0; i < ivArrayDotsPager.length; i++) {
                ivArrayDotsPager[i] = new ImageView(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(16, 0, 16, 0);
                ivArrayDotsPager[i].setLayoutParams(params);
                ivArrayDotsPager[i].setImageResource(R.drawable.unselected_drawable);
                ivArrayDotsPager[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setAlpha(1);
                    }
                });
                getViewDataBinding().pagerDots.addView(ivArrayDotsPager[i]);
                getViewDataBinding().pagerDots.bringToFront();
            }
            ivArrayDotsPager[0].setImageResource(R.drawable.selected_drawable);
        }
    }

    @Override
    public void colorSizeContainerVisibility(List<OtherSkus> otherSkuList) {

        // ProductDetails item has no colour
        if (!productHasColour()) {
            hideView(getViewDataBinding().llColorSize.relColorSelector);
            getViewDataBinding().llColorSize.relSizeSelector.setPadding(Utils.dp2px(getActivity(), 16), 0, 0, 0);

            hideView(getViewDataBinding().llColorSize.vwSeparator);
        }

        // ProductDetails item has no size
        if (!productHasSize()) {
            hideView(getViewDataBinding().llColorSize.relSizeSelector);
            getViewDataBinding().llColorSize.relColorSelector.setPadding(Utils.dp2px(getActivity(), 16), 0, 0, 0);
            hideView(getViewDataBinding().llColorSize.vwSeparator);
        }

        if (otherSkuList.size() > 0 && getViewModel().getProductType().equalsIgnoreCase(CLOTHING_PRODUCT)) {
            showView(getViewDataBinding().incColorSize);
            hideView(getViewDataBinding().llLoadingColorSize);
        } else {
            hideView(getViewDataBinding().incColorSize);
            hideView(getViewDataBinding().llLoadingColorSize);
        }
    }

    @Override
    public void setColorList(List<OtherSkus> skuList) {
        mSizeSkuList = (ArrayList<OtherSkus>) skuList;
    }

    @Override
    public void setSizeList(List<OtherSkus> skuList) {
        mSkuColorList = (ArrayList<OtherSkus>) skuList;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cancelBackgroundTask();
    }

    private void cancelBackgroundTask() {
        getGlobalState().setColorWasPopup(false);
        getGlobalState().setColorPickerSku(null);
        getGlobalState().setSizeWasPopup(false);
        getGlobalState().setSizePickerSku(null);
        cancelRequest(mLocationItemTask);
        cancelRequest(mPostAddItemToCart);
        cancelRequest(mGetCartSummary);
        if (mInventoryForStore != null)
            mInventoryForStore.cancelInventoryForStoreCall();
    }

    private void executeLocationItemTask() {
        mLocationItemTask = getViewModel().locationItemTask(getActivity());
    }

    @Override
    public void startLocationUpdates() {
        showFindInStoreProgress();
        mFuseLocationAPISingleton.addLocationChangeListener(this);
        mFuseLocationAPISingleton.startLocationUpdate();
    }

    @Override
    public void stopLocationUpdate() {
        if (mFuseLocationAPISingleton != null)
            mFuseLocationAPISingleton.stopLocationUpdate();
    }

    @Override
    public void showFindInStoreProgress() {
        getViewDataBinding().llStoreFinder.setEnabled(false);
        hideView(getViewDataBinding().tvBtnFinder);
        showView(getViewDataBinding().mButtonProgress);
    }

    @Override
    public void dismissFindInStoreProgress() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getViewDataBinding().llStoreFinder.setEnabled(true);
                showView(getViewDataBinding().tvBtnFinder);
                hideView(getViewDataBinding().mButtonProgress);
            }
        });
    }

    @Override
    public void onLocationItemSuccess(List<StoreDetails> location) {
        Utils.removeObjectFromArrayList(getActivity(), location);
        if (location.size() > 0) {
            getGlobalState().setStoreDetailsArrayList(location);
            Intent intentInStoreFinder = new Intent(getActivity(), WStockFinderActivity.class);
            intentInStoreFinder.putExtra("PRODUCT_NAME", mSubCategoryTitle);
            startActivity(intentInStoreFinder);
            getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
        } else {
            outOfStockDialog();
        }
    }

    @Override
    public void outOfStockDialog() {
        //no stock error message
        Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.NO_STOCK, "");
    }

    @Override
    public void onPermissionGranted() {
        smoothScrollToTop();
        if (!isNetworkConnected()) return;
        if (selectedFindInStoreOtherSkus != null) {
            noSizeColorIntent();
            setFinInStoreOtherSkus(null);
            return;
        }
        mProductHasColour = productHasColour();
        mProductHasSize = productHasSize();
        mProductHasOneColour = productHasOneColour();
        mProductHasOneSize = productHasOneSize();

        boolean colorWasPopUp = getGlobalState().colorWasPopup();
        boolean sizeWasPopUp = getGlobalState().sizeWasPopup();

        OtherSkus popupColorSKu = getGlobalState().getColorPickerSku();
        OtherSkus popupSizeSKu = getGlobalState().getSizePickerSku();

			/*
			color | size
			0 | 0 - > none selected
			0 | 1 - > size was selected
			1 | 0 - > color was selected
			1 | 1 - color and size were selected
			*/

        if (!colorWasPopUp && !sizeWasPopUp) {
            sizeColorSelector();
        } else if (!colorWasPopUp && sizeWasPopUp) {
            displayColor(popupSizeSKu);
        } else if (colorWasPopUp && !sizeWasPopUp) {
            sizeOnlyIntent(popupColorSKu);
        } else {
            switch (getGlobalState().getLatestSelectedPicker()) {
                case 1:
                    mSkuId = getGlobalState().getColorPickerSku();
                    break;
                case 2:
                    mSkuId = getGlobalState().getSizePickerSku();
                    break;
                default:
                    break;
            }
            noSizeColorIntent();
        }

    }

    private void setFinInStoreOtherSkus(OtherSkus otherSkus) {
        selectedFindInStoreOtherSkus = otherSkus;
    }

    private boolean productHasColour() {
        return getColorList().size() > 0;
    }

    private boolean productHasOneColour() {
        return getColorList().size() == 1;
    }

    private boolean productHasOneSize() {
        return getSizeList().size() == 1;
    }

    private boolean productHasSize() {
        return getSizeList().size() > 0;
    }

    private ArrayList<OtherSkus> getColorList() {
        Collections.sort(getViewModel().otherSkuList(), new Comparator<OtherSkus>() {
            @Override
            public int compare(OtherSkus lhs, OtherSkus rhs) {
                return lhs.colour.compareToIgnoreCase(rhs.colour);
            }
        });

        ArrayList<OtherSkus> commonColorSku = new ArrayList<>();
        for (OtherSkus sku : getViewModel().otherSkuList()) {
            if (!colourValueExist(commonColorSku, sku.colour)) {
                commonColorSku.add(sku);
            }
        }
        return commonColorSku;
    }

    public boolean colourValueExist(ArrayList<OtherSkus> list, String name) {
        for (OtherSkus item : list) {
            if (item.colour.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<OtherSkus> getSizeList() {
        Collections.sort(getViewModel().otherSkuList(), new Comparator<OtherSkus>() {
            @Override
            public int compare(OtherSkus lhs, OtherSkus rhs) {
                return lhs.size.compareToIgnoreCase(rhs.size);
            }
        });

        ArrayList<OtherSkus> commonColorSku = new ArrayList<>();
        for (OtherSkus sku : getViewModel().otherSkuList()) {
            if (!colourValueExist(commonColorSku, sku.size)) {
                commonColorSku.add(sku);
            }
        }
        return commonColorSku;
    }

    private void sizeColorSelector() {
        try {
            if (mProductHasColour) {
                if (mProductHasOneColour) {
                    // one colour only
                    String skuColour = getColorList().get(0).colour;
                    ArrayList<OtherSkus> getSize;
                    if (!TextUtils.isEmpty(skuColour)) {
                        getSize = getViewModel().commonSizeList(mProductHasColour, skuColour);
                    } else {
                        getSize = getSizeList();
                    }
                    if (getSize.size() > 0) {
                        if (getSize.size() == 1) {
                            mSkuId = getSize.get(0);
                            noSizeColorIntent();
                        } else {
                            sizeIntent(skuColour);
                        }
                    } else {
                        mSkuId = createOtherSkus(getViewModel().getDefaultProduct().sku);
                        noSizeColorIntent();
                    }
                } else {
                    // contain several colours
                    colourIntent();
                }
            } else {
                if (mProductHasSize) {
                    if (mProductHasOneSize) { //one size
                        ArrayList<OtherSkus> getSize = getSizeList();
                        mSkuId = getSize.get(0);
                        noSizeColorIntent();
                    } else { // more sizes
                        sizeIntent();
                    }
                } else {
                    mSkuId = createOtherSkus(getViewModel().getDefaultProduct().sku);
                    noSizeColorIntent();
                }
            }
        } catch (Exception ignored) {
        }
    }

    @NonNull
    private OtherSkus createOtherSkus(String skuId) {
        OtherSkus otherSkus = new OtherSkus();
        otherSkus.sku = skuId;
        return otherSkus;
    }

    public void noSizeColorIntent() {
        smoothScrollToTop();
        getGlobalState().setSelectedSKUId(mSkuId);
        Activity activity = getActivity();
        if (activity != null) {
            switch (getGlobalState().getSaveButtonClick()) {
                case INDEX_STORE_FINDER:
                    startLocationUpdates();
                    break;

                case INDEX_ADD_TO_SHOPPING_LIST:
                    openAddToListFragment(activity);
                    break;
                default:
                    /**
                     * Make add to cart call for food type item
                     */
                    if (getViewModel().getProductType().equalsIgnoreCase(FOOD_PRODUCT)
                            || (getViewModel().otherSkuList().size() == 0)) {
                        sendBus(new ProductState(POST_ADD_ITEM_TO_CART, 1));
                        return;
                    }

                    String productId = getViewModel().getProductId();
                    String catalogRefId = productId;
                    //Parse skuId to catalogRefId if productType is of type CLOTHING_PRODUCT
                    if (getViewModel().getProductType().equalsIgnoreCase(CLOTHING_PRODUCT)) {
                        catalogRefId = getGlobalState().getSelectedSKUId().sku;
                    }
                    int quantity = 1;
                    mApiAddItemToCart = new AddItemToCart(productId, catalogRefId, quantity);
                    apiAddItemToCart();
                    break;
            }
        }
    }

    public void colorSizePicker(ArrayList<OtherSkus> otherSkusList, boolean colorIsSelected, boolean sizeIsSelected) {
        getGlobalState().setColourSKUArrayList(otherSkusList);
        Intent mIntent = new Intent(getActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("COLOR_LIST", toJson(otherSkusList));
        mIntent.putExtra("OTHERSKU", toJson(getViewModel().otherSkuList()));
        mIntent.putExtra(ConfirmColorSizeActivity.COLOR_PICKER_SELECTOR, colorIsSelected);
        mIntent.putExtra(ConfirmColorSizeActivity.SIZE_PICKER_SELECTOR, sizeIsSelected);
        mIntent.putExtra(ConfirmColorSizeActivity.FULFILLMENT_TYPE, getFulFillmentType());
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra("PRODUCT_NAME", getViewModel().getDefaultProduct().productName);
        getActivity().startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
        getActivity().overridePendingTransition(0, 0);
    }

    public void colorSizePicker(ArrayList<OtherSkus> otherSkusList,
                                boolean colorIsSelected, boolean sizeIsSelected, OtherSkus selectedSku) {
        getGlobalState().setColourSKUArrayList(otherSkusList);
        Intent mIntent = new Intent(getActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("COLOR_LIST", toJson(otherSkusList));
        mIntent.putExtra("OTHERSKU", toJson(getViewModel().otherSkuList()));
        mIntent.putExtra(ConfirmColorSizeActivity.COLOR_PICKER_SELECTOR, colorIsSelected);
        mIntent.putExtra(ConfirmColorSizeActivity.SIZE_PICKER_SELECTOR, sizeIsSelected);
        mIntent.putExtra(ConfirmColorSizeActivity.FULFILLMENT_TYPE, getFulFillmentType());
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra(ConfirmColorSizeActivity.SELECTED_SKU, selectedSku.sku);
        mIntent.putExtra("PRODUCT_NAME", getViewModel().getDefaultProduct().productName);
        getActivity().startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
        getActivity().overridePendingTransition(0, 0);
    }


    public void colorSizePicker(ArrayList<OtherSkus> otherSkusList,
                                boolean colorIsSelected, boolean sizeIsSelected, OtherSkus selectedSku, boolean enableInventoryCall) {
        getGlobalState().setColourSKUArrayList(otherSkusList);
        Intent mIntent = new Intent(getActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("COLOR_LIST", toJson(otherSkusList));
        mIntent.putExtra("OTHERSKU", toJson(getViewModel().otherSkuList()));
        mIntent.putExtra(ConfirmColorSizeActivity.COLOR_PICKER_SELECTOR, colorIsSelected);
        mIntent.putExtra(ConfirmColorSizeActivity.SIZE_PICKER_SELECTOR, sizeIsSelected);
        mIntent.putExtra(ConfirmColorSizeActivity.FULFILLMENT_TYPE, getFulFillmentType());
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra(ConfirmColorSizeActivity.SELECTED_SKU, selectedSku.sku);
        mIntent.putExtra("PRODUCT_NAME", getViewModel().getDefaultProduct().productName);
        mIntent.putExtra("MAKE_INVENTORY_CALL", enableInventoryCall);
        getActivity().startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
        getActivity().overridePendingTransition(0, 0);
    }


    public void sizeIntent() {
        getGlobalState().setColourSKUArrayList(getColorList());
        Intent mIntent = new Intent(getActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("COLOR_LIST", toJson(getColorList()));
        mIntent.putExtra("OTHERSKU", toJson(getViewModel().otherSkuList()));
        mIntent.putExtra("PRODUCT_HAS_COLOR", false);
        mIntent.putExtra("PRODUCT_HAS_SIZE", true);
        mIntent.putExtra(ConfirmColorSizeActivity.FULFILLMENT_TYPE, getFulFillmentType());
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra("PRODUCT_NAME", getViewModel().getDefaultProduct().productName);
        getActivity().startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
        getActivity().overridePendingTransition(0, 0);
    }

    public void sizeIntent(String colour) {
        getGlobalState().setColourSKUArrayList(getColorList());
        Intent mIntent = new Intent(getActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("SELECTED_COLOUR", colour);
        mIntent.putExtra("OTHERSKU", toJson(getViewModel().otherSkuList()));
        mIntent.putExtra("PRODUCT_HAS_COLOR", false);
        mIntent.putExtra("PRODUCT_HAS_SIZE", true);
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra(ConfirmColorSizeActivity.FULFILLMENT_TYPE, getFulFillmentType());
        mIntent.putExtra("PRODUCT_NAME", getViewModel().getDefaultProduct().productName);
        getActivity().startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
        getActivity().overridePendingTransition(0, 0);
    }

    public void colourIntent() {
        getGlobalState().setColourSKUArrayList(getColorList());
        Intent mIntent = new Intent(getActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("COLOR_LIST", toJson(getColorList()));
        mIntent.putExtra("OTHERSKU", toJson(getViewModel().otherSkuList()));
        mIntent.putExtra("PRODUCT_HAS_COLOR", true);
        mIntent.putExtra("PRODUCT_HAS_SIZE", true);
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra(ConfirmColorSizeActivity.FULFILLMENT_TYPE, getFulFillmentType());
        mIntent.putExtra("PRODUCT_NAME", getViewModel().getDefaultProduct().productName);
        getActivity().startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
        getActivity().overridePendingTransition(0, 0);
    }

    public void colorIntent(String size) {
        Intent mIntent = new Intent(getActivity(), ConfirmColorSizeActivity.class);
        mIntent.putExtra("SELECTED_COLOUR", size);
        mIntent.putExtra("COLOR_LIST", toJson(getGlobalState().getColourSKUArrayList()));
        mIntent.putExtra("OTHERSKU", toJson(getViewModel().otherSkuList()));
        mIntent.putExtra("PRODUCT_HAS_COLOR", true);
        mIntent.putExtra("PRODUCT_HAS_SIZE", false);
        mIntent.putExtra(ConfirmColorSizeActivity.FULFILLMENT_TYPE, getFulFillmentType());
        mIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, "");
        mIntent.putExtra("PRODUCT_NAME", getViewModel().getDefaultProduct().productName);
        getActivity().startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
        getActivity().overridePendingTransition(0, 0);
    }

    private void sizeOnlyIntent(OtherSkus otherSku) {
        ArrayList<OtherSkus> sizeList = getViewModel().commonSizeList(otherSku);
        int sizeListSize = sizeList.size();
        if (sizeListSize > 0) {
            if (sizeListSize == 1) {
                // one size only
                mSkuId = sizeList.get(0);
                noSizeColorIntent();
            } else {
                // size > 1
                sizeIntent(otherSku.colour);
            }
        } else {
            // no size
            mSkuId = otherSku;
            noSizeColorIntent();
        }
    }

    private String toJson(Object jsonObject) {
        return new Gson().toJson(jsonObject);
    }

    private void displayColor(OtherSkus otherSku) {
        ArrayList<OtherSkus> colorList = getViewModel().commonColorList(otherSku);
        if (colorList != null) {
            int colorListSize = colorList.size();
            if (colorListSize > 0) {
                if (colorListSize == 1) {
                    // one color only
                    mSkuId = colorList.get(0);
                    noSizeColorIntent();
                } else {
                    // color > 1
                    getGlobalState().setColourSKUArrayList(colorList);
                    colorIntent(otherSku.size);
                }
            } else {
                // no color
                mSkuId = otherSku;
                noSizeColorIntent();
            }
        } else {
            mSkuId = otherSku;
            noSizeColorIntent();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            //do when hidden
            hideToolbar();
            setToolbarBackgroundColor(R.color.white);
        }
    }

    /***
     * Auto-connect stuff
     */

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.unregisterReceiver(mConnectionBroadcast);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_DETAIL);
        registerReceiver();
    }

    private void registerReceiver() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.registerReceiver(mConnectionBroadcast,
                    new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }

    @Override
    public void onConnectionChanged() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean productLoadFail = getViewModel().productLoadFail();
                    boolean findInStoreLoadFail = getViewModel().findInStoreLoadFail();
                    boolean addedToCart = getViewModel().getAddToCart();
                    if (isNetworkConnected()) {
                        if (productLoadFail) {
                            ProductList defaultProduct = getViewModel().getDefaultProduct();
                            getViewModel().productDetail(new ProductRequest(defaultProduct.productId, defaultProduct.sku));
                            return;
                        }
                        if ((getGlobalState().getSaveButtonClick() == INDEX_STORE_FINDER) &&
                                findInStoreLoadFail) {
                            getViewModel().setFindInStoreLoadFail(false);
                            executeLocationItemTask();
                            return;
                        }

                        if ((getGlobalState().getSaveButtonClick() == INDEX_ADD_TO_CART) &&
                                !addedToCart) {
                            getViewDataBinding().llAddToCart.performClick();
                        }

                        /***
                         * Auto-connect on inventory connection failure
                         */
                        if (mInventoryForStore != null) {
                            if (mInventoryForStore.getOnConnectFailure()) {
                                getViewDataBinding().llAddToCart.performClick();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void apiIdentifyTokenValidation() {
        getViewModel().setAddedToCart(true);
        Activity activity = getActivity();
        if (activity != null) {
            //Check if the user has a sessionToken
            if (!SessionUtilities.getInstance().isUserAuthenticated()) {
                getGlobalState().setDetermineLocationPopUpEnabled(true);
                ScreenManager.presentSSOSignin(activity);
                onAddToCartLoadComplete();
            } else {
                // query the status of the JWT on STS using the the identityTokenValidation endpoint
                cartSummaryAPI();
            }
        }
    }

    private void cartSummaryAPI() {
        final Activity activity = getActivity();
        if (activity != null) {
            ShoppingDeliveryLocation shoppingDeliveryLocation = Utils.getPreferredDeliveryLocation();
            if (shoppingDeliveryLocation == null) {
                executeCartSummary();
                return;
            }
            if (shoppingDeliveryLocation.suburb != null) {
                if (!TextUtils.isEmpty(shoppingDeliveryLocation.suburb.name)) {
                    // show pop up message after login
                    if (activate_location_popup) {
                        smoothScrollToTop();
                        Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.DETERMINE_LOCATION_POPUP, DETERMINE_LOCATION_POPUP);
                        activate_location_popup = false;
                        //One time biometricsWalkthrough
                        ScreenManager.presentBiometricWalkthrough(getActivity());
                        return;
                    }

                    /***
                     * Determine whether to display colour size box
                     * if product type is of type clothing or otherSkuList size is greater than 1
                     * size > 1 product i.e. perfume productType of type food but sku can be > 1
                     * next step become colour/size process
                     * else run through food step
                     */
                    if (getViewModel().getProductType() != null) {
                        smoothScrollToTop();
                        if (getViewModel().getProductType().equalsIgnoreCase(CLOTHING_PRODUCT)) {
                            onAddToCartLoadComplete();
                            onPermissionGranted();
                        } else {
                            onAddToCartLoadComplete();
                            WProductDetail product = getViewModel().getProduct();
                            if (product == null) return;
                            if (product.otherSkus == null) return;
                            if (product.otherSkus.get(0) == null) return;
                            getGlobalState().setSelectedSKUId(product.otherSkus.get(0));
                            onAddToCartLoad();
                            mInventoryForStore = new InventoryForStore(product.fulfillmentType, product.sku) {
                                @Override
                                public void onInventoryForStoreSuccess(SkusInventoryForStoreResponse skusInventoryForStoreResponse) {
                                    if (skusInventoryForStoreResponse == null) return;
                                    switch (skusInventoryForStoreResponse.httpCode) {
                                        case 200:
                                            List<SkuInventory> skuInventoryList = skusInventoryForStoreResponse.skuInventory;
                                            // If no quantity is found, display the out of stock message
                                            if (skuInventoryList.size() == 0) {
                                                outOfStockDialog();
                                                return;
                                            }
                                            SkuInventory skuInventory = skuInventoryList.get(0);
                                            int quantity = skuInventory.quantity;
                                            if (quantity == 0) {
                                                outOfStockDialog();
                                            } else {
                                                // If quantity is found, perform default add to cart flow
                                                openQuantityPopup(quantity);
                                            }
                                            break;

                                        case 440:
                                            SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, skusInventoryForStoreResponse.response.stsParams, getActivity());
                                            break;

                                        default:
                                            if (skusInventoryForStoreResponse.response == null)
                                                return;
                                            if (skusInventoryForStoreResponse.response.desc == null)
                                                return;
                                            Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, skusInventoryForStoreResponse.response.desc);
                                            break;
                                    }
                                }

                                @Override
                                public void onInventoryForStoreFailure(String message) {
                                    onAddToCartLoadComplete();
                                }

                                @Override
                                public void onNoMatchFoundForStoreId() {
                                    onAddToCartLoadComplete();
                                    outOfStockDialog();
                                }
                            };
                        }
                    }
                } else {
                    //If the user does not have a suburb id & name stored, the set location from region and suburb process is followed
                    onAddToCartLoadComplete();
                    deliverySelectionIntent(activity);
                }
            }
        }
    }

    private void openQuantityPopup(int quantity) {
        Activity activity = getActivity();
        if (activity == null) return;
        Intent editQuantityIntent = new Intent(activity, ConfirmColorSizeActivity.class);
        editQuantityIntent.putExtra(ConfirmColorSizeActivity.SELECT_PAGE, ConfirmColorSizeActivity.QUANTITY);
        editQuantityIntent.putExtra("CART_QUANTITY_In_STOCK", quantity);
        activity.startActivity(editQuantityIntent);
        activity.overridePendingTransition(0, 0);
    }

    private void executeCartSummary() {
        Activity activity = getActivity();
        if (activity == null) {
            onAddToCartLoadComplete();
            return;
        }
        mGetCartSummary = getViewModel().getCartSummary();
    }

    @Override
    public void onTokenFailure(String e) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mErrorHandlerView.showToast();
                    onAddToCartLoadComplete();
                }
            });
        }
    }

    @Override
    public void onCartSummarySuccess(CartSummaryResponse cartSummaryResponse) {
        if (cartSummaryResponse.data != null) {
            List<CartSummary> cartSummaryList = cartSummaryResponse.data;
            if (cartSummaryList.get(0) != null) {
                CartSummary cartSummary = cartSummaryList.get(0);
                if (TextUtils.isEmpty(cartSummary.suburbId)) {
                    deliverySelectionIntent(getActivity());
                } else {
                    cartSummaryAPI();
                }
            }
        }
    }

    private void deliverySelectionIntent(Activity activity) {
        Intent deliveryLocationSelectionActivity = new Intent(activity, DeliveryLocationSelectionActivity.class);
        activity.startActivityForResult(deliveryLocationSelectionActivity, DELIVERY_LOCATION_FROM_PDP_REQUEST);
        activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);

    }

    @Override
    public void otherHttpCode(Response response) {
        Activity activity = getActivity();
        if (activity != null) {
            if (response.desc != null) {
                Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
            }
        }
    }

    @Override
    public void onAddToCartLoad() {
        hideView(getViewDataBinding().tvAddToCart);
        showView(getViewDataBinding().pbAddToCart);
    }

    @Override
    public void onAddToCartLoadComplete() {
        hideView(getViewDataBinding().pbAddToCart);
        showView(getViewDataBinding().tvAddToCart);
    }

    @Override
    public void apiAddItemToCart() {
        List<AddItemToCart> addItemToCarts = new ArrayList<>();
        addItemToCarts.add(mApiAddItemToCart);
        mPostAddItemToCart = getViewModel().postAddItemToCart(addItemToCarts);
    }

    @Override
    public void addItemToCartResponse(AddItemToCartResponse addItemToCartResponse) {
        Log.d(TAG, addItemToCartResponse.toString());
        if (getActivity() instanceof BottomNavigationActivity)
            ((BottomNavigationActivity) getActivity()).cartSummaryAPI();

        onAddToCartLoadComplete();
        List<AddToCartDaTum> addToCartList = addItemToCartResponse.data;
        if (addToCartList != null && addToCartList.size() > 0) {
            AddToCartDaTum datum = addToCartList.get(0);
            if (datum != null) {
                List<FormException> formExceptionList = datum.formexceptions;
                if (formExceptionList != null) {
                    FormException formException = formExceptionList.get(0);
                    if (formException != null) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            if (formException.message.toLowerCase().contains(getString(R.string.out_of_stock_err))) {
                                if (getViewModel().getProductType().equalsIgnoreCase(CLOTHING_PRODUCT) ||
                                        getViewModel().otherSkuList().size() > 1) {
                                    onAddToCartLoadComplete();
                                    colorSizePicker(mSkuColorList, false, true, getGlobalState().getSelectedSKUId(), true);
                                    return;
                                }
                                Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC, getString(R.string.out_of_stock), getString(R.string.out_of_stock_desc));
                                return;
                            }
                            Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, formException.message);
                            return;
                        }
                    }
                }
            }
        }

        if (addToCartList != null) {
            if (getActivity() instanceof BottomNavigationActivity)
                sendBus(new CartSummaryResponse(addItemToCartResponse));
            if (getActivity() instanceof CartActivity)
                ((CartActivity) getActivity()).onActivityResult(RESULT_FROM_ADD_TO_CART_PRODUCT_DETAIL, RESULT_FROM_ADD_TO_CART_PRODUCT_DETAIL, null);
            closeSlideUpPanel();
        }
    }

    @Override
    public void onAddItemToCartFailure(String error) {
        onAddToCartLoadComplete();
    }

    @Override
    public void onSessionTokenExpired(final Response response) {
        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE);
        final Activity activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activity != null) {
                    onAddToCartLoadComplete();
                    if (response != null) {
                        if (response.message != null) {
                            getGlobalState().setDetermineLocationPopUpEnabled(true);
                            ScreenManager.presentSSOSignin(activity);
                            onAddToCartLoadComplete();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void handleSetSuburbResponse(Object object) {
        if (object != null) {
            SetDeliveryLocationSuburbResponse setDeliveryLocationSuburbResponse = (SetDeliveryLocationSuburbResponse) object;
            switch (setDeliveryLocationSuburbResponse.httpCode) {
                case 200:
                    apiIdentifyTokenValidation();
                    break;

                default:
                    if (setDeliveryLocationSuburbResponse.response != null) {
                        Response response = setDeliveryLocationSuburbResponse.response;
                        Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
                    }
                    break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // FuseLocationAPISingleton.kt : Change location method to High Accuracy confirmation dialog
        if (requestCode == FuseLocationAPISingleton.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    llStoreFinder.performClick();
                    break;

                default:
                    dismissFindInStoreProgress();
                    break;
            }
        }

        if (requestCode == SSOActivity.SSOActivityResult.LAUNCH.rawValue()) {
            if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
                switch (getGlobalState().getSaveButtonClick()) {
                    case INDEX_ADD_TO_SHOPPING_LIST:
                        getBtnAddShoppingList().performClick();
                        break;

                    case INDEX_ADD_TO_CART:
                        activate_location_popup = true;
                        cartSummaryAPI();
                        break;

                    default:
                        break;
                }
                return;
            }
        }

        /***
         * perform add to cart call for first time user
         */
        if (requestCode == DELIVERY_LOCATION_FROM_PDP_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (getGlobalState().getSaveButtonClick() == INDEX_ADD_TO_CART) {
                    getViewDataBinding().llAddToCart.performClick();
                    return;
                }
            }

            if (resultCode == DELIVERY_LOCATION_CLOSE_CLICKED) {
                onAddToCartLoadComplete();
            }
        }

        // perform find in-store api call
        if ((requestCode == BOTTOM_FRAGMENT_REQUEST_CODE) && (resultCode == RESULT_TAP_FIND_INSTORE_BTN)) {
            mSkuId = getGlobalState().getSelectedSKUId();
            setFinInStoreOtherSkus(getGlobalState().getSelectedSKUId());
            llStoreFinder.performClick();
            return;
        } else if ((requestCode == BOTTOM_FRAGMENT_REQUEST_CODE) && (resultCode == RESULT_LOADING_INVENTORY_FAILURE)) {
            if (data != null) {
                Bundle bundleResponse = data.getExtras();
                String responseFromBundle = bundleResponse.getString("response");
                Response response = new Gson().fromJson(responseFromBundle, Response.class);
                if (response != null) {
                    Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
                }
            }
            return;
        } else if (data != null) {
            if (requestCode == BOTTOM_FRAGMENT_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    String selectedSKu = extras.getString("selected_sku");
                    String selectionType = extras.getString("sectionType");
                    OtherSkus otherSkus = new Gson().fromJson(selectedSKu, OtherSkus.class);
                    switch (selectionType) {
                        case "color":
                            setSelectedTextColor(otherSkus);
                            setSizeList(getViewModel().commonSizeList(otherSkus));
                            setAuxiliaryImages(getViewModel().getAuxiliaryImageList(otherSkus));
                            getGlobalState().setColorWasPopup(true);
                            getGlobalState().setColorPickerSku(otherSkus);
                            break;

                        case "size":
                            WTextView tvSize = getViewDataBinding().llColorSize.tvSelectedSizeValue;
                            setSelectedSize(otherSkus);
                            getGlobalState().setSizeWasPopup(true);
                            setPrice(otherSkus);
                            tvSize.setText(otherSkus.size);
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void onToastButtonClicked(String currentState) {
        if (mToastUtils != null) {
            String state = mToastUtils.getCurrentState();
            if (state.equalsIgnoreCase(currentState)) {
                Activity activity = getActivity();
                if (activity != null) {
                    if (activity instanceof CartActivity) {
                        CartActivity cartActivity = (CartActivity) activity;
                        cartActivity.navigateToList(mNumberOfListSelected);
                    }
                }
            }
        }
    }

    public String getFulFillmentType() {
        return TextUtils.isEmpty(getViewModel().getProduct().fulfillmentType) ? null : getViewModel().getProduct().fulfillmentType;
    }

    @Override
    public void onLocationChange(@NotNull Location location) {
        stopLocationUpdate();
        Utils.saveLastLocation(location, getContext());
        executeLocationItemTask();
    }

    @Override
    public void onPopUpLocationDialogMethod() {
        dismissFindInStoreProgress();
    }
}

