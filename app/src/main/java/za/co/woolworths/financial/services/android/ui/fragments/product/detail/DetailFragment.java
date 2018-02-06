package za.co.woolworths.financial.services.android.ui.fragments.product.detail;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.ProductDetailViewBinding;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.Product;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.MultipleImageActivity;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.activities.bottom_menu.BottomNavigator;
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizeAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ProductUtils;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.CancelableCallback;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FusedLocationSingleton;
import za.co.woolworths.financial.services.android.util.LocationItemTask;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.SimpleDividerItemDecoration;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static za.co.woolworths.financial.services.android.ui.fragments.product.detail.DetailViewModel.CLOTHING_PRODUCT;

public class DetailFragment extends BaseFragment<ProductDetailViewBinding, DetailViewModel> implements DetailNavigator, ProductViewPagerAdapter.MultipleImageInterface, View.OnClickListener, NetworkChangeListener {

	private CompositeDisposable mDisposables = new CompositeDisposable();
	private DetailViewModel detailViewModel;
	private List<String> mAuxiliaryImage = new ArrayList<>();
	private String mSubCategoryTitle;
	private String TAG = this.getClass().getSimpleName();

	private ImageView[] ivArrayDotsPager;
	private PopupWindow mPColourWindow;
	private PopupWindow mPSizeWindow;
	private LocationItemTask mLocationItemTask;

	private boolean mProductHasColour;
	private boolean mProductHasSize;
	private boolean mProductHasOneColour;
	private boolean mProductHasOneSize;
	private String mSkuId;

	private BroadcastReceiver mConnectionBroadcast;
	private ErrorHandlerView mErrorHandlerView;

	@Override
	public DetailViewModel getViewModel() {
		return detailViewModel;
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
		detailViewModel = ViewModelProviders.of(this).get(DetailViewModel.class);
		getViewModel().setNavigator(this);
		mConnectionBroadcast = Utils.connectionBroadCast(getBaseActivity(), this);
		Bundle bundle = this.getArguments();
		if (bundle != null) {
			getViewModel().setDefaultProduct(bundle.getString("strProductList"));
			mSubCategoryTitle = bundle.getString("strProductCategory");
		}

		mDisposables.add(WoolworthsApplication.getInstance()
				.bus()
				.toObservable()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Consumer<Object>() {
					@Override
					public void accept(Object object) throws Exception {
						if (object instanceof DetailFragment) {
							onPermissionGranted();
						} else if (object instanceof ConfirmColorSizeActivity) {
							startLocationUpdates();
						}else  if (object instanceof Product){

						}
					}
				}));
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
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
		getViewDataBinding().btnShopOnlineWoolies.setOnClickListener(this);
		getViewDataBinding().incProductColor.linSize.setOnClickListener(this);
		getViewDataBinding().incProductColor.linColour.setOnClickListener(this);
		getViewDataBinding().llStoreFinder.setOnClickListener(this);
		mErrorHandlerView = new ErrorHandlerView(getBaseActivity());
	}

	@Override
	public void closeView(View view) {
		getBottomNavigator().closeSlideUpPanel();
	}

	@Override
	public void nestedScrollViewHelper() {
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
		setSubCategoryTitle();

		try {
			// set price list
			ProductUtils.gridPriceList(getViewDataBinding().textPrice,
					getViewDataBinding().textActualPrice, String.valueOf(mDefaultProduct.fromPrice),
					getViewModel().maxWasPrice(mDefaultProduct.otherSkus));
		} catch (NullPointerException ex) {
		}

		//set promotional Images
		PromotionImages promotionalImage = mDefaultProduct.promotionImages;
		if (promotionalImage != null) {
			ProductUtils.showPromotionalImages(getViewDataBinding().imSave,
					getViewDataBinding().imReward, getViewDataBinding().imVitality,
					getViewDataBinding().imVitality, mDefaultProduct.promotionImages);
		}

		//set promotional text
		setText(mDefaultProduct.saveText, getViewDataBinding().tvSaveText);

		getViewDataBinding().btnAddShoppingList.setOnClickListener(this);

		getViewModel().getProductDetail(getBaseActivity(), mDefaultProduct.productId, mDefaultProduct.sku);
	}

	private void setSubCategoryTitle() {
		setText(getViewDataBinding().tvSubCategoryTitle, mSubCategoryTitle);
	}

	@Override
	public List<String> getAuxiliaryImage() {
		return getViewModel().getAuxiliaryImage();
	}

	@Override
	public void onSuccessResponse(final WProduct product) {
		Activity activity = getBaseActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					WProductDetail newProductList = product.product;
					enableFindInStoreButton(newProductList);

					List<OtherSkus> otherSkuList = getViewModel().otherSkuList();

					//display ingredient info
					getViewModel().displayIngredient();

					setProductCode(getViewModel().getProductId());

					setProductDescription(getViewModel().getProductDescription(getBaseActivity()));

					// use highest sku as default price
					OtherSkus highestPriceSku = getViewModel().highestSKUPrice(product.product.fromPrice);

					setSelectedSize(highestPriceSku);

					setSelectedTextColor(highestPriceSku);

					setPrice(highestPriceSku);

					colorSizeContainerVisibility(otherSkuList.size());

					//set promotional Images
					ProductUtils.showPromotionalImages(getViewDataBinding().imSave, getViewDataBinding().imReward, getViewDataBinding().imVitality, getViewDataBinding().imVitality, product.product.promotionImages);

					try {
						setAuxiliaryImages(getViewModel().getAuxiliaryImageList(highestPriceSku));

						setColorList(getViewModel().commonColorList(highestPriceSku));

						setSizeList(getViewModel().commonSizeList(highestPriceSku));
					} catch (NullPointerException ignored) {
					}
				}
			});
		}
	}

	@Override
	public void onFailureResponse(String s) {
		mErrorHandlerView.showToast();
	}

	@Override
	public void disableStoreFinder() {
		setLayoutWeight(getViewDataBinding().btnShopOnlineWoolies, 1.0f);
		hideView(getViewDataBinding().llStoreFinder);
	}

	@Override
	public void responseFailureHandler(Response response) {

	}

	@Override
	public void SelectedImage(String image) {
		Activity activity = getBaseActivity();
		if (activity != null) {
			Intent openMultipleImage = new Intent(getActivity(), MultipleImageActivity.class);
			openMultipleImage.putExtra("auxiliaryImages", image);
			startActivity(openMultipleImage);
			activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}
	}

	@Override
	public void setProductName() {
		setText(getViewDataBinding().tvProductName, getViewModel().getDefaultProduct().productName);
	}

	@Override
	public void onLoadStart() {
		disableStoreFinder();

		//disable shop online button
		getViewDataBinding().btnShopOnlineWoolies.setAlpha(0.55f);
		getViewDataBinding().btnShopOnlineWoolies.setEnabled(false);

		// hide color and size view
		hideView(getViewDataBinding().incProductColor.linProductContainer);
		hideView(getViewDataBinding().incProductColorBottomLine);

		// hide ingredient
		hideView(getViewDataBinding().ingredientLine);
		hideView(getViewDataBinding().linIngredient);

		// load product info
		setText(getViewDataBinding().productCode, getString(R.string.loading_product_info));
	}

	@Override
	public void onLoadComplete() {
		showView(getViewDataBinding().btnShopOnlineWoolies);
		hideView(getViewDataBinding().productLoadDot);
	}

	@Override
	public void addToShoppingList() {
		Utils.addToShoppingCart(getActivity(), new ShoppingList(
				getViewModel().getDefaultProduct().productId,
				getViewModel().getDefaultProduct().productName, false));
		Utils.displayValidationMessage(getActivity(),
				CustomPopUpWindow.MODAL_LAYOUT.SHOPPING_LIST_INFO,
				"viewShoppingList");
	}

	@Override
	public String getImageByWidth(String imageUrl, Context context) {
		WindowManager display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
		assert display != null;
		Display deviceHeight = display.getDefaultDisplay();
		Point size = new Point();
		deviceHeight.getSize(size);
		int width = size.x;
		return imageUrl + "?w=" + width + "&q=" + 85;
	}

	@Override
	public void onClick(View view) {
		MultiClickPreventer.preventMultiClick(view);
		switch (view.getId()) {
			case R.id.imClose:
				closeView(view);
				break;

			case R.id.btnAddShoppingList:
				addToShoppingList();
				break;

			case R.id.btnShopOnlineWoolies:
				String checkoutLink = getViewModel().getCheckOutLink();
				if (!isEmpty(checkoutLink)) {
					Utils.openExternalLink(getBaseActivity(), Utils.addUTMCode(checkoutLink));
				}
				break;

			case R.id.linColour:
				cancelPopWindow(mPSizeWindow);
				mPColourWindow.showAsDropDown(getViewDataBinding().incProductColor.textSelectColour, -50, -180);
				break;

			case R.id.linSize:
				cancelPopWindow(mPColourWindow);
				mPSizeWindow.showAsDropDown(getViewDataBinding().incProductColor.textSelectSize, -50, -180);
				break;

			case R.id.llStoreFinder:
				getViewDataBinding().scrollProductDetail.scrollTo(0, 0);
				Activity activity = getBaseActivity();
				if (activity != null) {
					if (Utils.isLocationEnabled(getActivity())) {
						BottomNavigator bottomNavigator = getBottomNavigator();
						bottomNavigator.getRuntimePermission().check_permission(bottomNavigator.getPermissionType(android.Manifest.permission.ACCESS_FINE_LOCATION), "Explain here why the app needs permissions", 1);
					} else {
						Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.LOCATION_OFF, "");
					}
				}
				break;
			default:
				break;
		}
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
		setText(getViewDataBinding().productCode, getString(R.string.product_code) + ": " + productCode);
	}

	@Override
	public void setProductDescription(String productDescription) {
		getViewDataBinding().webDescription.loadDataWithBaseURL("file:///android_res/drawable/",
				productDescription, "text/html; charset=UTF-8", "UTF-8", null);
	}

	@Override
	public void setSelectedSize(OtherSkus sku) {
		String size = sku.size;
		getGlobalState().setSizePickerSku(sku);
		WTextView tvTextSize = getViewDataBinding().incProductColor.textProductSize;
		setText(tvTextSize, size);
		tvTextSize.setTextColor(Color.BLACK);
	}

	@Override
	public void setPrice(OtherSkus otherSkus) {
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
						setText(tvPrice, WFormatter.formatAmount(price));
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

	@Override
	public void setAuxiliaryImages(ArrayList<String> auxiliaryImages) {
		mAuxiliaryImage = auxiliaryImages;
		getViewModel().setAuxiliaryImage(mAuxiliaryImage);
		setUpImageViewPager();
		setupPagerIndicatorDots(auxiliaryImages.size());
	}

	@Override
	public void setSelectedTextColor(OtherSkus otherSkus) {
		WTextView tvColour = getViewDataBinding().incProductColor.textColour;
		selectedColor(otherSkus.externalColourRef);
		if (!isEmpty(otherSkus.colour)) {
			setText(tvColour, otherSkus.colour);
		} else {
			setText(tvColour, getString(R.string.product_colour));
		}
	}

	@Override
	public void removeAllDots() {
		getViewDataBinding().pagerDots.removeAllViews();
	}

	@Override
	public void enableFindInStoreButton(WProductDetail productList) {
		try {
			LinearLayout llStoreFinder = getViewDataBinding().llStoreFinder;
			WButton btnShopOnline = getViewDataBinding().btnShopOnlineWoolies;
			String productType = productList.productType;
			WGlobalState mcs = WoolworthsApplication.getInstance().getWGlobalState();
			if ((productType.equalsIgnoreCase("clothingProducts") & mcs.clothingIsEnabled()) || (productType.equalsIgnoreCase("foodProducts") & mcs.isFoodProducts())) {
				btnShopOnline.setAlpha(1f);
				btnShopOnline.setEnabled(true);
				setLayoutWeight(btnShopOnline, 0.5f);
				setLayoutWeight(llStoreFinder, 0.5f);
				showView(llStoreFinder);
			} else {
				setLayoutWeight(btnShopOnline, 1f);
				hideView(llStoreFinder);
			}
		} catch (IllegalStateException ex) {
			Log.d(TAG, ex.toString());
		}
	}

	private void selectedColor(String url) {
		SimpleDraweeView mImSelectedColor = getViewDataBinding().incProductColor.imSelectedColor;
		if (TextUtils.isEmpty(url)) {
			mImSelectedColor.setImageAlpha(0);
		} else {
			mImSelectedColor.setImageAlpha(255);
			DrawImage drawImage = new DrawImage(getActivity());
			drawImage.displayImage(mImSelectedColor, url);
		}
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
	public void colorSizeContainerVisibility(int size) {
		if (size > 0 && getViewModel().getProductType().equalsIgnoreCase(CLOTHING_PRODUCT)) {
			showView(getViewDataBinding().incProductColor.linProductContainer);
			hideView(getViewDataBinding().llLoadingColorSize);
		} else {
			hideView(getViewDataBinding().incProductColor.linProductContainer);
			hideView(getViewDataBinding().llLoadingColorSize);
		}
	}

	@Override
	public void setColorList(List<OtherSkus> skuList) {
		LayoutInflater layoutInflater = (LayoutInflater) getBaseActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		assert layoutInflater != null;
		View mpw = layoutInflater.inflate(R.layout.product_size_row, null);
		ProductColorAdapter mProductColorAdapter = new ProductColorAdapter(skuList, this);
		RecyclerView rlSizeList = mpw.findViewById(R.id.rclSize);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rlSizeList.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
		rlSizeList.setLayoutManager(mLayoutManager);
		rlSizeList.setAdapter(mProductColorAdapter);
		mPColourWindow = new PopupWindow(
				mpw,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, false);
		mPColourWindow.setTouchable(true);

	}

	@Override
	public void setSizeList(List<OtherSkus> skuList) {
		LayoutInflater layoutInflater = (LayoutInflater) getBaseActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		assert layoutInflater != null;
		View mpw = layoutInflater.inflate(R.layout.product_size_row, null);
		ProductSizeAdapter mProductSizeAdapter = new ProductSizeAdapter(skuList, this);
		RecyclerView rlSizeList = mpw.findViewById(R.id.rclSize);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rlSizeList.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
		rlSizeList.setLayoutManager(mLayoutManager);
		rlSizeList.setAdapter(mProductSizeAdapter);
		mPSizeWindow = new PopupWindow(
				mpw,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, false);
		mPSizeWindow.setTouchable(true);
	}

	@Override
	public void onSizeItemClicked(OtherSkus sku) {
		WTextView tvSize = getViewDataBinding().incProductColor.textProductSize;
		cancelPopWindow(mPSizeWindow);
		setSelectedSize(sku);
		getGlobalState().setSizeWasPopup(true);
		setPrice(getViewModel().updatePrice(sku, tvSize.getText().toString()));
	}

	@Override
	public void onColourItemClicked(OtherSkus otherSkus) {
		WTextView tvSize = getViewDataBinding().incProductColor.textProductSize;
		cancelPopWindow(mPColourWindow);
		setSelectedTextColor(otherSkus);
		setSizeList(getViewModel().commonSizeList(otherSkus));
		setAuxiliaryImages(getViewModel().getAuxiliaryImageList(otherSkus));
		getGlobalState().setColorWasPopup(true);
		getGlobalState().setColorPickerSku(otherSkus);
		setPrice(getViewModel().updatePrice(otherSkus, tvSize.getText().toString()));
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mDisposables != null && !mDisposables.isDisposed())
			mDisposables.clear();
		getGlobalState().setColorWasPopup(false);
		getGlobalState().setColorPickerSku(null);
		getGlobalState().setSizeWasPopup(false);
		getGlobalState().setSizePickerSku(null);
		CancelableCallback.cancelAll();
		cancelRequest(mLocationItemTask);
	}

	private void cancelPopWindow(PopupWindow popupWindow) {
		if (popupWindow != null) {
			if (popupWindow.isShowing()) {
				popupWindow.dismiss();
			}
		}
	}

	/*****************************
	 * FIND IN STORE SECTION
	 * ***************
	 */

	private BroadcastReceiver mLocationUpdated = new BroadcastReceiver() {
		@RequiresApi(api = Build.VERSION_CODES.M)
		@Override
		public void onReceive(Context context, final Intent intent) {
			try {
				Location mLocation = intent.getParcelableExtra(FusedLocationSingleton.LBM_EVENT_LOCATION_UPDATE);
				Utils.saveLastLocation(mLocation, getBaseActivity());
				stopLocationUpdate();
				executeLocationItemTask();
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	};

	private void executeLocationItemTask() {
		mLocationItemTask = getViewModel().locationItemTask(getBaseActivity());
		mLocationItemTask.execute();
	}

	@Override
	public void startLocationUpdates() {
		showFindInStoreProgress();
		FusedLocationSingleton.getInstance().startLocationUpdates();
		// register observer for location updates
		LocalBroadcastManager.getInstance(getBaseActivity()).registerReceiver(mLocationUpdated,
				new IntentFilter(FusedLocationSingleton.INTENT_FILTER_LOCATION_UPDATE));
	}

	@Override
	public void stopLocationUpdate() {
		// stop location updates
		FusedLocationSingleton.getInstance().stopLocationUpdates();
		// unregister observer
		LocalBroadcastManager.getInstance(getBaseActivity()).unregisterReceiver(mLocationUpdated);
	}

	@Override
	public void showFindInStoreProgress() {
		getViewDataBinding().llStoreFinder.setEnabled(false);
		hideView(getViewDataBinding().tvBtnFinder);
		showView(getViewDataBinding().mButtonProgress);
	}

	@Override
	public void dismissFindInStoreProgress() {
		getBaseActivity().runOnUiThread(new Runnable() {
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
		Utils.removeObjectFromArrayList(getBaseActivity(), location);
		if (location.size() > 0) {
			getGlobalState().setStoreDetailsArrayList(location);
			Intent intentInStoreFinder = new Intent(getBaseActivity(), WStockFinderActivity.class);
			intentInStoreFinder.putExtra("PRODUCT_NAME", mSubCategoryTitle);
			startActivity(intentInStoreFinder);
			getBaseActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
		} else {
			noStockAvailable();
		}
	}

	@Override
	public void noStockAvailable() {
		//no stock error message
		Utils.displayValidationMessage(getBaseActivity(), CustomPopUpWindow.MODAL_LAYOUT.NO_STOCK, "");
	}

	@Override
	public void onPermissionGranted() {
		getViewDataBinding().scrollProductDetail.scrollTo(0, 0);
		if (isNetworkConnected()) {
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
						mSkuId = getGlobalState().getColorPickerSku().sku;
						break;
					case 2:
						mSkuId = getGlobalState().getSizePickerSku().sku;
						break;
					default:
						break;
				}
				noSizeColorIntent();
			}
		}
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
							mSkuId = getSize.get(0).sku;
							noSizeColorIntent();
						} else {
							sizeIntent(skuColour);
						}
					} else {
						mSkuId = getViewModel().getDefaultProduct().sku;
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
						mSkuId = getSize.get(0).sku;
						noSizeColorIntent();
					} else { // more sizes
						sizeIntent();
					}
				} else {
					mSkuId = getViewModel().getDefaultProduct().sku;
					noSizeColorIntent();
				}
			}
		} catch (Exception ignored) {
		}
	}

	public void noSizeColorIntent() {
		getViewDataBinding().scrollProductDetail.scrollTo(0, 0);
		getGlobalState().setSelectedSKUId(mSkuId);
		startLocationUpdates();
	}

	public void sizeIntent() {
		getGlobalState().setColourSKUArrayList(getColorList());
		Intent mIntent = new Intent(getBaseActivity(), ConfirmColorSizeActivity.class);
		mIntent.putExtra("COLOR_LIST", toJson(getColorList()));
		mIntent.putExtra("OTHERSKU", toJson(getViewModel().otherSkuList()));
		mIntent.putExtra("PRODUCT_HAS_COLOR", false);
		mIntent.putExtra("PRODUCT_HAS_SIZE", true);
		mIntent.putExtra("PRODUCT_NAME", getViewModel().getDefaultProduct().productName);
		startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
		getBaseActivity().overridePendingTransition(0, 0);
	}

	public void sizeIntent(String colour) {
		getGlobalState().setColourSKUArrayList(getColorList());
		Intent mIntent = new Intent(getBaseActivity(), ConfirmColorSizeActivity.class);
		mIntent.putExtra("SELECTED_COLOUR", colour);
		mIntent.putExtra("OTHERSKU", toJson(getViewModel().otherSkuList()));
		mIntent.putExtra("PRODUCT_HAS_COLOR", false);
		mIntent.putExtra("PRODUCT_HAS_SIZE", true);
		mIntent.putExtra("PRODUCT_NAME", getViewModel().getDefaultProduct().productName);
		startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
		getBaseActivity().overridePendingTransition(0, 0);
	}

	public void colourIntent() {
		getGlobalState().setColourSKUArrayList(getColorList());
		Intent mIntent = new Intent(getBaseActivity(), ConfirmColorSizeActivity.class);
		mIntent.putExtra("COLOR_LIST", toJson(getColorList()));
		mIntent.putExtra("OTHERSKU", toJson(getViewModel().otherSkuList()));
		mIntent.putExtra("PRODUCT_HAS_COLOR", true);
		mIntent.putExtra("PRODUCT_HAS_SIZE", true);
		mIntent.putExtra("PRODUCT_NAME", getViewModel().getDefaultProduct().productName);
		startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
		getBaseActivity().overridePendingTransition(0, 0);
	}

	public void colorIntent(String size) {
		Intent mIntent = new Intent(getBaseActivity(), ConfirmColorSizeActivity.class);
		mIntent.putExtra("SELECTED_COLOUR", size);
		mIntent.putExtra("COLOR_LIST", toJson(getGlobalState().getColourSKUArrayList()));
		mIntent.putExtra("OTHERSKU", toJson(getViewModel().otherSkuList()));
		mIntent.putExtra("PRODUCT_HAS_COLOR", true);
		mIntent.putExtra("PRODUCT_HAS_SIZE", false);
		mIntent.putExtra("PRODUCT_NAME", getViewModel().getDefaultProduct().productName);
		startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
		getBaseActivity().overridePendingTransition(0, 0);
	}

	private void sizeOnlyIntent(OtherSkus otherSku) {
		ArrayList<OtherSkus> sizeList = getViewModel().commonSizeList(otherSku);
		int sizeListSize = sizeList.size();
		if (sizeListSize > 0) {
			if (sizeListSize == 1) {
				// one size only
				mSkuId = sizeList.get(0).sku;
				noSizeColorIntent();
			} else {
				// size > 1
				sizeIntent(otherSku.colour);
			}
		} else {
			// no size
			mSkuId = otherSku.sku;
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
					mSkuId = colorList.get(0).sku;
					noSizeColorIntent();
				} else {
					// color > 1
					getGlobalState().setColourSKUArrayList(colorList);
					colorIntent(otherSku.size);
				}
			} else {
				// no color
				mSkuId = otherSku.sku;
				noSizeColorIntent();
			}
		} else {
			mSkuId = otherSku.sku;
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
		Activity activity = getBaseActivity();
		if (activity != null) {
			getBaseActivity().unregisterReceiver(mConnectionBroadcast);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver();
	}

	private void registerReceiver() {
		Activity activity = getBaseActivity();
		if (activity != null) {
			activity.registerReceiver(mConnectionBroadcast,
					new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		}
	}

	@Override
	public void onConnectionChanged() {
		Activity activity = getBaseActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					boolean productLoadFail = getViewModel().productLoadFail();
					boolean findInStoreLoadFail = getViewModel().findInStoreLoadFail();
					if (isNetworkConnected() && (productLoadFail || findInStoreLoadFail)) {
						if (productLoadFail) {
							ProductList defaultProduct = getViewModel().getDefaultProduct();
							getViewModel().getProductDetail(getBaseActivity(), defaultProduct.productId, defaultProduct.sku);
							return;
						}
						if (findInStoreLoadFail) {
							executeLocationItemTask();
						}
					}
				}
			});
		}
	}
}

