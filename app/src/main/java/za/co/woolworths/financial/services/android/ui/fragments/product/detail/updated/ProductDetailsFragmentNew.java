package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ProductDetailsFragmentNewBinding;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse;
import za.co.woolworths.financial.services.android.models.dto.AuxiliaryImage;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductDetails;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.models.rest.product.ProductRequest;
import za.co.woolworths.financial.services.android.ui.activities.CartActivity;
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorPickerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizePickerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ProductUtils;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 2018/07/14.
 */

public class ProductDetailsFragmentNew extends BaseFragment<ProductDetailsFragmentNewBinding, ProductDetailsViewModelNew> implements ProductDetailNavigatorNew, ProductViewPagerAdapter.MultipleImageInterface, View.OnClickListener, ProductColorPickerAdapter.OnColorSelection, ProductSizePickerAdapter.OnSizeSelection {
	public ProductDetailsViewModelNew productDetailsViewModelNew;
	private String mSubCategoryTitle;
	private boolean mFetchFromJson;
	private String mDefaultProductResponse;
	private ProductList mDefaultProduct;
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
	private ProductColorPickerAdapter colorPickerAdapter;
	private ProductSizePickerAdapter sizePickerAdapter;
	private RecyclerView rcvSizePicker;
	private RelativeLayout btnFindInStore;
	private RelativeLayout btnAddToCart;


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
			mDefaultProduct = (ProductList) Utils.jsonStringToObject(bundle.getString("strProductList"), ProductList.class);
			mSubCategoryTitle = bundle.getString("strProductCategory");
			mDefaultProductResponse = bundle.getString("productResponse");
			mFetchFromJson = bundle.getBoolean("fetchFromJson");
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		slideBottomPanel();
		initViews();
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
		getViewDataBinding().imClose.setOnClickListener(this);
		btnSizeSelector.setOnClickListener(this);
		btnColorSelector.setOnClickListener(this);
		this.configureDefaultUI();
	}

	public void configureDefaultUI() {

		getViewDataBinding().tvProductName.setText(mDefaultProduct.productName);
		getViewDataBinding().tvSubCategoryTitle.setText(mSubCategoryTitle);

		if (!TextUtils.isEmpty(mDefaultProduct.saveText)) {
			txtSaveText.setVisibility(View.VISIBLE);
			txtSaveText.setText(mDefaultProduct.saveText);
		}

		try {
			// set price list
			ProductUtils.gridPriceList(txtFromPrice, txtActualPrice, String.valueOf(mDefaultProduct.fromPrice), getViewModel().maxWasPrice(mDefaultProduct.otherSkus));
		} catch (Exception ignored) {
		}

		this.mAuxiliaryImage.add(getImageByWidth(mDefaultProduct.externalImageRef, getActivity()));
		this.mProductViewPagerAdapter = new ProductViewPagerAdapter(getActivity(), this.mAuxiliaryImage, this);
		this.mImageViewPager.setAdapter(mProductViewPagerAdapter);

		//set promotional Images
		if (mDefaultProduct.promotionImages != null)
			loadPromotionalImages(mDefaultProduct.promotionImages);

		//loadProductDetails.
		getViewModel().productDetail(new ProductRequest(mDefaultProduct.productId, mDefaultProduct.sku)).execute();
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
			View view = getLayoutInflater().inflate(R.layout.promotional_image, null);
			SimpleDraweeView simpleDraweeView = view.findViewById(R.id.promotionImage);
			drawImage.displaySmallImage(simpleDraweeView, image);
			promotionalImagesLayout.addView(view);
		}
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.imClose:
				closeSlideUpPanel();
				break;
			case R.id.relColorSelector:
				colorPickerDialog.show();
				break;
			case R.id.relSizeSelector:
				openSizePicker(selectedGroupKey);
				break;
		}
	}

	public void addItemToCart(){

	}

	@Override
	public void renderView() {

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

	}

	@Override
	public void setUpImageViewPager(List<String> auxImages) {

	}

	@Override
	public void defaultProduct() {

	}

	@Override
	public void setProductName() {

	}

	@Override
	public void onLoadStart() {

	}

	@Override
	public void onLoadComplete() {

	}

	@Override
	public void addToShoppingList() {

	}

	@Override
	public String getImageByWidth(String imageUrl, Context context) {
		WindowManager display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
		assert display != null;
		Display deviceHeight = display.getDefaultDisplay();
		Point size = new Point();
		deviceHeight.getSize(size);
		int width = size.x;
		imageUrl = (imageUrl.contains("jpg")) ? "https://images.woolworthsstatic.co.za/" + imageUrl : imageUrl;
		return imageUrl + "" + ((imageUrl.contains("jpg")) ? "" : "?w=" + width + "&q=" + 85);
	}

	@Override
	public List<String> getAuxiliaryImage() {
		return null;
	}

	@Override
	public void onSuccessResponse(ProductDetails productDetails) {
		this.productDetails = productDetails;
		this.otherSKUsByGroupKey = groupOtherSKUsByColor(productDetails.otherSkus);
		this.updateDefaultUI();
	}

	public void updateDefaultUI() {
		this.defaultSku = getDefaultSku(otherSKUsByGroupKey);
		// when there is no size available
		// selectedSKU will be the defaultSKU
			if(!hasSize)
				this.selectedOtherSku = this.defaultSku;
		getViewDataBinding().llLoadingColorSize.setVisibility(View.GONE);
		getViewDataBinding().loadingInfoView.setVisibility(View.GONE);
		this.configureButtonsAndSelectors();
		this.updateViewPagerWithAuxiliaryImages();
		this.setProductCode(productDetails.productId);
		this.setProductDescription(getViewModel().getProductDescription(getActivity(), productDetails));
		this.configureUIForOtherSKU(defaultSku);
	}

	public void configureButtonsAndSelectors(){
		getViewDataBinding().colorSizeLayout.setVisibility((hasColor || hasSize) ? View.VISIBLE : View.GONE);
		btnColorSelector.setEnabled(hasColor);
		btnSizeSelector.setEnabled(hasSize);

		// if colors not available set the color icon to N/A icon , Icons will look like " / "
		if(hasColor){
			this.configureColorPicker();
		}else {
			this.setSelectedColorIcon();
		}

		if(hasSize){
			this.configureSizePicker();
		}else {
			tvSelectedSize.setText("NO SZ");
		}

		btnFindInStore.setVisibility(Boolean.valueOf(productDetails.isnAvailable) ? View.VISIBLE :View.GONE);
		btnAddToCart.setAlpha(1f);
		btnAddToCart.setEnabled(true);
	}

	private void configureColorPicker() {
		View view = getLayoutInflater().inflate(R.layout.color_size_picker_bottom_sheet_dialog, null);
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
		View view = getLayoutInflater().inflate(R.layout.color_size_picker_bottom_sheet_dialog, null);
		WTextView title = view.findViewById(R.id.title);
		rcvSizePicker = view.findViewById(R.id.rvPickerList);
		ImageView closeSizePicker = view.findViewById(R.id.imClosePicker);
		closeSizePicker.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sizePickerDialog.dismiss();
			}
		});
		rcvSizePicker.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		title.setText(getString(R.string.confirm_size_range_desc));
		sizePickerDialog.setContentView(view);
	}

	public void openSizePicker(String groupKey) {
		ArrayList<OtherSkus> selectedOtherSKUsForGroupKey = this.otherSKUsByGroupKey.get(groupKey);
		sizePickerAdapter = new ProductSizePickerAdapter(selectedOtherSKUsForGroupKey, this);
		rcvSizePicker.setAdapter(sizePickerAdapter);
		sizePickerDialog.show();
	}

	private void configureUIForOtherSKU(OtherSkus otherSku) {
		txtFromPrice.setText(otherSku.price);
		if (!TextUtils.isEmpty(otherSku.wasPrice))
			txtActualPrice.setText(otherSku.wasPrice);
		if (hasColor)
			this.setSelectedColorIcon();
	}


	@Override
	public void onFailureResponse(String s) {

	}

	@Override
	public void disableStoreFinder() {

	}

	@Override
	public void responseFailureHandler(Response response) {

	}

	@Override
	public void enableFindInStoreButton(WProductDetail productList) {

	}

	@Override
	public void setLayoutWeight(View v, float weight) {

	}

	@Override
	public void setIngredients(String string) {

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
	public void setSelectedSize(OtherSkus sku) {

	}

	@Override
	public void setPrice(OtherSkus otherSkus) {

	}

	@Override
	public void setAuxiliaryImages(ArrayList<String> auxiliaryImages) {

	}

	@Override
	public void setSelectedTextColor(OtherSkus otherSkus) {

	}

	@Override
	public void removeAllDots() {

	}

	@Override
	public void setupPagerIndicatorDots(int size) {

	}

	@Override
	public void colorSizeContainerVisibility(List<OtherSkus> otherSkuList) {

	}

	@Override
	public void setColorList(List<OtherSkus> skuList) {

	}

	@Override
	public void setSizeList(List<OtherSkus> skuList) {

	}

	@Override
	public void startLocationUpdates() {

	}

	@Override
	public void stopLocationUpdate() {

	}

	@Override
	public void showFindInStoreProgress() {

	}

	@Override
	public void dismissFindInStoreProgress() {

	}

	@Override
	public void onLocationItemSuccess(List<StoreDetails> location) {

	}

	@Override
	public void outOfStockDialog() {

	}

	@Override
	public void onPermissionGranted() {

	}

	@Override
	public void apiIdentifyTokenValidation() {

	}

	@Override
	public void onTokenFailure(String e) {

	}

	@Override
	public void onCartSummarySuccess(CartSummaryResponse cartSummaryResponse) {

	}

	@Override
	public void onAddToCartLoad() {

	}

	@Override
	public void onAddToCartLoadComplete() {

	}

	@Override
	public void apiAddItemToCart() {

	}

	@Override
	public void addItemToCartResponse(AddItemToCartResponse addItemToCartResponse) {

	}

	@Override
	public void otherHttpCode(Response addItemToCartResponse) {

	}

	@Override
	public void onAddItemToCartFailure(String error) {

	}

	@Override
	public void onSessionTokenExpired(Response response) {

	}

	@Override
	public void handleSetSuburbResponse(Object object) {

	}

	@Override
	public void SelectedImage(String otherSkus) {

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
				if (otherSkusObj.sku.equalsIgnoreCase(mDefaultProduct.sku)) {
					this.selectedGroupKey = key;
					return otherSkusObj;
				}
			}
		}
		return null;

	}

	private void updateViewPagerWithAuxiliaryImages() {
		this.mAuxiliaryImage = this.getAuxiliaryImagesByGroupKey(this.selectedGroupKey);
		mProductViewPagerAdapter.updatePagerItems(this.mAuxiliaryImage);
	}

	public List<String> getAuxiliaryImagesByGroupKey(String groupKey) {

		List<String> updatedAuxiliaryImages = new ArrayList<>();
		if (this.productDetails.otherSkus.size() > 0)
			updatedAuxiliaryImages.add(this.otherSKUsByGroupKey.get(groupKey).get(0).externalImageRef);

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
		String	url = this.otherSKUsByGroupKey.get(this.selectedGroupKey).get(0).externalColourRef;
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
		if(!hasSize) {
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
	public void onSizeSelected(OtherSkus selectedSizeSku) {
		this.selectedOtherSku = selectedSizeSku;
		this.tvSelectedSize.setText(this.selectedOtherSku.size);
		this.configureUIForOtherSKU(selectedOtherSku);
		sizePickerDialog.dismiss();
	}
}
