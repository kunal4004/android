package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizeAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.LoadingDots;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentViewPager;
import za.co.woolworths.financial.services.android.ui.views.WrapContentWebView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.SimpleDividerItemDecoration;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class WProductDetailActivity extends AppCompatActivity implements
		SelectedProductView,
		ProductViewPagerAdapter.MultipleImageInterface, View.OnClickListener {

	public WTextView mTextSelectSize;
	public RecyclerView mRecyclerviewSize;
	private ArrayList<WProductDetail> mProductDetail;
	private WTextView mTextTitle;
	private WTextView mTextPrice;
	private LinearLayout llColorSizeContainer;
	private WTextView mProductCode;
	public List<OtherSku> otherSkusList;
	public WTextView mTextSelectColor;
	public PopupWindow mPColourWindow;
	public PopupWindow mPSizeWindow;
	private boolean productIsColored = true;
	private ArrayList<OtherSku> uniqueColorList;
	public RecyclerView mColorRecycleSize;
	public ArrayList<OtherSku> uniqueSizeList;
	public ViewPager mViewPagerProduct;
	public ImageView mImCloseProduct;
	public RelativeLayout mLinSize;
	public SimpleDraweeView mImNewImage;
	public SimpleDraweeView mImSave;
	public SimpleDraweeView mImReward;
	public SimpleDraweeView mVitalityView;
	public String mCheckOutLink;
	private ArrayList<String> mAuxiliaryImages;
	private LinearLayout mLlPagerDots;
	private ImageView[] ivArrayDotsPager;
	private SimpleDraweeView mImSelectedColor;
	private View vColorSizeHorizontalLine;
	private WTextView mTextPromo;
	private WTextView mTextActualPrice;
	private WTextView mTextColour;
	private WrapContentWebView mWebDescription;
	private WButton mBtnAddShoppingList;
	private WTextView mIngredientList;
	private LinearLayout mLinIngredient;
	private View ingredientLine;
	public String mProductJSON;
	public NestedScrollView mScrollProductDetail;
	private int mPreviousState;
	private ViewPager mTouchTarget;
	private WProductDetail mObjProductDetail;
	private String mDefaultImage;
	public final int IMAGE_QUALITY = 85;
	public int mPosition;
	public ProductList mSelectedProduct;
	public WButton mBtnShopOnlineWoolies;
	public ProductColorAdapter mProductColourAdapter;
	private ProductSizeAdapter mProductSizeAdapter;
	private ProgressBar mSizeProgressBar;
	private ProductViewPagerAdapter mProductViewPagerAdapter;
	private String currentSKUId;
	private OtherSku mDefaultSKUModel;
	private ImageView mImColorArrow;
	private ImageView mColorArrow;
	private WTextView mTextProductSize;
	private LoadingDots mLoadingDaot;
	private int mOtherSkuSize;
	private LinearLayout llLoadingColorSize;
	private View loadingColorDivider;
	private RelativeLayout mLinColor;

	protected void initProductDetailUI() {
		mScrollProductDetail = (NestedScrollView) findViewById(R.id.scrollProductDetail);
		mSizeProgressBar = (ProgressBar) findViewById(R.id.mWoolworthsProgressBar);
		vColorSizeHorizontalLine = findViewById(R.id.colorView);
		mTextSelectSize = (WTextView) findViewById(R.id.textSelectSize);
		mTextColour = (WTextView) findViewById(R.id.textColour);
		mTextProductSize = (WTextView) findViewById(R.id.textProductSize);
		mTextTitle = (WTextView) findViewById(R.id.textTitle);
		mTextActualPrice = (WTextView) findViewById(R.id.textActualPrice);
		mViewPagerProduct = (ViewPager) findViewById(R.id.mProductDetailPager);
		mTextPrice = (WTextView) findViewById(R.id.textPrice);
		mLinIngredient = (LinearLayout) findViewById(R.id.linIngredient);
		mIngredientList = (WTextView) findViewById(R.id.ingredientList);
		mTextPromo = (WTextView) findViewById(R.id.textPromo);
		mTextSelectColor = (WTextView) findViewById(R.id.textSelectColour);
		mProductCode = (WTextView) findViewById(R.id.product_code);
		llColorSizeContainer = (LinearLayout) findViewById(R.id.linProductContainer);
		mLinColor = (RelativeLayout) findViewById(R.id.linColour);
		mLinSize = (RelativeLayout) findViewById(R.id.linSize);
		mBtnAddShoppingList = (WButton) findViewById(R.id.btnAddShoppingList);
		mBtnShopOnlineWoolies = (WButton) findViewById(R.id.btnShopOnlineWoolies);
		mColorArrow = (ImageView) findViewById(R.id.mColorArrow);
		mImCloseProduct = (ImageView) findViewById(R.id.imCloseProduct);
		mImSelectedColor = (SimpleDraweeView) findViewById(R.id.imSelectedColor);
		mLlPagerDots = (LinearLayout) findViewById(R.id.pager_dots);
		llLoadingColorSize = (LinearLayout) findViewById(R.id.llLoadingColorSize);
		loadingColorDivider = findViewById(R.id.loadingColorDivider);
		mImColorArrow = (ImageView) findViewById(R.id.imColorArrow);
		mWebDescription = (WrapContentWebView) findViewById(R.id.webDescription);
		ingredientLine = findViewById(R.id.ingredientLine);
		mImNewImage = (SimpleDraweeView) findViewById(R.id.imNewImage);
		mImSave = (SimpleDraweeView) findViewById(R.id.imSave);
		mImReward = (SimpleDraweeView) findViewById(R.id.imReward);
		mVitalityView = (SimpleDraweeView) findViewById(R.id.imVitality);
		mLoadingDaot = (LoadingDots) findViewById(R.id.loadingDots);
		mTextSelectColor.setOnClickListener(this);
		mTextSelectSize.setOnClickListener(this);
		mImColorArrow.setOnClickListener(this);
		mImSelectedColor.setOnClickListener(this);
		mColorArrow.setOnClickListener(this);
		mTextProductSize.setOnClickListener(this);
		mImCloseProduct.setOnClickListener(this);
		mLinColor.setOnClickListener(this);
		mLinSize.setOnClickListener(this);
	}

	protected void displayProductDetail(String mProductList, String skuId, int otherSkuSize) {
		this.currentSKUId = skuId;
		this.mOtherSkuSize = otherSkuSize;
		try {
			SessionDao sessionDao = new SessionDao(WProductDetailActivity.this,
					SessionDao.KEY.STORES_LATEST_PAYLOAD).get();
			mProductJSON = sessionDao.value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		mProductDetail = new Gson().fromJson(mProductList, new TypeToken<List<WProductDetail>>() {
		}.getType());
		assert mProductDetail != null;
		WProductDetail mProductDetail = this.mProductDetail.get(0);
		otherSkusList = mProductDetail.otherSkus;
		mCheckOutLink = mProductDetail.checkOutLink;
		//update default image
		mDefaultImage = getImageByWidth(mProductDetail.externalImageRef);
		setPromotionText(mProductDetail.saveText);
		getHtmlData();
		mDefaultSKUModel = getDefaultSKUModel();
		updateHeroImage();
		updatePrice();
		mScrollProductDetail.scrollTo(0, 0);
	}

	protected void getHtmlData() {
		mObjProductDetail = mProductDetail.get(0);

		String head = "<head>" +
				"<meta charset=\"UTF-8\">" +
				"<style>" +
				"@font-face {font-family: 'myriad-pro-regular';src: url('file://"
				+ this.getFilesDir().getAbsolutePath() + "/fonts/MyriadPro-Regular.otf');}" +
				"body {" +
				"line-height: 110%;" +
				"font-size: 92% !important;" +
				"text-align: justify;" +
				"color:grey;" +
				"font-family:'myriad-pro-regular';}" +
				"</style>" +
				"</head>";

		String descriptionWithoutExtraTag = "";
		if (!TextUtils.isEmpty(mObjProductDetail.longDescription)) {
			descriptionWithoutExtraTag = mObjProductDetail.longDescription
					.replaceAll("</ul>\n\n<ul>\n", " ")
					.replaceAll("<p>&nbsp;</p>", "")
					.replaceAll("<ul><p>&nbsp;</p></ul>", " ");
		}

		String htmlData = "<!DOCTYPE html><html>"
				+ head
				+ "<body>"
				+ isEmpty(descriptionWithoutExtraTag)
				+ "</body></html>";

		mWebDescription.loadDataWithBaseURL("file:///android_res/drawable/",
				htmlData,
				"text/html; charset=UTF-8", "UTF-8", null);
		mTextTitle.setText(Html.fromHtml(isEmpty(mObjProductDetail.productName)));
		mProductCode.setText(getString(R.string.product_code) + ": " + mObjProductDetail.productId);
	}

	public void productPriceList(WTextView wPrice, WTextView WwasPrice,
								 String price, String wasPrice, String productType) {
		switch (productType) {
			case "clothingProducts":
				vColorSizeHorizontalLine.setVisibility(View.VISIBLE);
				llColorSizeContainer.setVisibility(View.VISIBLE);
				if (TextUtils.isEmpty(wasPrice)) {
					wPrice.setText(WFormatter.formatAmount(price));
					wPrice.setPaintFlags(0);
					WwasPrice.setText("");
				} else {
					if (wasPrice.equalsIgnoreCase(price)) {
						//wasPrice equals currentPrice
						wPrice.setText(WFormatter.formatAmount(price));
						WwasPrice.setText("");
						wPrice.setPaintFlags(0);
					} else {
						wPrice.setText(WFormatter.formatAmount(wasPrice));
						wPrice.setPaintFlags(wPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
						WwasPrice.setText(WFormatter.formatAmount(price));
					}
				}
				break;
			default:
				vColorSizeHorizontalLine.setVisibility(View.GONE);
				llColorSizeContainer.setVisibility(View.GONE);
				if (TextUtils.isEmpty(wasPrice)) {
					wPrice.setText(WFormatter.formatAmount(price));
					wPrice.setPaintFlags(0);
					WwasPrice.setText("");
				} else {
					if (wasPrice.equalsIgnoreCase(price)) { //wasPrice equals currentPrice
						wPrice.setText(WFormatter.formatAmount(price));
						WwasPrice.setText("");
					} else {
						wPrice.setText(WFormatter.formatAmount(wasPrice));
						wPrice.setPaintFlags(wPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
						WwasPrice.setText(WFormatter.formatAmount(price));
					}
				}
				break;
		}
	}

	protected String isEmpty(String value) {
		if (TextUtils.isEmpty(value)) {
			return "";
		} else {
			return value;
		}
	}

	protected void addButtonEvent() {
		mBtnAddShoppingList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.addToShoppingCart(WProductDetailActivity.this, new ShoppingList(
						mSelectedProduct.productId,
						mSelectedProduct.productName, false));
				Utils.displayValidationMessage(WProductDetailActivity.this,
						CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.SHOPPING_LIST_INFO,
						"viewShoppingList");
			}
		});

		mBtnShopOnlineWoolies.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(mCheckOutLink)) {
					Utils.openExternalLink(WProductDetailActivity.this, Utils.addUTMCode(mCheckOutLink));
				}
			}
		});
	}

	protected void setIngredients(String ingredients) {
		if (TextUtils.isEmpty(ingredients)) {
			mLinIngredient.setVisibility(View.GONE);
			ingredientLine.setVisibility(View.GONE);
		} else {
			mIngredientList.setText(ingredients);
			mLinIngredient.setVisibility(View.VISIBLE);
			ingredientLine.setVisibility(View.VISIBLE);
		}
	}

	protected void colorParams(int position) {
		mPosition = position;
		String colour = uniqueColorList.get(position).colour;
		String defaultUrl = uniqueColorList.get(position).externalColourRef;
		if (TextUtils.isEmpty(colour)) {
			colour = getString(R.string.product_colour);
		}
		mTextColour.setText(colour);
		mAuxiliaryImages = null;
		mAuxiliaryImages = new ArrayList<>();
		mDefaultImage = getSkuExternalImageRef(colour);
		//show default image when imageUrl is empty
		selectedColor(defaultUrl);
		getSKUDefaultSize(colour);
		String size = mTextSelectSize.getText().toString();
		String price = updatePrice(colour, size);
		String wasPrice = updateWasPrice(colour, size);
		retrieveJson(colour);
		if (!TextUtils.isEmpty(price)) {
			productDetailPriceList(mTextPrice, mTextActualPrice,
					price, wasPrice, mObjProductDetail.productType);
		}
	}

	protected void selectedColor(String url) {
		if (TextUtils.isEmpty(url)) {
			mImSelectedColor.setImageAlpha(0);
		} else {
			mImSelectedColor.setImageAlpha(255);
			DrawImage drawImage = new DrawImage(this);
			drawImage.displayImage(mImSelectedColor, url);
		}
	}

	protected void retrieveJson(String colour) {
		JSONObject jsProduct;
		if (mAuxiliaryImages != null) {
			mAuxiliaryImages.clear();
		}
		try {
			// Instantiate a JSON object from the request response
			jsProduct = new JSONObject(mProductJSON);
			String mProduct = jsProduct.getString("product");
			JSONObject jsProductList = new JSONObject(mProduct);
			if (jsProductList.has("ingredients")) {
				setIngredients(jsProductList.getString("ingredients"));
			} else {
				setIngredients("");
			}

			//display default image
			if (mAuxiliaryImages != null) {
				if (!TextUtils.isEmpty(mDefaultImage))
					mAuxiliaryImages.add(0, mDefaultImage);
			}

			String auxiliaryImages = jsProductList.getString("auxiliaryImages");
			JSONObject jsAuxiliaryImages = new JSONObject(auxiliaryImages);
			Iterator<String> keysIterator = jsAuxiliaryImages.keys();
			colour = colour.replace(" ", "");
			while (keysIterator.hasNext()) {
				String keyStr = keysIterator.next();
				if (keyStr.toLowerCase().contains(colour.toLowerCase())) {
					String valueStr = jsAuxiliaryImages.getString(keyStr);
					JSONObject jsonObject = new JSONObject(valueStr);
					if (jsonObject.has("externalImageRef")) {
						mAuxiliaryImages.add(getImageByWidth(jsonObject.getString("externalImageRef")));
					}
				}
			}

			Set<String> removeAuxiliaryImageDuplicate = new LinkedHashSet<>(mAuxiliaryImages);
			mAuxiliaryImages.clear();
			mAuxiliaryImages.addAll(removeAuxiliaryImageDuplicate);

			mProductViewPagerAdapter = new ProductViewPagerAdapter(this, mAuxiliaryImages, this);
			mViewPagerProduct.setAdapter(mProductViewPagerAdapter);
			mProductViewPagerAdapter.notifyDataSetChanged();
			setupPagerIndicatorDots();
			mViewPagerProduct.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
					// All of this is to inhibit any scrollable container from consuming our touch events as the user is changing pages
					if (mPreviousState == ViewPager.SCROLL_STATE_IDLE) {
						if (state == ViewPager.SCROLL_STATE_DRAGGING) {
							mTouchTarget = mViewPagerProduct;
						}
					} else {
						if (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_SETTLING) {
							mTouchTarget = null;
						}
					}

					mPreviousState = state;
				}
			});

		} catch (JSONException ignored) {
		}
	}

	protected void selectedProduct(int position) {
		// clearAdapter();
		if (productIsColored) {
			if (mPColourWindow != null) {
				if (mPColourWindow.isShowing()) {
					mPColourWindow.dismiss();
				}
			}
			colorParams(position);
		} else {
			if (mPSizeWindow != null) {
				if (mPSizeWindow.isShowing()) {
					mPSizeWindow.dismiss();
				}
			}
			if (uniqueSizeList.size() > 0) {
				String selectedSize = uniqueSizeList.get(position).size;
				mTextSelectSize.setText(selectedSize);
				mTextSelectSize.setTextColor(Color.BLACK);
				String colour = mTextColour.getText().toString();
				String price = updatePrice(colour, selectedSize);
				String wasPrice = updateWasPrice(colour, selectedSize);
				retrieveJson(colour);
				if (!TextUtils.isEmpty(price)) {
					productDetailPriceList(mTextPrice, mTextActualPrice,
							price, wasPrice, mObjProductDetail.productType);
				}
			}
		}
	}

	protected void bindWithUI(List<OtherSku> otherSkus, boolean productIsColored) {
		this.productIsColored = productIsColored;
		LinearLayoutManager mSlideUpPanelLayoutManager = new LinearLayoutManager(this);
		mSlideUpPanelLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		if (!productIsColored) {
			//sort ascending
			Collections.sort(otherSkus, new Comparator<OtherSku>() {
				@Override
				public int compare(OtherSku lhs, OtherSku rhs) {
					return lhs.size.compareToIgnoreCase(rhs.size);
				}
			});
			//remove duplicates
			uniqueSizeList = new ArrayList<>();
			for (OtherSku os : otherSkus) {
				if (!sizeValueExist(uniqueSizeList, os.size)) {
					uniqueSizeList.add(os);
				}
			}
			mProductSizeAdapter = new ProductSizeAdapter(uniqueSizeList, this);
			mRecyclerviewSize.addItemDecoration(new SimpleDividerItemDecoration(this));
			mRecyclerviewSize.setLayoutManager(new LinearLayoutManager(this));
			mRecyclerviewSize.setNestedScrollingEnabled(false);
			mRecyclerviewSize.setAdapter(mProductSizeAdapter);
			mProductSizeAdapter.notifyDataSetChanged();
		} else {
			if (otherSkus != null) {
				//sort ascending
				Collections.sort(otherSkus, new Comparator<OtherSku>() {
					@Override
					public int compare(OtherSku lhs, OtherSku rhs) {
						return lhs.colour.compareToIgnoreCase(rhs.colour);
					}
				});
				//remove duplicates
				uniqueColorList = new ArrayList<>();
				if (uniqueColorList.size() > 0) {
					uniqueColorList.clear();
				}
				for (OtherSku os : otherSkus) {
					if (!colourValueExist(uniqueColorList, os.colour)) {
						uniqueColorList.add(os);
					}
				}
				mProductColourAdapter = new ProductColorAdapter(uniqueColorList, this);
				mColorRecycleSize.addItemDecoration(new SimpleDividerItemDecoration(this));
				mColorRecycleSize.setLayoutManager(new LinearLayoutManager(this));
				mColorRecycleSize.setNestedScrollingEnabled(false);
				mColorRecycleSize.setAdapter(mProductColourAdapter);
				mProductColourAdapter.notifyDataSetChanged();
			}
		}
	}

	protected void setupPagerIndicatorDots() {
		ivArrayDotsPager = null;
		mLlPagerDots.removeAllViews();
		if (mAuxiliaryImages.size() > 1) {
			ivArrayDotsPager = new ImageView[mAuxiliaryImages.size()];
			for (int i = 0; i < ivArrayDotsPager.length; i++) {
				ivArrayDotsPager[i] = new ImageView(this);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				params.setMargins(10, 0, 10, 0);
				ivArrayDotsPager[i].setLayoutParams(params);
				ivArrayDotsPager[i].setImageResource(R.drawable.unselected_drawable);
				ivArrayDotsPager[i].setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						view.setAlpha(1);
					}
				});
				mLlPagerDots.addView(ivArrayDotsPager[i]);
				mLlPagerDots.bringToFront();
			}
			ivArrayDotsPager[0].setImageResource(R.drawable.selected_drawable);
		}
	}


	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mTouchTarget != null) {
			boolean wasProcessed = mTouchTarget.onTouchEvent(ev);

			if (!wasProcessed) {
				mTouchTarget = null;
			}

			return wasProcessed;
		}
		return super.dispatchTouchEvent(ev);
	}

	protected void showPromotionalImages(PromotionImages imPromo) {
		if (imPromo != null) {
			String wSave = imPromo.save;
			String wReward = imPromo.wRewards;
			String wVitality = imPromo.vitality;
			String wNewImage = imPromo.newImage;
			DrawImage drawImage = new DrawImage(this);
			if (!TextUtils.isEmpty(wSave)) {
				mImSave.setVisibility(View.VISIBLE);
				drawImage.displaySmallImage(mImSave, wSave);
			} else {
				mImSave.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(wReward)) {
				mImReward.setVisibility(View.VISIBLE);
				drawImage.displaySmallImage(mImReward, wReward);
			} else {
				mImReward.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(wVitality)) {
				mVitalityView.setVisibility(View.VISIBLE);
				drawImage.displaySmallImage(mVitalityView, wVitality);
			} else {
				mVitalityView.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(wNewImage)) {
				mImNewImage.setVisibility(View.VISIBLE);
				drawImage.displaySmallImage(mImNewImage, wNewImage);

			} else {
				mImNewImage.setVisibility(View.GONE);
			}
		}
	}

	public void dismissSizeDialog() {
		if (mPSizeWindow != null) {
			if (mPSizeWindow.isShowing()) {
				mPSizeWindow.dismiss();
			}
		}
	}

	public void dismissColourDialog() {
		if (mPColourWindow != null) {
			if (mPColourWindow.isShowing()) {
				mPColourWindow.dismiss();
			}
		}
	}

	public void dismissPopWindow() {
		if (mPColourWindow != null) {
			if (mPColourWindow.isShowing()) {
				mPColourWindow.dismiss();
			}
		}
		if (mPSizeWindow != null) {
			if (mPSizeWindow.isShowing()) {
				mPSizeWindow.dismiss();
			}
		}
	}

	protected String getImageByWidth(String imageUrl) {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		return imageUrl + "?w=" + width + "&q=" + IMAGE_QUALITY;
	}

	@Override
	public void onSelectedProduct(View v, int position) {
	}

	@Override
	public void onLongPressState(View v, int position) {

	}

	@Override
	public void onSelectedColor(View v, int position) {
		selectedProduct(position);
	}


	public void setTextFromGrid() {
		mTextTitle.setText(Html.fromHtml(isEmpty(mSelectedProduct.productName)));
	}

	public void loadHeroImage(String heroImage) {
		mDefaultImage = heroImage;
		mAuxiliaryImages = new ArrayList<>();
		mAuxiliaryImages.clear();
		mAuxiliaryImages.add(heroImage);
		mProductViewPagerAdapter = new ProductViewPagerAdapter(this, mAuxiliaryImages, this);
		mViewPagerProduct.setAdapter(mProductViewPagerAdapter);
		mProductViewPagerAdapter.notifyDataSetChanged();
	}

	public void showPrice() {
		ArrayList<Double> priceList = new ArrayList<>();
		for (OtherSkus os : mSelectedProduct.otherSkus) {
			if (!TextUtils.isEmpty(os.wasPrice)) {
				priceList.add(Double.valueOf(os.wasPrice));
			}
		}
		String wasPrice = "";
		if (priceList.size() > 0) {
			wasPrice = String.valueOf(Collections.max(priceList));
		}

		String fromPrice = String.valueOf(mSelectedProduct.fromPrice);
		productPriceList(mTextPrice, mTextActualPrice,
				fromPrice, wasPrice, mSelectedProduct.productType);
	}

	public void resetProductSize() {
		mTextSelectSize.setText("");
		uniqueSizeList = new ArrayList<>();
		uniqueColorList = new ArrayList<>();
		mProductSizeAdapter = new ProductSizeAdapter(uniqueSizeList, this);
		mProductSizeAdapter.notifyDataSetChanged();
	}

	public void showSizeProgressBar() {
		mProductCode.setText(getString(R.string.loading_product_info));
		showProductCode();
		mSizeProgressBar.getIndeterminateDrawable().setColorFilter(null);
		mSizeProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		mSizeProgressBar.bringToFront();
		mSizeProgressBar.setVisibility(View.VISIBLE);
		mLoadingDaot.setVisibility(View.VISIBLE);
		llColorSizeContainer.setVisibility(View.GONE);
		loadingColorDivider.setVisibility(View.VISIBLE);
		llLoadingColorSize.setVisibility(View.VISIBLE);
		vColorSizeHorizontalLine.setVisibility(View.GONE);
		mSizeProgressBar.setAlpha(0.3f);
		mTextColour.setAlpha(0.3f);
		mImSelectedColor.setAlpha(0.3f);
		mTextSelectSize.setAlpha(0.3f);
		mImColorArrow.setAlpha(0.3f);
		mColorArrow.setAlpha(0.3f);
		mTextSelectSize.setAlpha(0.3f);
		mTextProductSize.setAlpha(0.3f);
		mBtnShopOnlineWoolies.setAlpha(0.55f);
		mBtnShopOnlineWoolies.setEnabled(false);
	}

	public void hideProgressDetailLoad() {
		mSizeProgressBar.setVisibility(View.GONE);
		mLoadingDaot.setVisibility(View.GONE);
		loadingColorDivider.setVisibility(View.GONE);
		llLoadingColorSize.setVisibility(View.GONE);
		mSizeProgressBar.setAlpha(1f);
		mTextColour.setAlpha(1f);
		mImSelectedColor.setAlpha(1f);
		mTextSelectSize.setAlpha(1f);
		mImColorArrow.setAlpha(1f);
		mColorArrow.setAlpha(1f);
		mTextSelectSize.setAlpha(1f);
		mTextProductSize.setAlpha(1f);
		mBtnShopOnlineWoolies.setAlpha(1f);
		if (mOtherSkuSize > 1) {
			vColorSizeHorizontalLine.setVisibility(View.VISIBLE);
			llColorSizeContainer.setVisibility(View.VISIBLE);
		} else {
			vColorSizeHorizontalLine.setVisibility(View.GONE);
			llColorSizeContainer.setVisibility(View.GONE);
		}
	}

	public void resetLongDescription() {
		mWebDescription.loadDataWithBaseURL("file:///android_res/drawable/", "", "text/html", "UTF-8", null);

	}

	public void showProductCode() {
		mProductCode.setVisibility(View.VISIBLE);
	}

	public void hideProductCode() {
		mProductCode.setVisibility(View.GONE);
	}

	public void resetColourField() {
		mTextColour.setText(getString(R.string.product_colour));
	}

	public void setPromotionText(String saveText) {
		if (TextUtils.isEmpty(saveText)) {
			mTextPromo.setVisibility(View.GONE);
		} else {
			mTextPromo.setVisibility(View.VISIBLE);
			mTextPromo.setText(saveText);
		}
	}

	public OtherSku getDefaultSKUModel() {
		if (otherSkusList != null) {
			if (otherSkusList.size() > 0) {
				for (OtherSku option : otherSkusList) {
					if (option.sku.equalsIgnoreCase(currentSKUId)) {
						return option;
					}
				}
			}
		}
		return new OtherSku();
	}

	public void updateHeroImage() {
		try {
			JSONObject jsProductList = new JSONObject(new JSONObject(mProductJSON).getString("product"));

			//set ingredients
			if (jsProductList.has("ingredients")) {
				setIngredients(jsProductList.getString("ingredients"));
			} else {
				setIngredients("");
			}

			//setup auxiliaryImages
			if (jsProductList.has("auxiliaryImages")) {
				setUpAuxiliaryImages(jsProductList.getString("auxiliaryImages"));
			}

		} catch (JSONException ignored) {
		}
	}

	public void setUpAuxiliaryImages(String auxiliaryImages) {
		mAuxiliaryImages = new ArrayList<>();
		mAuxiliaryImages.clear();
		try {
			JSONObject jsAuxiliaryImages = new JSONObject(auxiliaryImages);
			Iterator<String> keysIterator = jsAuxiliaryImages.keys();
			String colour = mDefaultSKUModel.colour;
			if (colour != null) {
				selectedColor(mDefaultSKUModel.externalColourRef);
				mTextColour.setText(colour);
				colour = colour.toLowerCase().replaceAll("\\s+", "");
				while (keysIterator.hasNext()) {
					String keyStr = keysIterator.next();
					if (keyStr.toLowerCase().contains(colour)) {
						mAuxiliaryImages.add(0, mDefaultImage);
						String valueStr = jsAuxiliaryImages.getString(keyStr);
						JSONObject jsonObject = new JSONObject(valueStr);
						if (jsonObject.has("externalImageRef")) {
							mAuxiliaryImages.add(getImageByWidth(jsonObject.getString("externalImageRef")));
						}
					}
				}
			} else {
				mAuxiliaryImages.add(0, mDefaultImage);
			}
		} catch (JSONException ignored) {
		}

		Set<String> removeDuplicateImage = new LinkedHashSet<>(mAuxiliaryImages);
		mAuxiliaryImages.clear();
		mAuxiliaryImages.addAll(removeDuplicateImage);

		//display default image when auxiliary images is empty
		if (mAuxiliaryImages.size() == 0) {
			mAuxiliaryImages.add(0, mDefaultImage);
		}

		setUpBinder(mAuxiliaryImages);
	}

	public void setUpBinder(ArrayList<String> mAuxiliaryImages) {
		mProductViewPagerAdapter = new ProductViewPagerAdapter(this, mAuxiliaryImages, this);
		mViewPagerProduct.setAdapter(mProductViewPagerAdapter);
		mProductViewPagerAdapter.notifyDataSetChanged();
		setupPagerIndicatorDots();
		mViewPagerProduct.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
				// All of this is to inhibit any scrollable container from consuming our touch events as the user is changing pages
				if (mPreviousState == ViewPager.SCROLL_STATE_IDLE) {
					if (state == ViewPager.SCROLL_STATE_DRAGGING) {
						mTouchTarget = mViewPagerProduct;
					}
				} else {
					if (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_SETTLING) {
						mTouchTarget = null;
					}
				}

				mPreviousState = state;
			}
		});
	}

	public String getSkuExternalImageRef(String colour) {
		if (otherSkusList != null) {
			if (otherSkusList.size() > 0) {
				List<OtherSku> otherSku = otherSkusList;
				for (OtherSku sku : otherSku) {
					if (sku.colour.equalsIgnoreCase(colour)) {
						return getImageByWidth(sku.externalImageRef);
					}
				}
			}
		}
		return "";
	}

	public void getSKUDefaultSize(String colour) {
		if (otherSkusList != null) {
			if (otherSkusList.size() > 0) {
				List<OtherSku> otherSku = otherSkusList;
				for (OtherSku sku : otherSku) {
					if (sku.colour.equalsIgnoreCase(colour)) {
						if (!TextUtils.isEmpty(sku.size))
							setSelectedTextSize(sku.size);
						else
							setSelectedTextSize("");
						break;
					}
				}
			}
		}
	}

	public void setSelectedTextSize(String size) {
		mTextSelectSize.setText(size);
		mTextSelectSize.setTextColor(Color.BLACK);
	}

	public void updatePrice() {
		highestSKUPrice(String.valueOf(mSelectedProduct.fromPrice));
	}

	public void productDetailPriceList(WTextView wPrice, WTextView WwasPrice,
									   String price, String wasPrice, String productType) {
		switch (productType) {
			case "clothingProducts":
				if (TextUtils.isEmpty(wasPrice)) {
					wPrice.setText(WFormatter.formatAmount(price));
					wPrice.setPaintFlags(0);
					WwasPrice.setText("");
				} else {
					if (wasPrice.equalsIgnoreCase(price)) {
						//wasPrice equals currentPrice
						wPrice.setText(WFormatter.formatAmount(price));
						WwasPrice.setText("");
						wPrice.setPaintFlags(0);
					} else {
						wPrice.setText(WFormatter.formatAmount(wasPrice));
						wPrice.setPaintFlags(wPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
						WwasPrice.setText(WFormatter.formatAmount(price));
					}
				}
				break;

			default:
				if (TextUtils.isEmpty(wasPrice)) {
					if (Utils.isLocationEnabled(WProductDetailActivity.this)) {
						ArrayList<Double> priceList = new ArrayList<>();
						for (OtherSku os : mObjProductDetail.otherSkus) {
							if (!TextUtils.isEmpty(os.price)) {
								priceList.add(Double.valueOf(os.price));
							}
						}
						if (priceList.size() > 0) {
							price = String.valueOf(Collections.max(priceList));
						}
					}
					wPrice.setText(WFormatter.formatAmount(price));
					wPrice.setPaintFlags(0);
					WwasPrice.setText("");
				} else {
					if (Utils.isLocationEnabled(WProductDetailActivity.this)) {
						ArrayList<Double> priceList = new ArrayList<>();
						for (OtherSku os : mObjProductDetail.otherSkus) {
							if (!TextUtils.isEmpty(os.price)) {
								priceList.add(Double.valueOf(os.price));
							}
						}
						if (priceList.size() > 0) {
							price = String.valueOf(Collections.max(priceList));
						}
					}

					if (wasPrice.equalsIgnoreCase(price)) { //wasPrice equals currentPrice
						wPrice.setText(WFormatter.formatAmount(price));
						WwasPrice.setText("");
					} else {
						wPrice.setText(WFormatter.formatAmount(wasPrice));
						wPrice.setPaintFlags(wPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
						WwasPrice.setText(WFormatter.formatAmount(price));
					}
				}
				break;
		}
	}

	private String updatePrice(String colour, String size) {
		String price = "";
		if (otherSkusList != null) {
			if (otherSkusList.size() > 0) {
				for (OtherSku option : otherSkusList) {
					if (colour.equalsIgnoreCase(option.colour) &&
							size.equalsIgnoreCase(option.size)) {
						return option.price;
					}
				}
			}
		}
		return price;
	}

	private String updateWasPrice(String colour, String size) {
		String wasPrice = "";
		if (otherSkusList != null) {
			if (otherSkusList.size() > 0) {
				for (OtherSku option : otherSkusList) {
					if (colour.equalsIgnoreCase(option.colour) &&
							size.equalsIgnoreCase(option.size)) {
						return option.wasPrice;
					}
				}
			}
		}
		return wasPrice;
	}

	public void highestSKUPrice(String fromPrice) {
		List<OtherSku> objPrice = mObjProductDetail.otherSkus;
		if (objPrice.size() > 0) {
			for (OtherSku os : mObjProductDetail.otherSkus) {
				if (fromPrice.equalsIgnoreCase(os.price)) {
					setSelectedTextSize(os.size);
					String wasPrice = os.wasPrice;
					if (TextUtils.isEmpty(wasPrice)) {
						wasPrice = "";
					}
					productDetailPriceList(mTextPrice, mTextActualPrice, fromPrice, wasPrice,
							mObjProductDetail.productType);
					return;
				}
			}
		}
	}

	@Override
	public void SelectedImage(int position, View v) {
		Intent openMultipleImage = new Intent(this, MultipleImageActivity.class);
		openMultipleImage.putExtra("position", position);
		openMultipleImage.putExtra("auxiliaryImages", mAuxiliaryImages);
		startActivity(openMultipleImage);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	public void setLayoutWeight(View v, float weight) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
		params.weight = weight;
		params.setMarginStart((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
		params.setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
		v.setLayoutParams(params);
	}

	public boolean sizeValueExist(ArrayList<OtherSku> list, String name) {
		for (OtherSku item : list) {
			if (item.size.equals(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean colourValueExist(ArrayList<OtherSku> list, String name) {
		for (OtherSku item : list) {
			if (item.colour.equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {

	}
}


