package za.co.woolworths.financial.services.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit.RetrofitError;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewListAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.AddToShoppingListFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.CancelableCallback;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.Utils;

public class ProductGridActivity extends WProductDetailActivity implements SelectedProductView,
		View.OnClickListener, NetworkChangeListener {
	private Toolbar mToolbar;
	private WTextView mToolBarTitle;
	private String productId;
	private int pageNumber = 0;
	private RecyclerView mProductList;
	private ProductGridActivity mContext;
	private List<ProductList> mProduct;
	private WTextView mNumberOfItem;
	private NestedScrollView mProductScroll;
	private ProgressBar mProgressBar;
	private RelativeLayout mRelProgressBar, mRelViewProgressBar;
	private ProductViewListAdapter mProductAdapter;
	private GridLayoutManager recyclerViewLayoutManager;
	private boolean mIsLoading = false;
	private boolean mIsLastPage = false;
	private ProgressBar mProgressVBar;
	private String searchItem = "";
	public int num_of_item;
	private int pageOffset;
	private SlidingUpPanelLayout mSlideUpPanelLayout;
	public String mProductJSON;
	private LinearLayout mLinProductList;
	private boolean productCanClose = false;
	private SlidingUpPanelLayout.PanelState panelIsCollapsed = SlidingUpPanelLayout.PanelState.COLLAPSED;
	private Menu mMenu;
	private String mSkuId, mProductId, mProductName;
	private ErrorHandlerView mErrorHandlerView;
	private WoolworthsApplication mWoolWorthsApplication;
	private ProductGridActivity networkChangeListener;
	private BroadcastReceiver connectionBroadcast;
	private boolean selectProductDetail = false;
	private boolean productBackgroundFail = false;
	private Button mBtnShopOnlineWoolies;
	private List<OtherSku> mOtherSKU;
	private WGlobalState mGlobalState;
	private WButton mBtnStoreFinder;

	private enum RUN_BACKGROUND_TASK {
		SEARCH_PRODUCT, SEARCH_MORE_PRODUCT, LOAD_PRODUCT, LOAD_MORE_PRODUCT
	}

	private RUN_BACKGROUND_TASK runTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		mContext = this;
		setContentView(R.layout.product_layout);
		Utils.updateStatusBarBackground(ProductGridActivity.this);

		try {
			networkChangeListener = ProductGridActivity.this;
		} catch (ClassCastException ignored) {
		}
		connectionBroadcast = Utils.connectionBroadCast(ProductGridActivity.this, networkChangeListener);
		mWoolWorthsApplication = ((WoolworthsApplication) ProductGridActivity.this.getApplication());
		mGlobalState = mWoolWorthsApplication.getWGlobalState();
		initUI();
		initProductDetailUI();


		actionBar();
		bundle();
		slideUpPanelListener();
		registerReceiver(broadcast_reciever, new IntentFilter("closeProductView"));
		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(ProductGridActivity.this)) {
					switch (runTask) {

						case SEARCH_PRODUCT:
							searchProduct();
							break;

						case SEARCH_MORE_PRODUCT:
							searchMoreProduct();
							break;

						case LOAD_PRODUCT:
							loadProduct();
							break;

						case LOAD_MORE_PRODUCT:
							loadMoreProduct();
							break;

						default:
							break;
					}
				}
			}
		});

		disableStoreFinder();
	}

	private void slideUpPanelListener() {
		mSlideUpPanelLayout.setFadeOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			}
		});
		mSlideUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				if (slideOffset == 0.0) {
					mSlideUpPanelLayout.setAnchorPoint(1.0f);
				}
			}

			@Override
			public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
											SlidingUpPanelLayout.PanelState newState) {
				switch (newState) {
					case COLLAPSED:
						panelIsCollapsed = SlidingUpPanelLayout.PanelState.COLLAPSED;
						dismissPopWindow();
						selectProductDetail = false;
						if (productCanClose) { //close ProductView activity when maximum row 1
							closeGridView();
							finish();
						}
						mContext.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
						break;

					case DRAGGING:
						if (productCanClose) { //close ProductView activity when maximum row 1
							dismissPopWindow();
							mLinProductList.removeAllViews();
						}
						break;

					case EXPANDED:
						panelIsCollapsed = SlidingUpPanelLayout.PanelState.EXPANDED;
						mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
						break;
					default:
						break;
				}
			}
		});
	}

	private void productConfig(String productName) {
		mToolBarTitle.setText(productName);
	}

	private void bundle() {
		mProductName = getIntent().getStringExtra("sub_category_name");
		productId = getIntent().getStringExtra("sub_category_id");
		mErrorHandlerView = new ErrorHandlerView(this
				, (RelativeLayout) findViewById(R.id.no_connection_layout));
		hideProgressBar();
		Bundle extras = getIntent().getExtras();
		searchItem = extras.getString("searchProduct");
		String mTitle = extras.getString("title");
		String mTitleNav = extras.getString("titleNav");
		if (TextUtils.isEmpty(searchItem)) {
			if (!TextUtils.isEmpty(mTitle)) {
				mProductName = mTitleNav;
				productId = mTitle;
			}
			productConfig(mProductName);
			searchItem = "";
			loadProduct();
		} else {
			if (TextUtils.isEmpty(mTitle)) {
				productConfig(searchItem);
			} else {
				productConfig(mTitle);
			}
			productId = searchItem;
			searchProduct();
		}
	}

	private void actionBar() {
		setSupportActionBar(mToolbar);
		ActionBar mActionBar = getSupportActionBar();
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(false);
			mActionBar.setHomeAsUpIndicator(R.drawable.back24);
			mActionBar.setBackgroundDrawable(ContextCompat.getDrawable(this,
					R.drawable.appbar_background));
		}
	}

	private void initUI() {
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolBarTitle = (WTextView) findViewById(R.id.toolbarText);
		mNumberOfItem = (WTextView) findViewById(R.id.numberOfItem);
		mProductList = (RecyclerView) findViewById(R.id.productList);
		mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);
		mProgressVBar = (ProgressBar) findViewById(R.id.mProgressB);
		mRelProgressBar = (RelativeLayout) findViewById(R.id.relProgressBar);
		mSlideUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		mLinProductList = (LinearLayout) findViewById(R.id.linProductList);
		mRelViewProgressBar = (RelativeLayout) findViewById(R.id.relViewProgressBar);
		mProductScroll = (NestedScrollView) findViewById(R.id.scrollProduct);
		mBtnShopOnlineWoolies = (Button) findViewById(R.id.btnShopOnlineWoolies);
		mBtnStoreFinder = (WButton) findViewById(R.id.btnStoreFinder);
		mBtnStoreFinder.setOnClickListener(this);

		mProductScroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
			@Override
			public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if (v.getChildAt(v.getChildCount() - 1) != null) {
					if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
							scrollY > oldScrollY) {
						//code to fetch more data for endless scrolling
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								try {
									int visibleItemCount = recyclerViewLayoutManager.getChildCount();
									int totalItemCount = recyclerViewLayoutManager.getItemCount();
									int firstVisibleItemPosition = recyclerViewLayoutManager.findFirstVisibleItemPosition();
									if (!mIsLoading && !mIsLastPage) {
										if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
												&& firstVisibleItemPosition >= 0
												&& totalItemCount >= Utils.PAGE_SIZE) {
											if (mProduct.size() < num_of_item) {
												if (TextUtils.isEmpty(searchItem)) {
													loadMoreProduct();
												} else {
													searchMoreProduct();
												}
											}
										}
									}
								} catch (NullPointerException ignored) {
								}
							}
						});
					}
				}
			}
		});
	}

	@Override
	public void onSelectedProduct(View v, int position) {
		selectProductDetail = true;
		mSelectedProduct = mProduct.get(position);
		mSkuId = mSelectedProduct.otherSkus.get(0).sku;
		mProductId = mSelectedProduct.productId;
		onCallback(mProductId, mSkuId, false);
	}

	@Override
	public void onLongPressState(View v, int position) {

		ProductList mShopListProduct = mProduct.get(position);
		String productId = mShopListProduct.productId;
		String productName = mShopListProduct.productName;
		String externalImageRef = getImageByWidth(mShopListProduct.externalImageRef);
		android.app.FragmentManager fm = mContext.getFragmentManager();
		AddToShoppingListFragment mAddToShoppingListFragment =
				AddToShoppingListFragment.newInstance(productId, productName, externalImageRef);
		mAddToShoppingListFragment.show(fm, "addToShop");
	}

	@Override
	public void onSelectedColor(View v, int position) {
		selectedProduct(position);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ps_search_icon, menu);
		if (TextUtils.isEmpty(searchItem)) {
			menuItemVisible(menu, true);
		} else {
			menuItemVisible(menu, false);
		}
		return super.onCreateOptionsMenu(menu);
	}

	public void menuItemVisible(Menu menu, boolean isVisible) {
		try {
			MenuItem menuItem = menu.findItem(R.id.action_search);
			if (isVisible) {
				menuItem.setEnabled(true);
				menuItem.getIcon().setAlpha(255);
			} else {
				menuItem.setEnabled(false);
				menuItem.getIcon().setAlpha(25);
			}
		} catch (Exception ignored) {
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_search:
				Intent openSearchBarActivity = new Intent(ProductGridActivity.this,
						ProductSearchActivity.class);
				startActivity(openSearchBarActivity);
				break;
			case android.R.id.home:
				onBackPressed();
				overridePendingTransition(0, 0);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showProgressBar() {
		mRelProgressBar.setVisibility(View.VISIBLE);
		mProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK,
				PorterDuff.Mode.MULTIPLY);
	}

	public void hideProgressBar() {
		mRelProgressBar.setVisibility(View.GONE);
	}

	public void searchProduct() {
		searchProductApi().execute();
	}

	private void loadProduct() {
		loadProductAPI().execute();
	}

	public HttpAsyncTask<String, String, ProductView> loadProductAPI() {
		runTask = RUN_BACKGROUND_TASK.LOAD_PRODUCT;
		setNumberOfItem(0);
		return new HttpAsyncTask<String, String, ProductView>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showVProgressBar();
				mErrorHandlerView.hideErrorHandlerLayout();
				mProductScroll.setVisibility(View.GONE);
			}

			@Override
			protected ProductView httpDoInBackground(String... params) {
				pageNumber = 0;
				mIsLastPage = false;
				return mWoolWorthsApplication.getApi().productViewRequest(false, pageNumber, Utils.PAGE_SIZE, productId);

			}

			@Override
			protected Class<ProductView> httpDoInBackgroundReturnType() {
				return ProductView.class;
			}

			@Override
			protected ProductView httpError(String errorMessage, HttpErrorCode
					httpErrorCode) {
				mErrorHandlerView.networkFailureHandler(errorMessage);
				return new ProductView();
			}

			@Override
			protected void onPostExecute(ProductView pv) {
				super.onPostExecute(pv);
				handleLoadAnSearchProductsResponse(pv);
			}
		};
	}

	public HttpAsyncTask<String, String, ProductView> searchProductApi() {
		runTask = RUN_BACKGROUND_TASK.SEARCH_PRODUCT;
		return new HttpAsyncTask<String, String, ProductView>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showVProgressBar();
				mErrorHandlerView.hideErrorHandlerLayout();
				mProductScroll.setVisibility(View.GONE);
			}

			@Override
			protected ProductView httpDoInBackground(String... params) {
				pageNumber = 0;
				mIsLastPage = false;
				return mWoolWorthsApplication.getApi()
						.getProductSearchList(searchItem, false, pageNumber, Utils.PAGE_SIZE);
			}

			@Override
			protected Class<ProductView> httpDoInBackgroundReturnType() {
				return ProductView.class;
			}

			@Override
			protected ProductView httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				mErrorHandlerView.networkFailureHandler(errorMessage);
				return new ProductView();
			}

			@Override
			protected void onPostExecute(ProductView pv) {
				super.onPostExecute(pv);
				menuItemVisible(mMenu, true);
				handleLoadAnSearchProductsResponse(pv);
			}
		};
	}

	private void bindDataWithUI(List<ProductList> prod) {
		mProductAdapter = new ProductViewListAdapter(mContext, prod, mContext);
		recyclerViewLayoutManager = new GridLayoutManager(mContext, 2);
		mProductList.setLayoutManager(recyclerViewLayoutManager);
		mProductList.setNestedScrollingEnabled(false);
		mProductList.setAdapter(mProductAdapter);
	}

	public void loadMoreProduct() {
		runTask = RUN_BACKGROUND_TASK.LOAD_MORE_PRODUCT;
		new HttpAsyncTask<String, String, ProductView>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showProgressBar();
				mIsLoading = true;
				pagination();
			}

			@Override
			protected ProductView httpDoInBackground(String... params) {
				return mWoolWorthsApplication.getApi().productViewRequest(false,
						pageOffset, Utils.PAGE_SIZE, productId);
			}

			@Override
			protected Class<ProductView> httpDoInBackgroundReturnType() {
				return ProductView.class;
			}

			@Override
			protected ProductView httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				ProductView productResponse = new ProductView();
				productResponse.response = new Response();
				hideProgressBar();
				mIsLoading = false;
				mErrorHandlerView.showToast();
				return productResponse;
			}

			@Override
			protected void onPostExecute(ProductView productResponse) {
				super.onPostExecute(productResponse);
				mIsLoading = false;
				List<ProductList> moreProductList;
				moreProductList = productResponse.products;
				if (moreProductList != null && moreProductList.size() != 0) {
					if (moreProductList.size() < Utils.PAGE_SIZE) {
						mIsLastPage = true;
					}
					int actualSize = mProduct.size();
					mProduct.addAll(moreProductList);
					mProductAdapter.notifyItemRangeChanged(actualSize + 1, mProduct.size());
				}
				pageNumber += 1;
				hideProgressBar();
			}
		}.execute();
	}

	private void getProductDetail(final String productId, final String skuId, final boolean closeActivity) {
		productCanClose = closeActivity;
		disableStoreFinder();
		mWoolWorthsApplication.getAsyncApi().getProductDetail(productId, skuId, new CancelableCallback<String>() {

			@Override
			public void onSuccess(String strProduct, retrofit.client.Response response) {
				WProduct wProduct = Utils.stringToJson(mContext, strProduct);
				if (wProduct != null) {
					switch (wProduct.httpCode) {
						case 200:
							mProductJSON = strProduct;
							ArrayList<WProductDetail> mProductList = new ArrayList<>();
							WProductDetail productList = wProduct.product;
							clothingIsEnabled(productList);
							mOtherSKU = wProduct.product.otherSkus;
							if (productList != null) {
								mProductList.add(productList);
							}
							GsonBuilder builder = new GsonBuilder();
							Gson gson = builder.create();
							displayProductDetail(gson.toJson(mProductList), mSkuId, mProductList.get(0).otherSkus.size());
							mBtnShopOnlineWoolies.setEnabled(true);
							break;

						default:
							Utils.updateStatusBarBackground(ProductGridActivity.this);
							Utils.alertErrorMessage(ProductGridActivity.this, wProduct.response.desc);
							mBtnShopOnlineWoolies.setEnabled(false);
							break;
					}

					hideProgressDetailLoad();
					productBackgroundFail = false;
				}
			}

			@Override
			public void onFailure(RetrofitError error) {
				hideProductCode();
				hideProgressDetailLoad();
				if (error.toString().contains("Unable to resolve host")) {
					mErrorHandlerView.showToast();
					productBackgroundFail = true;
				}
			}
		});
	}

	private void hideVProgressBar() {
		mRelViewProgressBar.setVisibility(View.GONE);
		mProductScroll.setVisibility(View.VISIBLE);
		mProgressVBar.getIndeterminateDrawable().setColorFilter(null);
	}

	private void showVProgressBar() {
		mRelViewProgressBar.setVisibility(View.VISIBLE);
		mProductScroll.setVisibility(View.GONE);
		mProgressVBar.getIndeterminateDrawable().setColorFilter(null);
		mProgressVBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
	}

	@Override
	public void onBackPressed() {
		switch (panelIsCollapsed) {
			case COLLAPSED:
				finishActivity();
				break;
			case EXPANDED:
				mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
				break;
			default:
				finishActivity();
				break;
		}
	}

	BroadcastReceiver broadcast_reciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			finish();
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		menuItemVisible(mMenu, true);
		unregisterReceiver(broadcast_reciever);
	}

	private void finishActivity() {
		finish();
		overridePendingTransition(0, 0);
	}

	/***
	 * LOAD MORE PRODUCT FROM SEARCH
	 ***/

	public void searchMoreProduct() {
		runTask = RUN_BACKGROUND_TASK.SEARCH_MORE_PRODUCT;
		new HttpAsyncTask<String, String, ProductView>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showProgressBar();
				mIsLoading = true;
				pageNumber += 1;
				pagination();

			}

			@Override
			protected ProductView httpDoInBackground(String... params) {
				mIsLastPage = false;
				return ((WoolworthsApplication) getApplication()).getApi()
						.getProductSearchList(searchItem, false, pageOffset, Utils.PAGE_SIZE);
			}

			@Override
			protected Class<ProductView> httpDoInBackgroundReturnType() {
				return ProductView.class;
			}

			@Override
			protected ProductView httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				ProductView productResponse = new ProductView();
				productResponse.response = new Response();
				hideProgressBar();
				mIsLoading = false;
				mErrorHandlerView.showToast();
				return productResponse;
			}

			@Override
			protected void onPostExecute(ProductView pv) {
				super.onPostExecute(pv);
				mIsLoading = false;
				List<ProductList> moreProductList;
				moreProductList = pv.products;
				if (moreProductList != null && moreProductList.size() != 0) {
					if (moreProductList.size() < Utils.PAGE_SIZE) {
						mIsLastPage = true;
					}
					int actualSize = mProduct.size();
					mProduct.addAll(moreProductList);
					mProductAdapter.notifyItemRangeChanged(actualSize + 1, mProduct.size());
				}
				pageNumber += 1;
				hideProgressBar();
			}
		}.execute();
	}

	private void pagination() {
		if (mProduct.size() < num_of_item) {
			if (pageNumber == 1) {
				pageOffset = Utils.PAGE_SIZE;  //+1
			} else {
				pageOffset = pageOffset + Utils.PAGE_SIZE;
			}
		}
	}

	public void onCallback(final String productId, final String skuId, final boolean closeActivity) {
		//this block of code should be protected from IllegalStateException
		resetProductSize();
		loadHeroImage(getImageByWidth(mSelectedProduct.externalImageRef));
		selectedColor("");
		showSlideUpView();
		setSelectedTextSize("");
		setTextFromGrid();
		showPrice();
		setPromotionText("");
		resetColourField();
		showPromotionalImages(new PromotionImages());
		showPromotionalImages(mSelectedProduct.promotionImages);
		addButtonEvent();
		setIngredients("");
		resetLongDescription();
		setupPagerIndicatorDots();

		showSizeProgressBar(mSelectedProduct.productType);
		CancelableCallback.cancelAll();
		getProductDetail(productId, skuId, closeActivity);
	}

	private void showSlideUpView() {
		mSlideUpPanelLayout.setAnchorPoint(1.0f);
		mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
		mSlideUpPanelLayout.setScrollableViewHelper(new NestedScrollableViewHelper(mScrollProductDetail));
	}

	private void closeGridView() {
		if (productCanClose) {
			mContext.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			mLinProductList.removeAllViews();
		}
	}

	public void handleLoadAnSearchProductsResponse(ProductView pv) {
		try {
			switch (pv.httpCode) {
				case 200:
					mProduct = null;
					mProduct = new ArrayList<>();
					if (pv.products != null && pv.products.size() != 0) {
						mProduct = pv.products;
						if (pv.products.size() == 1) {
							mProductScroll.setVisibility(View.GONE);
							mSkuId = mProduct.get(0).otherSkus.get(0).sku;
							mProductId = mProduct.get(0).productId;
							mSelectedProduct = mProduct.get(0);
							onCallback(mProductId, mSkuId, true);
						} else {
							int totalItem = pv.pagingResponse.numItemsInTotal;
							num_of_item = totalItem;
							setNumberOfItem(totalItem);
							bindDataWithUI(mProduct);
							hideVProgressBar();
						}
						break;
					}
				default:
					mNumberOfItem.setText(String.valueOf(0));
					hideVProgressBar();
					break;
			}
		} catch (Exception ignored) {
		}
	}

	private void setNumberOfItem(int numberOfItem) {
		mNumberOfItem.setText(String.valueOf(numberOfItem));
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(connectionBroadcast);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(connectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	}

	@Override
	public void onConnectionChanged() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (new ConnectionDetector().isOnline(ProductGridActivity.this)) {
					if (selectProductDetail && productBackgroundFail) {
						onCallback(mProductId, mSkuId, false);
					}
				} else {
					if (selectProductDetail) {
						mErrorHandlerView.showToast();
					}
				}
			}
		});
	}


	private String toJson(Object jsonObject) {
		return new Gson().toJson(jsonObject);
	}

	private ArrayList<OtherSku> getColorList() {
		Collections.sort(mOtherSKU, new Comparator<OtherSku>() {
			@Override
			public int compare(OtherSku lhs, OtherSku rhs) {
				return lhs.colour.compareToIgnoreCase(rhs.colour);
			}
		});

		ArrayList<OtherSku> commonColorSku = new ArrayList<>();
		for (OtherSku sku : mOtherSKU) {
			if (!colourValueExist(commonColorSku, sku.colour)) {
				commonColorSku.add(sku);
			}
		}
		return commonColorSku;
	}

	private ArrayList<OtherSku> getSizeList() {
		Collections.sort(mOtherSKU, new Comparator<OtherSku>() {
			@Override
			public int compare(OtherSku lhs, OtherSku rhs) {
				return lhs.size.compareToIgnoreCase(rhs.size);
			}
		});

		ArrayList<OtherSku> commonColorSku = new ArrayList<>();
		for (OtherSku sku : mOtherSKU) {
			if (!colourValueExist(commonColorSku, sku.size)) {
				commonColorSku.add(sku);
			}
		}
		return commonColorSku;
	}

	private boolean productHasColour() {
		return getColorList().size() > 0 ? true : false;
	}

	private boolean productHasSize() {
		return getSizeList().size() > 0 ? true : false;
	}

	public void colourIntent() {
		Intent mIntent = new Intent(this, ConfirmColorSizeActivity.class);
		mIntent.putExtra("COLOR_LIST", toJson(getColorList()));
		mIntent.putExtra("OTHERSKU", toJson(mOtherSKU));
		mIntent.putExtra("PRODUCT_HAS_COLOR", true);
		mIntent.putExtra("PRODUCT_HAS_SIZE", true);
		mIntent.putExtra("PRODUCT_NAME", mSelectedProduct.productName);
		startActivity(mIntent);
		overridePendingTransition(0,0);
	}

	public void sizeIntent() {
		Intent mIntent = new Intent(this, ConfirmColorSizeActivity.class);
		mIntent.putExtra("COLOR_LIST", toJson(getColorList()));
		mIntent.putExtra("OTHERSKU", toJson(mOtherSKU));
		mIntent.putExtra("PRODUCT_HAS_COLOR", false);
		mIntent.putExtra("PRODUCT_HAS_SIZE", true);
		mIntent.putExtra("PRODUCT_NAME", mSelectedProduct.productName);
		startActivity(mIntent);
		overridePendingTransition(0,0);
	}

	public void colourNoSizeIntent() {
		Intent mIntent = new Intent(this, ConfirmColorSizeActivity.class);
		mIntent.putExtra("COLOR_LIST", toJson(getColorList()));
		mIntent.putExtra("OTHERSKU", toJson(mOtherSKU));
		mIntent.putExtra("PRODUCT_HAS_COLOR", true);
		mIntent.putExtra("PRODUCT_HAS_SIZE", false);
		mIntent.putExtra("PRODUCT_NAME", mSelectedProduct.productName);
		startActivity(mIntent);
		overridePendingTransition(0,0);
	}

	public void noSizeColorIntent() {
		Intent mIntent = new Intent(this, WStockFinderActivity.class);
		mIntent.putExtra("PRODUCT_NAME", mSelectedProduct.productName);
		mIntent.putExtra("SELECTED_SKU", mSkuId);
		startActivity(mIntent);
		overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}

	private void disableStoreFinder() {
		setLayoutWeight(mBtnShopOnlineWoolies, 1.0f);
		mBtnStoreFinder.setVisibility(View.GONE);
	}

	private void clothingIsEnabled(WProductDetail productList) {
		String productType = productList.productType;
		if (productType.equalsIgnoreCase("clothingProducts")) {
			if (mGlobalState.clothingIsEnabled()) {
				setLayoutWeight(mBtnShopOnlineWoolies, 0.5f);
				setLayoutWeight(mBtnStoreFinder, 0.5f);
				mBtnStoreFinder.setVisibility(View.VISIBLE);
			} else {
				setLayoutWeight(mBtnShopOnlineWoolies, 1.0f);
				mBtnStoreFinder.setVisibility(View.GONE);
			}
		} else {
			if (mGlobalState.isFoodProducts()) {
				setLayoutWeight(mBtnShopOnlineWoolies, 0.5f);
				setLayoutWeight(mBtnStoreFinder, 0.5f);
				mBtnStoreFinder.setVisibility(View.VISIBLE);
			} else {
				setLayoutWeight(mBtnShopOnlineWoolies, 1.0f);
				mBtnStoreFinder.setVisibility(View.GONE);
			}
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.textSelectColour:
			case R.id.imSelectedColor:
			case R.id.imColorArrow:
			case R.id.linColour:
			case R.id.textColour:
				if (otherSkusList != null) {
					if (otherSkusList.size() > 0) {
						dismissSizeDialog();
						LayoutInflater mSlideUpPanelLayoutInflater
								= (LayoutInflater) getBaseContext()
								.getSystemService(LAYOUT_INFLATER_SERVICE);
						View mPopWindow = mSlideUpPanelLayoutInflater.inflate(R.layout.product_size_row, null);
						mColorRecycleSize = (RecyclerView) mPopWindow.findViewById(R.id.recyclerviewSize);
						bindWithUI(otherSkusList, true);
						mPColourWindow = new PopupWindow(
								mPopWindow,
								ViewGroup.LayoutParams.WRAP_CONTENT,
								ViewGroup.LayoutParams.WRAP_CONTENT);
						mPopWindow.setOnTouchListener(new View.OnTouchListener() {

							@Override
							public boolean onTouch(View arg0, MotionEvent arg1) {
								return true;
							}
						});
						mPColourWindow.setTouchable(true);
						mPColourWindow.showAsDropDown(mTextSelectColor, -50, -180);
					}
				}
				break;

			case R.id.textProductSize:
			case R.id.mColorArrow:
			case R.id.textSelectSize:
			case R.id.linSize:
				if (otherSkusList != null) {
					if (otherSkusList.size() > 0) {
						dismissColourDialog();
						LayoutInflater layoutInflater
								= (LayoutInflater) getBaseContext()
								.getSystemService(LAYOUT_INFLATER_SERVICE);
						View popupView = layoutInflater.inflate(R.layout.product_size_row, null);
						mRecyclerviewSize = (RecyclerView) popupView.findViewById(R.id.recyclerviewSize);
						LinearLayout mPopLinContainer = (LinearLayout) popupView.findViewById(R.id.linPopUpContainer);
						bindWithUI(otherSkusList, false);
						mPSizeWindow = new PopupWindow(
								popupView,
								ViewGroup.LayoutParams.WRAP_CONTENT,
								ViewGroup.LayoutParams.WRAP_CONTENT);
						mPopLinContainer.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								mPSizeWindow.dismiss();
							}
						});
						mPSizeWindow.showAsDropDown(mTextSelectSize, -50, -180);
					}
				}
				break;

			case R.id.imCloseProduct:
				onBackPressed();
				break;

			case R.id.btnStoreFinder:
				boolean productHasColour = productHasColour();
				boolean productHasSize = productHasSize();

				mScrollProductDetail.scrollTo(0, 0);
				if (productHasColour && productHasSize) {
					colourIntent();
				} else if (!productHasColour && productHasSize) {
					sizeIntent();
				} else if (productHasColour && !productHasSize) {
					colourNoSizeIntent();
				} else {
					noSizeColorIntent();
				}
				break;
		}

	}
}

