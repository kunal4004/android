package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated;

import android.Manifest;
import android.app.Activity;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ProductDetailsFragmentNewBinding;
import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.ILocationProvider;
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart;
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.AuxiliaryImage;
import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductDetails;
import za.co.woolworths.financial.services.android.models.dto.ProductRequest;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.SkuInventory;
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.DeliveryLocationSelectionActivity;
import za.co.woolworths.financial.services.android.ui.activities.MultipleImageActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.activities.product.ProductDetailsActivity;
import za.co.woolworths.financial.services.android.ui.adapters.AvailableSizePickerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorPickerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizePickerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ProductUtils;
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.FuseLocationAPISingleton;
import za.co.woolworths.financial.services.android.util.PermissionResultCallback;
import za.co.woolworths.financial.services.android.util.PermissionUtils;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.ToastUtils;
import za.co.woolworths.financial.services.android.util.Utils;

import static android.app.Activity.RESULT_OK;
import static za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_REQUEST_CODE;

/**
 * Created by W7099877 on 2018/07/14.
 */

public class ProductDetailsFragmentNew extends BaseFragment<ProductDetailsFragmentNewBinding, ProductDetailsViewModelNew> implements ProductDetailNavigatorNew, ProductViewPagerAdapter.MultipleImageInterface, View.OnClickListener, ProductColorPickerAdapter.OnItemSelection, ProductSizePickerAdapter.OnSizeSelection, AvailableSizePickerAdapter.OnAvailableSizeSelection, PermissionResultCallback, ToastUtils.ToastInterface, WMaterialShowcaseView.IWalkthroughActionListener, ILocationProvider {
    public ProductDetailsViewModelNew productDetailsViewModelNew;
    private String mSubCategoryTitle;
    private boolean mFetchFromJson;
    private String mDefaultProductResponse;
    private ProductViewPagerAdapter mProductViewPagerAdapter;
    private List<String> mAuxiliaryImage = new ArrayList<>();
    private ViewPager mImageViewPager;
    private WTextView txtFromPrice;
    private WTextView txtActualPrice;
    private WTextView txtSaveText;
    private ProductDetails productDetails;
    private HashMap<String, ArrayList<OtherSkus>> otherSKUsByGroupKey;
    private boolean hasColor;
    private boolean hasSize;
    private OtherSkus defaultSku;
    private OtherSkus selectedOtherSku;
    private String selectedGroupKey;
    private RelativeLayout btnColorSelector;
    private RelativeLayout btnSizeSelector;
    private WTextView tvSelectedSize;
    private BottomSheetDialog colorPickerDialog;
    private BottomSheetDialog sizePickerDialog;
    private BottomSheetDialog multiPickerDialog;
    private ProductColorPickerAdapter colorPickerAdapter;
    private ProductSizePickerAdapter sizePickerAdapter;
    private AvailableSizePickerAdapter availableSizePickerAdapter;
    private RecyclerView rcvSizePicker;
    private RelativeLayout btnFindInStore;
    private RelativeLayout btnAddToCart;
    private ViewSwitcher viewSwitcher;
    private RecyclerView rcvSizePickerForInventory;
    private RecyclerView rcvQuantityPicker;
    private ImageView imBackIconOnPicker;
    private WTextView tvMultiPickerTitle;
    private OtherSkus otherSKUForCart;
    private ProductColorPickerAdapter quantityPickerAdapter;
    private final int VIEW_SWITCHER_QUANTITY_PICKER = 1;
    private final int VIEW_SWITCHER_SIZE_PICKER = 0;
    private final int SSO_REQUEST_ADD_TO_CART = 1010;
    private final int SSO_REQUEST_ADD_TO_SHOPPING_LIST = 1011;
    private static final int REQUEST_SUBURB_CHANGE = 153;
    private BottomSheetDialog confirmDeliveryLocationDialog;
    private WButton btnAddToShoppingList;
    private OtherSkus otherSKUForList;
    private String TAG = this.getClass().getSimpleName();
    PermissionUtils permissionUtils;
    private OtherSkus otherSKUForFindInStore;
    CircleIndicator circleindicator;
    public static final int SET_DELIVERY_LOCATION_REQUEST_CODE = 180;
    private ToastUtils mToastUtils;
    private FuseLocationAPISingleton mFuseLocationAPISingleton;
    private Call<SkusInventoryForStoreResponse> mExecuteInventoryForSku;

    @Override
    public ProductDetailsViewModelNew getViewModel() {
        return productDetailsViewModelNew;
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.product_details_fragment_new;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        productDetailsViewModelNew = ViewModelProviders.of(this).get(ProductDetailsViewModelNew.class);
        productDetailsViewModelNew.setNavigator(this);
        final Bundle bundle = this.getArguments();
        if (bundle != null) {
            productDetails = (ProductDetails) Utils.jsonStringToObject(bundle.getString("strProductList"), ProductDetails.class);
            mSubCategoryTitle = bundle.getString("strProductCategory");
            mDefaultProductResponse = bundle.getString("productResponse");
            mFetchFromJson = bundle.getBoolean("fetchFromJson");
        }
        mToastUtils = new ToastUtils(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFuseLocationAPISingleton = FuseLocationAPISingleton.INSTANCE;
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_DETAIL);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(ProductDetailsActivity.walkThroughPromtView != null){
            ProductDetailsActivity.walkThroughPromtView.removeFromWindow();
        }
    }

    public void initViews() {
        txtFromPrice = getViewDataBinding().priceLayout.textPrice;
        txtActualPrice = getViewDataBinding().priceLayout.textActualPrice;
        txtSaveText = getViewDataBinding().priceLayout.tvSaveText;
        mImageViewPager = getViewDataBinding().mProductDetailPager;
        btnColorSelector = getViewDataBinding().llColorSize.relColorSelector;
        btnSizeSelector = getViewDataBinding().llColorSize.relSizeSelector;
        tvSelectedSize = getViewDataBinding().llColorSize.tvSelectedSizeValue;
        btnFindInStore = getView().findViewById(R.id.rlStoreFinder);
        btnAddToCart = getView().findViewById(R.id.rlAddToCart);
        colorPickerDialog = new BottomSheetDialog(getActivity());
        sizePickerDialog = new BottomSheetDialog(getActivity());
        multiPickerDialog = new BottomSheetDialog((getActivity()));
        btnAddToShoppingList = getViewDataBinding().btnAddShoppingList;
        getViewDataBinding().imClose.setOnClickListener(this);
        btnFindInStore.setOnClickListener(this);
        btnSizeSelector.setOnClickListener(this);
        btnColorSelector.setOnClickListener(this);
        btnAddToCart.setOnClickListener(this);
        btnAddToCart.setEnabled(false);
        btnAddToShoppingList.setOnClickListener(this);
        circleindicator = getView().findViewById(R.id.indicator);
        this.configureDefaultUI();
    }

    public void configureDefaultUI() {

        getViewDataBinding().tvProductName.setText(productDetails.productName);
        getViewDataBinding().tvSubCategoryTitle.setText(mSubCategoryTitle);

        if (!TextUtils.isEmpty(productDetails.saveText)) {
            txtSaveText.setVisibility(View.VISIBLE);
            txtSaveText.setText(productDetails.saveText);
        }

        try {
            // set price list
            ProductUtils.displayPrice(txtFromPrice, txtActualPrice, String.valueOf(productDetails.fromPrice), getViewModel().maxWasPrice(productDetails.otherSkus));
        } catch (Exception ignored) {
        }

        this.mAuxiliaryImage.add(getImageByWidth(productDetails.externalImageRef, getActivity()));
        this.mProductViewPagerAdapter = new ProductViewPagerAdapter(getActivity(), this.mAuxiliaryImage, this);
        this.mImageViewPager.setAdapter(mProductViewPagerAdapter);
        circleindicator.setViewPager(this.mImageViewPager);

        //set promotional Images
        if (productDetails.promotionImages != null)
            loadPromotionalImages(productDetails.promotionImages);

        if (mFetchFromJson) {
            ProductDetails productDetails = Utils.stringToJson(getActivity(), mDefaultProductResponse).product;
            this.onSuccessResponse(productDetails);
        } else {
            //loadProductDetails.
            getViewModel().productDetail(new ProductRequest(productDetails.productId, productDetails.sku));
        }
    }


    private void loadPromotionalImages(PromotionImages promotionalImage) {
        LinearLayout promotionalImagesLayout = getViewDataBinding().priceLayout.promotionalImages;
        List<String> images = new ArrayList<>();
        if (!TextUtils.isEmpty(promotionalImage.save))
            images.add(promotionalImage.save);
        if (!TextUtils.isEmpty(promotionalImage.wRewards))
            images.add(promotionalImage.wRewards);
        if (!TextUtils.isEmpty(promotionalImage.vitality))
            images.add(promotionalImage.vitality);
        if (!TextUtils.isEmpty(promotionalImage.newImage))
            images.add(promotionalImage.newImage);
        promotionalImagesLayout.removeAllViews();
        DrawImage drawImage = new DrawImage(getActivity());
        for (String image : images) {
            Activity activity = getActivity();
            if (activity == null) return;
            View view = activity.getLayoutInflater().inflate(R.layout.promotional_image, null);
            SimpleDraweeView simpleDraweeView = view.findViewById(R.id.promotionImage);
            drawImage.displaySmallImage(simpleDraweeView, image);
            promotionalImagesLayout.addView(view);
        }
    }


    @Override
    public void onClick(View view) {

        // To avoid clicks while feature tutorial popup showing
        if (!Utils.isFeatureTutorialsDismissed(ProductDetailsActivity.walkThroughPromtView))
            return;

        switch (view.getId()) {
            case R.id.imClose:
                getActivity().onBackPressed();
                break;
            case R.id.relColorSelector:
                colorPickerDialog.show();
                break;
            case R.id.relSizeSelector:
                openSizePicker(selectedGroupKey, false, false);
                break;
            case R.id.rlAddToCart:
                addItemToCart();
                break;
            case R.id.btnAddShoppingList:
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPADDTOLIST);
                addItemToShoppingList();
                break;
            case R.id.rlStoreFinder:
                findItemInStore();
                break;
        }
    }

    private void findItemInStore() {

        if (Utils.isLocationEnabled(getActivity())) {
            if (!checkRunTimePermissionForLocation()) {
                return;
            }
        } else {
            Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.LOCATION_OFF, "");
            return;
        }

        if (otherSKUForFindInStore != null) {
            // once application get current location it will execute findInStore API
            this.startLocationUpdates();
        } else if (this.selectedOtherSku != null) {
            this.otherSKUForFindInStore = this.selectedOtherSku;
            this.findItemInStore();
            return;
        } else {
            openSizePicker(this.selectedGroupKey, false, true);
            return;
        }

    }

    public void addItemToShoppingList() {
        if (!SessionUtilities.getInstance().isUserAuthenticated()) {
            ScreenManager.presentSSOSignin(getActivity(), SSO_REQUEST_ADD_TO_SHOPPING_LIST);
        } else if (selectedOtherSku == null && otherSKUForList == null) { // Size picker was not selected, open size selector first
            openSizePicker(this.selectedGroupKey, true, false);
        } else {
            OtherSkus otherSkus = (otherSKUForList == null) ? selectedOtherSku : otherSKUForList;
            AddToListRequest item = new AddToListRequest();
            item.setQuantity("1");
            item.setSkuID(otherSkus.sku);
            item.setCatalogRefId(otherSkus.sku);
            item.setGiftListId(otherSkus.sku);
            ArrayList<AddToListRequest> addToListRequests = new ArrayList<>();
            addToListRequests.add(item);
            NavigateToShoppingList.Companion.openShoppingList(getActivity(), addToListRequests, "", false);
            otherSKUForList = null; // remove otherSKUForList value to enable openSizePicker when user re-tap add to list button
        }
    }

    public void addItemToCart() {
        if (!SessionUtilities.getInstance().isUserAuthenticated()) {
            ScreenManager.presentSSOSignin(getActivity(), SSO_REQUEST_ADD_TO_CART);
            return;
        }

        ShoppingDeliveryLocation deliveryLocation = Utils.getPreferredDeliveryLocation();
        if (deliveryLocation == null) {
            enableAddToCartButton(true);
            getViewModel().getCartSummary();
            return;
        }

        if (this.selectedOtherSku != null && this.otherSKUForCart == null) {
            this.otherSKUForCart = this.selectedOtherSku;
            addItemToCart();
            return;
        } else {
            this.enableAddToCartButton(true);
            String storeId = Utils.retrieveStoreId(productDetails.fulfillmentType);
            if (TextUtils.isEmpty(storeId)) {
                this.otherSKUForCart = null;
                String message = "Unfortunately this item is unavailable in " + deliveryLocation.suburb.name + ". Try changing your delivery location and try again.";
                Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC, getString(R.string.product_unavailable), message);
                enableAddToCartButton(false);
                return;
            }

            if (this.otherSKUForCart != null)
              mExecuteInventoryForSku =  getViewModel().queryInventoryForSKUs(storeId, this.otherSKUForCart.sku, false);
            else {
                String multiSKUs = getViewModel().getMultiSKUsStringForInventory(this.otherSKUsByGroupKey.get(this.selectedGroupKey));
                mExecuteInventoryForSku = getViewModel().queryInventoryForSKUs(storeId, multiSKUs, true);
            }

        }

    }

    @Override
    public String getImageByWidth(String imageUrl, Context context) {
        WindowManager display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        assert display != null;
        Display deviceHeight = display.getDefaultDisplay();
        Point size = new Point();
        deviceHeight.getSize(size);
        int width = size.x;
        if (TextUtils.isEmpty(imageUrl)) {
            imageUrl = "https://images.woolworthsstatic.co.za/";
        }
        return imageUrl + "" + ((imageUrl.contains("jpg")) ? "" : "?w=" + width + "&q=" + 85);
    }


    @Override
    public void onSuccessResponse(ProductDetails productDetails) {
        this.productDetails = productDetails;
        if (this.productDetails.otherSkus != null && this.productDetails.otherSkus.size() > 0) {
            this.otherSKUsByGroupKey = groupOtherSKUsByColor(productDetails.otherSkus);
            this.updateDefaultUI();
        } else {
            getViewDataBinding().llLoadingColorSize.setVisibility(View.GONE);
            getViewDataBinding().loadingInfoView.setVisibility(View.GONE);

            if (isAdded())
                Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.CLI_ERROR, getString(R.string.statement_send_email_false_desc));
        }
    }

    public void updateDefaultUI() {
        this.defaultSku = getDefaultSku(otherSKUsByGroupKey);
        // when there is no size available
        // selectedSKU will be the defaultSKU
        if (!hasSize)
            this.selectedOtherSku = this.defaultSku;
        getViewDataBinding().llLoadingColorSize.setVisibility(View.GONE);
        getViewDataBinding().loadingInfoView.setVisibility(View.GONE);
        this.configureButtonsAndSelectors();
        this.updateViewPagerWithAuxiliaryImages();
        this.setPromotionalText(productDetails);
        this.setProductCode(productDetails.productId);
        this.loadPromotionalImages(productDetails.promotionImages);
        this.setProductDescription(getViewModel().getProductDescription(getActivity(), productDetails));
        this.configureUIForOtherSKU(defaultSku);
        this.displayIngredients();
    }

    public void configureButtonsAndSelectors() {
        btnAddToShoppingList.setEnabled(true);
        getViewDataBinding().colorSizeLayout.setVisibility((hasColor || hasSize) ? View.VISIBLE : View.GONE);
        btnColorSelector.setEnabled(hasColor);
        btnSizeSelector.setEnabled(hasSize);

        // if colors not available set the color icon to N/A icon , Icons will look like " / "
        if (hasColor) {
            this.configureColorPicker();
        } else {
            this.setSelectedColorIcon();
        }

        if (hasSize) {
            this.configureSizePicker();
        } else {
            tvSelectedSize.setText("NO SZ");
        }

        btnFindInStore.setVisibility(Boolean.valueOf(productDetails.isnAvailable) ? View.VISIBLE : View.GONE);
        btnAddToCart.setAlpha(1f);
        btnAddToCart.setEnabled(true);
        this.configureMultiPickerDialog();
        if (Boolean.valueOf(productDetails.isnAvailable)) {
            showFeatureWalkthrough();
        }
    }

    private void configureColorPicker() {
        if (!isAdded())
            return;
        View view = getActivity().getLayoutInflater().inflate(R.layout.color_size_picker_bottom_sheet_dialog, null);
        WTextView title = view.findViewById(R.id.title);
        RecyclerView rcvColors = view.findViewById(R.id.rvPickerList);
        ImageView closePicker = view.findViewById(R.id.imClosePicker);
        closePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPickerDialog.dismiss();
            }
        });
        rcvColors.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        title.setText(getString(R.string.confirm_color_desc));
        colorPickerAdapter = new ProductColorPickerAdapter(new ArrayList<>(this.otherSKUsByGroupKey.keySet()), this);
        rcvColors.setAdapter(colorPickerAdapter);
        colorPickerDialog.setContentView(view);
    }

    private void configureSizePicker() {
        if (!isAdded())
            return;
        View view = getActivity().getLayoutInflater().inflate(R.layout.color_size_picker_bottom_sheet_dialog, null);
        WTextView title = view.findViewById(R.id.title);
        rcvSizePicker = view.findViewById(R.id.rvPickerList);
        ImageView closeSizePicker = view.findViewById(R.id.imClosePicker);
        closeSizePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPickerDialog(sizePickerDialog);
            }
        });
        rcvSizePicker.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        title.setText(getString(R.string.confirm_size_range_desc));
        sizePickerDialog.setContentView(view);
    }

    private void configureMultiPickerDialog() {
        if (!isAdded())
            return;
        View view = getActivity().getLayoutInflater().inflate(R.layout.size_quantity_selector_layout, null);
        viewSwitcher = view.findViewById(R.id.viewSwitcher);
        rcvSizePickerForInventory = view.findViewById(R.id.sizeSelectorForInventory);
        rcvQuantityPicker = view.findViewById(R.id.quantitySelector);
        rcvQuantityPicker.setNestedScrollingEnabled(false);
        rcvSizePickerForInventory.setNestedScrollingEnabled(false);
        ImageView closeSizePicker = view.findViewById(R.id.imCloseIcon);
        tvMultiPickerTitle = view.findViewById(R.id.title);
        imBackIconOnPicker = view.findViewById(R.id.imBackIcon);
        rcvQuantityPicker.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcvSizePickerForInventory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        closeSizePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPickerDialog(multiPickerDialog);
                enableAddToCartButton(false);
            }
        });
        multiPickerDialog.setCanceledOnTouchOutside(false);
        multiPickerDialog.setCancelable(false);
        imBackIconOnPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewSwitcher.setDisplayedChild(VIEW_SWITCHER_SIZE_PICKER);
                imBackIconOnPicker.setVisibility(View.GONE);
            }
        });
        multiPickerDialog.setContentView(view);

        // ViewSwitcher setMeasureAllChildren to true will occupy the space of the largest child
        // false attribute will discard setting largest height as default height
        viewSwitcher.setMeasureAllChildren(false);

        viewSwitcher.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                switch (viewSwitcher.getDisplayedChild()) {
                    case VIEW_SWITCHER_SIZE_PICKER:
                        rcvQuantityPicker.setVisibility(View.GONE);
                        rcvSizePickerForInventory.setVisibility(View.VISIBLE);
                        tvMultiPickerTitle.setText(getString(R.string.available_sizes));
                        break;

                    case VIEW_SWITCHER_QUANTITY_PICKER:
                        rcvQuantityPicker.setVisibility(View.VISIBLE);
                        rcvSizePickerForInventory.setVisibility(View.GONE);
                        tvMultiPickerTitle.setText(getString(R.string.edit_quantity));
                        break;

                    default:
                        break;
                }
            }
        });
    }

    public void openSizePicker(String groupKey, boolean isForShoppingList, boolean isForFindInStore) {

        //if (isForInventory = true) - color picker is used for select size to check inventory

        ArrayList<OtherSkus> selectedOtherSKUsForGroupKey = this.otherSKUsByGroupKey.get(groupKey);
        sizePickerAdapter = new ProductSizePickerAdapter(selectedOtherSKUsForGroupKey, this, isForShoppingList, isForFindInStore);
        rcvSizePicker.setAdapter(sizePickerAdapter);
        sizePickerDialog.show();
    }

    public void openQuantityPicker(int inStockQuantity, boolean isMultiSKUs) {
        quantityPickerAdapter = new ProductColorPickerAdapter(inStockQuantity, this);
        rcvQuantityPicker.setAdapter(quantityPickerAdapter);
        viewSwitcher.setDisplayedChild(VIEW_SWITCHER_QUANTITY_PICKER);
        // isMultiSKUs will manage back button on picker
        // will true when user have option to select other size's
        imBackIconOnPicker.setVisibility(isMultiSKUs ? View.VISIBLE : View.GONE);
        tvMultiPickerTitle.setText(getString(R.string.edit_quantity));
        multiPickerDialog.show();
    }

    public void openSizePickerWithAvailableQuantity(ArrayList<OtherSkus> otherSkuses) {
        availableSizePickerAdapter = new AvailableSizePickerAdapter(otherSkuses, this);
        rcvSizePickerForInventory.setAdapter(availableSizePickerAdapter);
        viewSwitcher.setDisplayedChild(VIEW_SWITCHER_SIZE_PICKER);
        tvMultiPickerTitle.setText(getString(R.string.available_sizes));
        multiPickerDialog.show();
    }

    private void configureUIForOtherSKU(OtherSkus otherSku) {

        try {
            // set price list
            ProductUtils.displayPrice(txtFromPrice, txtActualPrice, otherSku.price, String.valueOf(otherSku.wasPrice));
        } catch (Exception ignored) {
        }

        if (hasColor)
            this.setSelectedColorIcon();
    }


    @Override
    public void onFailureResponse(String s) {
    }

    @Override
    public void responseFailureHandler(Response response) {
        enableAddToCartButton(false);
        enableFindInStoreButton(false);
        if (isAdded())
            Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
    }

    @Override
    public void requestDeliveryLocation(String requestMessage) {
        enableAddToCartButton(false);
        enableFindInStoreButton(false);
        if (isAdded())
            Utils.displayValidationMessageForResult(this,
                    getActivity(),
                    CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                    null,
                    requestMessage,
                    getResources().getString(R.string.set_delivery_location_button),
                    SET_DELIVERY_LOCATION_REQUEST_CODE);
    }

    @Override
    public void setProductCode(String productCode) {
        try {
            getViewDataBinding().productCode.setVisibility(View.VISIBLE);
            getViewDataBinding().productCode.setText(getString(R.string.product_code) + ": " + productCode);
        } catch (IllegalStateException ex) {
            getViewDataBinding().productCode.setVisibility(View.GONE);
            Log.d("setProductCode", ex.getMessage());
        }
    }

    @Override
    public void setProductDescription(String productDescription) {
        getViewDataBinding().webDescription.loadDataWithBaseURL("file:///android_res/drawable/",
                productDescription, "text/html; charset=UTF-8", "UTF-8", null);
    }

    @Override
    public void dismissFindInStoreProgress() {
        enableFindInStoreButton(false);
    }

    @Override
    public void onLocationItemSuccess(List<StoreDetails> location) {
        Activity activity = getActivity();
        if (activity == null) return;
        this.enableFindInStoreButton(false);
        if (location.size() > 0) {
            getGlobalState().setStoreDetailsArrayList(location);
            Intent intentInStoreFinder = new Intent(activity, WStockFinderActivity.class);
            intentInStoreFinder.putExtra("PRODUCT_NAME", mSubCategoryTitle);
            startActivity(intentInStoreFinder);
            activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
        } else {
            this.showOutOfStockInStores();
        }
    }

    @Override
    public void outOfStockDialog() {
        if (isAdded())
            Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC, getString(R.string.out_of_stock), getString(R.string.out_of_stock_desc));
    }

    @Override
    public void showOutOfStockInStores() {
        if (isAdded())
            Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.NO_STOCK, "");
    }

    @Override
    public void onTokenFailure(String e) {
        enableAddToCartButton(false);
    }

    @Override
    public void onCartSummarySuccess(CartSummaryResponse cartSummaryResponse) {
        this.enableAddToCartButton(false);
        if (cartSummaryResponse.data != null) {
            List<CartSummary> cartSummaryList = cartSummaryResponse.data;
            if (cartSummaryList.get(0) != null) {
                CartSummary cartSummary = cartSummaryList.get(0);
                if (TextUtils.isEmpty(cartSummary.suburbId)) {
                    startActivityToSelectDeliveryLocation(true);
                } else {
                    // show popup to confirm location
                    this.confirmDeliveryLocation();
                }
            }
        }
    }

    private void startActivityToSelectDeliveryLocation(boolean addItemToCartOnFinished) {
        if (getActivity() != null) {
            Intent openDeliveryLocationSelectionActivity = new Intent(this.getContext(), DeliveryLocationSelectionActivity.class);
            if (addItemToCartOnFinished) {
                startActivityForResult(openDeliveryLocationSelectionActivity, REQUEST_SUBURB_CHANGE);
            } else {
                startActivity(openDeliveryLocationSelectionActivity);
            }
            getActivity().overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay);
        }
    }

    @Override
    public void addItemToCartResponse(AddItemToCartResponse addItemToCartResponse) {
        this.enableAddToCartButton(false);
        Intent intent = new Intent();
        if (addItemToCartResponse.data.size() > 0) {
            String successMessage = addItemToCartResponse.data.get(0).message;
            intent.putExtra("addedToCartMessage", successMessage);
        }
        Activity activity = getActivity();
        if (activity == null) return;
        activity.setResult(RESULT_OK, intent);
        activity.onBackPressed();
    }

    @Override
    public void onAddItemToCartFailure(String error) {
        enableAddToCartButton(false);
    }

    @Override
    public void onSessionTokenExpired() {

        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE);
        final Activity activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activity != null) {
                    enableAddToCartButton(false);
                    otherSKUForCart = null;
                    otherSKUForList = null;
                    ScreenManager.presentSSOSignin(activity);
                }
            }
        });
    }

    @Override
    public void onInventoryResponseForSelectedSKU(SkusInventoryForStoreResponse inventoryResponse) {
        int quantityInStock = 0;
        for (SkuInventory skuInventory : inventoryResponse.skuInventory) {
            if (skuInventory.sku.equalsIgnoreCase(this.otherSKUForCart.sku)) {
                quantityInStock = skuInventory.quantity;
            }
        }

        if (quantityInStock == 1) {
            executeAddToCartRequest(1);
        } else if (quantityInStock == 0) {

            if (this.otherSKUsByGroupKey.get(this.selectedGroupKey).size() == 1) {
                //if there is no other skus for that selected group show out of stock
                this.enableAddToCartButton(false);
                this.outOfStockDialog();
                return;
            }

            String multiSKUS = getViewModel().getMultiSKUsStringForInventory(this.otherSKUsByGroupKey.get(this.selectedGroupKey));
            String storeId = Utils.retrieveStoreId(productDetails.fulfillmentType);
            getViewModel().queryInventoryForSKUs(storeId, multiSKUS, true);

        } else {
            openQuantityPicker(quantityInStock, false);
        }
    }

    @Override
    public void onInventoryResponseForAllSKUs(SkusInventoryForStoreResponse inventoryResponse) {
        if (getActivity() == null || rcvSizePickerForInventory== null) return;
        ArrayList<OtherSkus> stockRequestedSkus = this.otherSKUsByGroupKey.get(this.selectedGroupKey);

        for (OtherSkus otherSkus : stockRequestedSkus) {
            for (SkuInventory skuInventory : inventoryResponse.skuInventory) {
                if (otherSkus.sku.equalsIgnoreCase(skuInventory.sku)) {
                    otherSkus.quantity = skuInventory.quantity;
                    break;
                }
            }
        }
        openSizePickerWithAvailableQuantity(stockRequestedSkus);

    }

    @Override
    public void onProductDetailedFailed(Response response) {
        getViewDataBinding().llLoadingColorSize.setVisibility(View.GONE);
        getViewDataBinding().loadingInfoView.setVisibility(View.GONE);
        if (isAdded())
            Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
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


    public HashMap<String, ArrayList<OtherSkus>> groupOtherSKUsByColor(ArrayList<OtherSkus> otherSKUsList) {
        otherSKUsByGroupKey = new HashMap<>();
        for (OtherSkus otherSkuObj : otherSKUsList) {
            String groupKey = "";
            if (TextUtils.isEmpty(otherSkuObj.colour) && !TextUtils.isEmpty(otherSkuObj.size)) {
                this.hasSize = otherSkuObj.size.equalsIgnoreCase("NO SZ") ? false : true;
                groupKey = otherSkuObj.size.trim();
            } else if (!TextUtils.isEmpty(otherSkuObj.colour) && !TextUtils.isEmpty(otherSkuObj.size)) {
                this.hasColor = otherSkuObj.colour.equalsIgnoreCase("N/A") ? false : true;
                this.hasSize = otherSkuObj.size.equalsIgnoreCase("NO SZ") ? false : true;
                groupKey = otherSkuObj.colour.trim();
            } else {
                this.hasColor = true;
                groupKey = otherSkuObj.colour.trim();
            }

            if (!otherSKUsByGroupKey.containsKey(groupKey)) {
                this.otherSKUsByGroupKey.put(groupKey, new ArrayList<OtherSkus>());
            }
            this.otherSKUsByGroupKey.get(groupKey).add(otherSkuObj);
        }
        return otherSKUsByGroupKey;
    }

    public OtherSkus getDefaultSku(HashMap<String, ArrayList<OtherSkus>> otherSKUsList) {

        for (String key : otherSKUsList.keySet()) {
            for (OtherSkus otherSkusObj : otherSKUsList.get(key)) {
                if (otherSkusObj.sku.equalsIgnoreCase(this.productDetails.sku)) {
                    this.selectedGroupKey = key;
                    return otherSkusObj;
                }
            }
        }
        return null;

    }

    private void updateViewPagerWithAuxiliaryImages() {
        Activity activity = getActivity();
        if (activity == null) return;
        this.mAuxiliaryImage = this.getAuxiliaryImagesByGroupKey(this.selectedGroupKey);
        //mProductViewPagerAdapter.updatePagerItems(this.mAuxiliaryImage);
        mProductViewPagerAdapter = new ProductViewPagerAdapter(activity, this.mAuxiliaryImage, this);
        mImageViewPager.setAdapter(mProductViewPagerAdapter);
        circleindicator.setViewPager(this.mImageViewPager);
    }

    public List<String> getAuxiliaryImagesByGroupKey(String groupKey) {
        if (TextUtils.isEmpty(groupKey)) {
            return this.mAuxiliaryImage;
        }
        List<String> updatedAuxiliaryImages = new ArrayList<>();
        ArrayList<OtherSkus> otherSkusArrayList = this.otherSKUsByGroupKey.get(groupKey);
        if (otherSkusArrayList != null) {
            String imageFromOtherSku = otherSkusArrayList.get(0).externalImageRef;
            if (this.productDetails.otherSkus.size() > 0 && imageFromOtherSku != null)
                updatedAuxiliaryImages.add(imageFromOtherSku);
        }

        Map<String, AuxiliaryImage> allAuxImages = new Gson().fromJson(this.productDetails.auxiliaryImages, new TypeToken<Map<String, AuxiliaryImage>>() {
        }.getType());

        String codeForAuxImage = this.getCodeForAuxiliaryImagesByOtherSkusGroupKey(groupKey);
        for (Map.Entry<String, AuxiliaryImage> entry : allAuxImages.entrySet()) {
            if (entry.getKey().contains(codeForAuxImage)) {
                updatedAuxiliaryImages.add(entry.getValue().externalImageRef);
            }
        }

        return (updatedAuxiliaryImages.size() != 0) ? updatedAuxiliaryImages : this.mAuxiliaryImage;
    }

    public String getCodeForAuxiliaryImagesByOtherSkusGroupKey(String groupKey) {

        String codeForAuxiliaryImages = "";
        String[] splitStr = groupKey.split("\\s+");
        if (splitStr.length == 1) {
            codeForAuxiliaryImages = splitStr[0];
        } else {
            //When the components consists of more than 1
            // i.e. let's say LIGHT BLUE, then:
            // 1. Use the first character of the first word
            // 2. Append the remaining words to create the desired code
            for (int i = 0; i < splitStr.length; i++) {
                if (i == 0) {
                    codeForAuxiliaryImages = splitStr[i];
                } else {
                    codeForAuxiliaryImages = codeForAuxiliaryImages.concat(splitStr[i]);
                }
            }
        }
        return codeForAuxiliaryImages;
    }

    private void setSelectedColorIcon() {
        WrapContentDraweeView mImSelectedColor = getViewDataBinding().llColorSize.imSelectedColor;
        DrawImage drawImage = new DrawImage(getActivity());
        String url = this.otherSKUsByGroupKey.get(this.selectedGroupKey).get(0).externalColourRef;
        mImSelectedColor.setImageAlpha(TextUtils.isEmpty(url) ? 0 : 255);
        drawImage.displayImage(mImSelectedColor, url);
    }

    @Override
    public void onColorSelected(String color) {

        if (this.selectedGroupKey.equalsIgnoreCase(color.trim())) {
            colorPickerDialog.dismiss();
            return;
        }

        this.selectedGroupKey = color;
        this.setSelectedColorIcon();
        this.updateViewPagerWithAuxiliaryImages();

        // when there is no size available
        // selectedSKU will be from color group
        if (!hasSize) {
            this.selectedOtherSku = this.otherSKUsByGroupKey.get(this.selectedGroupKey).get(0);
            this.configureUIForOtherSKU(this.selectedOtherSku);
            this.colorPickerDialog.dismiss();
            return;
        }

        //===== positive flow
        // if selected size available for the selected color
        // get the sku for the selected size from the new color group
        // update the selectedSizeSKU

        //===== negative flow
        // if selected size not available on the new color group
        // make selectedSKU to null

        if (this.selectedOtherSku != null) {
            ArrayList<OtherSkus> selectedColorSKUs = this.otherSKUsByGroupKey.get(this.selectedGroupKey);
            int index = -1;
            for (int i = 0; i < selectedColorSKUs.size(); i++) {
                if (selectedColorSKUs.get(i).size.equalsIgnoreCase(this.selectedOtherSku.size)) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                this.selectedOtherSku = null;
                this.tvSelectedSize.setText(getString(R.string.select));
                this.defaultSku = this.otherSKUsByGroupKey.get(this.selectedGroupKey).get(0);
                this.configureUIForOtherSKU(this.defaultSku);
            } else {
                this.selectedOtherSku = selectedColorSKUs.get(index);
                this.configureUIForOtherSKU(selectedOtherSku);
            }
        }
        colorPickerDialog.dismiss();
    }

    @Override
    public void onQuantitySelected(int selectedQuantity) {
        this.executeAddToCartRequest(selectedQuantity);
    }

    @Override
    public void onSizeSelected(OtherSkus selectedSizeSku) {
        this.selectedOtherSku = selectedSizeSku;
        this.tvSelectedSize.setText(this.selectedOtherSku.size);
        getGlobalState().setSelectedSKUId(selectedSizeSku);
        this.configureUIForOtherSKU(selectedOtherSku);
        sizePickerDialog.dismiss();
    }

    @Override
    public void onSizeSelectedForShoppingList(OtherSkus selectedSizeSku) {
        sizePickerDialog.dismiss();
        this.otherSKUForList = selectedSizeSku;
        this.addItemToShoppingList();
    }

    @Override
    public void onSizeSelectedForFindInStore(OtherSkus selectedSizeSku) {
        sizePickerDialog.dismiss();
        this.otherSKUForFindInStore = selectedSizeSku;
        this.findItemInStore();
    }

    public void executeAddToCartRequest(int quantity) {
        AddItemToCart item = new AddItemToCart(productDetails.productId, this.otherSKUForCart.sku, quantity);
        ArrayList<AddItemToCart> listOfItems = new ArrayList<>();
        listOfItems.add(item);
        getViewModel().postAddItemToCart(listOfItems);
        this.dismissPickerDialog(multiPickerDialog);
    }

    public void dismissPickerDialog(BottomSheetDialog dialog) {
        if (dialog.isShowing()) {
            dialog.dismiss();
            this.otherSKUForCart = null;
        }
    }

    @Override
    public void onAvailableSizeSelected(OtherSkus selectedAvailableSizeSku) {
        this.otherSKUForCart = selectedAvailableSizeSku;
        if (this.otherSKUForCart.quantity == 1) {
            this.executeAddToCartRequest(1);
            this.dismissPickerDialog(multiPickerDialog);
            return;
        }
        imBackIconOnPicker.setVisibility(View.VISIBLE);
        quantityPickerAdapter = new ProductColorPickerAdapter(this.otherSKUForCart.quantity, this);
        rcvQuantityPicker.setAdapter(quantityPickerAdapter);
        viewSwitcher.setDisplayedChild(VIEW_SWITCHER_QUANTITY_PICKER);
    }

    @Override
    public void onFindInStoreForNotAvailableProducts(OtherSkus notAvailableSKU) {
        this.dismissPickerDialog(multiPickerDialog);
        enableAddToCartButton(false);
        this.otherSKUForFindInStore = notAvailableSKU;
        this.findItemInStore();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // FuseLocationAPISingleton.kt : Change location method to High Accuracy confirmation dialog
        if (requestCode == FuseLocationAPISingleton.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    findItemInStore();
                    break;

                default:
                    dismissFindInStoreProgress();
                    break;
            }
        }
        if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            switch (requestCode) {
                case SSO_REQUEST_ADD_TO_CART:
                    addItemToCart();
                    break;
                case SSO_REQUEST_ADD_TO_SHOPPING_LIST:
                    addItemToShoppingList();
                    //One time biometricsWalkthrough
                    ScreenManager.presentBiometricWalkthrough(getActivity());
                    break;
            }
        } else if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SUBURB_CHANGE:
                    addItemToCart();
                    break;
                case ADD_TO_SHOPPING_LIST_REQUEST_CODE:
                    int listSize = data.getIntExtra("sizeOfList", 0);
                    boolean isSessionExpired = data.getBooleanExtra("sessionExpired", false);
                    if (isSessionExpired) {
                        onSessionTokenExpired();
                        return;
                    }
                    showToastMessage(getActivity(), listSize);
                    break;
                case SET_DELIVERY_LOCATION_REQUEST_CODE:
                    startActivityToSelectDeliveryLocation(false);
                    break;
            }
        }
    }

    public void confirmDeliveryLocation() {
        if (!isAdded())
            return;
        confirmDeliveryLocationDialog = new BottomSheetDialog(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.confirm_deliverylocation_bottom_sheet_dialog, null);
        WTextView tvLocation = view.findViewById(R.id.tvLocation);
        WButton btnSetNewLocation = view.findViewById(R.id.btnSetNewLocation);
        ImageView closeDialog = view.findViewById(R.id.imCloseIcon);
        Button btnConfirmLocation = view.findViewById(R.id.btnDefaultLocation);
        ShoppingDeliveryLocation shoppingDeliveryLocation = Utils.getPreferredDeliveryLocation();
        if (shoppingDeliveryLocation != null) {
            Suburb suburb = shoppingDeliveryLocation.suburb;
            if (suburb != null) {
                tvLocation.setText(suburb.name + ", " + shoppingDeliveryLocation.province.name);
            }
        }
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableAddToCartButton(false);
                dismissPickerDialog(confirmDeliveryLocationDialog);
            }
        });
        btnConfirmLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDeliveryLocationDialog.dismiss();
                addItemToCart();
            }
        });
        btnSetNewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDeliveryLocationDialog.dismiss();
                startActivityToSelectDeliveryLocation(true);
            }
        });
        confirmDeliveryLocationDialog.setCancelable(false);
        confirmDeliveryLocationDialog.setCanceledOnTouchOutside(false);
        confirmDeliveryLocationDialog.setContentView(view);
        confirmDeliveryLocationDialog.show();

        //One time biometricsWalkthrough
        ScreenManager.presentBiometricWalkthrough(getActivity());
    }

    private void executeLocationItemTask() {
        getViewModel().locationItemTask(getActivity(), this.otherSKUForFindInStore);
    }

    @Override
    public void startLocationUpdates() {
        Activity activity = getActivity();
        if ((activity == null) || (mFuseLocationAPISingleton == null)) return;

        this.enableFindInStoreButton(true);
        mFuseLocationAPISingleton.addLocationChangeListener(this);
        mFuseLocationAPISingleton.startLocationUpdate();
    }

    @Override
    public void stopLocationUpdate() {
        // stop location updates
        if (mFuseLocationAPISingleton != null)
            mFuseLocationAPISingleton.stopLocationUpdate();
    }

    @Override
    public void PermissionGranted(int request_code) {
        this.findItemInStore();
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {

    }

    @Override
    public void PermissionDenied(int request_code) {

    }

    @Override
    public void NeverAskAgain(int request_code) {

    }

    public boolean checkRunTimePermissionForLocation() {
        permissionUtils = new PermissionUtils(getActivity(), this);
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        boolean result = permissionUtils.checkAndRequestPermissions(permissions, 1);
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void enableAddToCartButton(boolean isLoading) {
        getViewDataBinding().llAddToCartFindInStore.pbAddToCart.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        getViewDataBinding().llAddToCartFindInStore.tvAddToCart.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if (isLoading) {
            btnAddToCart.setEnabled(false);
            btnFindInStore.setEnabled(false);
            btnAddToShoppingList.setEnabled(false);
        } else {
            btnAddToCart.setEnabled(true);
            btnFindInStore.setEnabled(true);
            btnAddToShoppingList.setEnabled(true);
        }
    }

    public void enableFindInStoreButton(boolean isLoading) {
        getViewDataBinding().llAddToCartFindInStore.mButtonProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        getViewDataBinding().llAddToCartFindInStore.tvBtnFinder.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if (isLoading) {
            btnAddToCart.setEnabled(false);
            btnFindInStore.setEnabled(false);
            btnAddToShoppingList.setEnabled(false);
        } else {
            btnAddToCart.setEnabled(true);
            btnFindInStore.setEnabled(true);
            btnAddToShoppingList.setEnabled(true);
        }
    }

    private void showToastMessage(Activity activity, int mNumberOfListSelected) {
        if (mNumberOfListSelected == 0)
            return;
        mToastUtils.setActivity(activity);
        mToastUtils.setCurrentState(TAG);
        String shoppingList = getString(R.string.shopping_list);
        // shopping list vs shopping lists
        mToastUtils.setCartText((mNumberOfListSelected > 1) ? shoppingList + "s" : shoppingList);
        mToastUtils.setPixel(getViewDataBinding().llAddToCartFindInStore.rlStoreFinder.getHeight() * 2);
        mToastUtils.setView(getViewDataBinding().llAddToCartFindInStore.rlStoreFinder);
        mToastUtils.setMessage(R.string.added_to);
        mToastUtils.setViewState(true);
        mToastUtils.build();
    }

    @Override
    public void onToastButtonClicked(String currentState) {
        if (getActivity() == null)
            return;
        Intent intent = new Intent();
        intent.putExtra("addedToShoppingList", true);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().onBackPressed();

    }

    public void displayIngredients() {
        if (!TextUtils.isEmpty(this.productDetails.ingredients)) {
            getViewDataBinding().linIngredient.setVisibility(View.VISIBLE);
            getViewDataBinding().ingredientList.setText(this.productDetails.ingredients);
        }
    }

    public void showFeatureWalkthrough() {
        if (getActivity() == null)
            return;
        if (!AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.findInStore)
            return;
        Crashlytics.setString(getString(R.string.crashlytics_materialshowcase_key),this.getClass().getCanonicalName());
        ProductDetailsActivity.walkThroughPromtView = new WMaterialShowcaseView.Builder(getActivity(), WMaterialShowcaseView.Feature.FIND_IN_STORE)
                .setTarget(btnFindInStore)
                .setTitle(R.string.tips_tricks_titles_stores)
                .setDescription(R.string.walkthrough_in_store_desc)
                .setActionText(R.string.check_in_store_availability)
                .setImage(R.drawable.tips_tricks_ic_stores)
                .setAction(this)
                .withRectangleShape()
                .setArrowPosition(WMaterialShowcaseView.Arrow.BOTTOM_LEFT)
                .setMaskColour(getResources().getColor(R.color.semi_transparent_black)).build();
        ProductDetailsActivity.walkThroughPromtView.show(getActivity());
    }

    @Override
    public void onWalkthroughActionButtonClick() {
        this.onClick(btnFindInStore);
    }

    @Override
    public void onPromptDismiss() {

    }

    public void setPromotionalText(ProductDetails productDetails) {
        if (!TextUtils.isEmpty(productDetails.saveText)) {
            txtSaveText.setVisibility(View.VISIBLE);
            txtSaveText.setText(productDetails.saveText);
        }
    }

    @Override
    public void onLocationChange(@NotNull Location location) {
        Utils.saveLastLocation(location, getContext());
        stopLocationUpdate();
        executeLocationItemTask();
    }

    @Override
    public void onPopUpLocationDialogMethod() {
        dismissFindInStoreProgress();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mExecuteInventoryForSku!=null &&  !mExecuteInventoryForSku.isCanceled()){
            mExecuteInventoryForSku.cancel();
        }
    }
}
