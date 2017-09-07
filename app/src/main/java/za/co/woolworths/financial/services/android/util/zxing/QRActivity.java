package za.co.woolworths.financial.services.android.util.zxing;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.MultiFormatReader;
import com.pacific.mvc.Activity;
import com.trello.rxlifecycle.ActivityEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.OtherSku;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.PromotionImages;
import za.co.woolworths.financial.services.android.models.dto.StoreDetails;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.activities.ConfirmColorSizeActivity;
import za.co.woolworths.financial.services.android.ui.activities.EnterBarcodeActivity;
import za.co.woolworths.financial.services.android.ui.activities.MultipleImageActivity;
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity;
import za.co.woolworths.financial.services.android.ui.views.LoadingDots;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpDialogManager;
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizeAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WrapContentWebView;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.FusedLocationSingleton;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.LocationItemTask;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.PermissionResultCallback;
import za.co.woolworths.financial.services.android.util.PermissionUtils;
import za.co.woolworths.financial.services.android.util.SelectedProductView;
import za.co.woolworths.financial.services.android.util.SimpleDividerItemDecoration;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class QRActivity extends Activity<QRModel> implements View.OnClickListener, SelectedProductView, ProductViewPagerAdapter.MultipleImageInterface, PermissionResultCallback {

	public final int IMAGE_QUALITY = 85;
	public static final int CODE_PICK_IMAGE = 0x100;
	private BaseCameraManager cameraManager;
	public final int ZBAR_PERMS_REQUEST_CODE = 12345678;
	private WButton mBtnManual;
	private TextView mTextInfo;
	private ProgressBar mProgressBar;
	private QRCodeView qRview;
	private SlidingUpPanelLayout mSlideUpPanelLayout;
	private WTextView mTextSelectSize;
	private RecyclerView mRecyclerviewSize;
	private ArrayList<WProductDetail> mproductDetail;
	private WTextView mTextTitle;
	private WTextView mTextPrice;
	private LinearLayout llColorSizeContainer;
	private WTextView mProductCode;
	private List<OtherSku> mOtherSKU;
	private WTextView mTextSelectColor;
	private PopupWindow mPColourWindow;
	private PopupWindow mPSizeWindow;
	private boolean productIsColored = false;
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
	private String mDefaultImage;
	private SimpleDraweeView mImSelectedColor;
	private View mColorView;
	private WTextView mTextPromo;
	private WTextView mTextActualPrice;
	private WTextView mTextColour;
	private WrapContentWebView mWebDescription;
	private WTextView mIngredientList;
	private LinearLayout mLinIngredient;
	private View ingredientLine;
	public String mProductJSON;
	public NestedScrollView mScrollProductDetail;
	private int mPreviousState;
	private ViewPager mTouchTarget;
	private WProductDetail mObjProductDetail;
	private SlidingUpPanelLayout.PanelState mPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
	private String mDefaultColor;
	private String mDefaultColorRef;
	public String mDefaultSize;
	public ProductViewPagerAdapter mProductViewPagerAdapter;
	private final String MBGPRODUCTDETAIL = "PRODUCT_DETAIL";
	private final String MBGPRODUCT = "PRODUCT";
	private String mBarcodeNumber;
	private String mCurrentBgTask = MBGPRODUCT;
	private ErrorHandlerView mErrorHandlerView;
	private String mProductId;
	private String mSkuId;
	public WoolworthsApplication mWoolworthsApplication;
	private WButton mBtnShopOnlineWoolies;
	private WGlobalState mGlobalState;
	private LinearLayout llStoreFinder;
	private LinearLayout llLoadingColorSize;
	private View loadingColorDivider;
	private PermissionUtils permissionUtils;
	private ArrayList<String> permissions;
	private WProductDetail productList;
	private WProductDetail mProduct;
	private WTextView tvBtnFinder;
	private ProgressBar mButtonProgress;
	private String TAG = this.getClass().getSimpleName();
	private Location mLocation;
	private LocationItemTask locationItemTask;
	private boolean mProductHasColour;
	private boolean mProductHasSize;
	private boolean mProductHasOneColour;
	private boolean mProductHasOneSize;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this, R.color.black);
		setContentView(R.layout.barcode_scanner_layout);
		setupToolbar();

		mGlobalState = ((WoolworthsApplication) QRActivity.this.getApplication()).getWGlobalState();
		if (Build.VERSION_CODES.LOLLIPOP >= Build.VERSION.SDK_INT) {
			cameraManager = new CameraManager(QRActivity.this.getApplication());
		} else {
			cameraManager = new CameraManager(QRActivity.this.getApplication());
		}
		model = new QRModel(new QRView(this));
		model.onCreate();

		permissionUtils = new PermissionUtils(this, this);
		permissions = new ArrayList<>();
		permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

		cameraManager.setOnResultListener(new BaseCameraManager.OnResultListener() {
			@Override
			public void onResult(final QRResult qrResult) {
				String barcodeFormat = qrResult.getResult().getBarcodeFormat().name();
				for (String bf : barcodeFormat()) {
					if (bf.equalsIgnoreCase(barcodeFormat)) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								cameraManager.stopCamera();
								String barcodeNumber = qrResult.getResult().getText();
								getProductRequest(barcodeNumber);

							}
						});
					}
				}
			}
		});

		initUI();
		initProductDetailUI();
		slideUpPanel();

		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(QRActivity.this)) {
					switch (mCurrentBgTask) {
						case MBGPRODUCT:
							getProductRequest(mBarcodeNumber);
							break;

						case MBGPRODUCTDETAIL:
							getProductDetail(mProductId, mSkuId);
							break;
					}
				}
			}
		});
		disableStoreFinder();
	}

	private void slideUpPanel() {

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
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
								resetColorSizePopup();
								cancelInStoreTask();
								dismissFindInStoreProgress();
								QRActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
								dismissPopWindow();
								QRActivity.this.onResume();
							}
						});
						break;

					case DRAGGING:
						model.setEmptyViewVisible(false);
						break;

					case EXPANDED:
						mPanelState = SlidingUpPanelLayout.PanelState.EXPANDED;
						model.setEmptyViewVisible(false);
						QRActivity.this.onPause();
						QRActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
						break;
					default:
						break;
				}
			}
		});
	}

	private void initUI() {
		qRview = (QRCodeView) findViewById(R.id.qr_view);
		mSlideUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		mTextInfo = (TextView) findViewById(R.id.textInfo);
		llStoreFinder = (LinearLayout) findViewById(R.id.llStoreFinder);
		llStoreFinder.setOnClickListener(this);
		LoadingDots mLoadingDot = (LoadingDots) findViewById(R.id.loadingDots);
		llLoadingColorSize = (LinearLayout) findViewById(R.id.llLoadingColorSize);
		loadingColorDivider = findViewById(R.id.loadingColorDivider);
		tvBtnFinder = (WTextView) findViewById(R.id.tvBtnFinder);
		mButtonProgress = (ProgressBar) findViewById(R.id.mButtonProgress);
		mLoadingDot.setVisibility(View.GONE);
		llLoadingColorSize.setVisibility(View.GONE);
		loadingColorDivider.setVisibility(View.GONE);
		mProgressBar = (ProgressBar) findViewById(R.id.ppBar);
		mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
		mBtnManual = (WButton) findViewById(R.id.btnManual);
		mWoolworthsApplication = (WoolworthsApplication) QRActivity.this.getApplication();
		mErrorHandlerView = new ErrorHandlerView(this
				, (RelativeLayout) findViewById(R.id.no_connection_layout));
		mBtnManual.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		resumeScan();
	}

	@Override
	protected void onPause() {
		super.onPause();
		model.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cameraManager.releaseCamera();
		cameraManager.shutdownExecutor();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == CODE_PICK_IMAGE) {
			String[] columns = {MediaStore.Images.Media.DATA};
			Cursor cursor = getContentResolver().query(data.getData(), columns, null, null, null);
			assert cursor != null;
			if (cursor.moveToFirst()) {
				Observable
						.just(cursor.getString(cursor.getColumnIndex(columns[0])))
						.observeOn(Schedulers.from(cameraManager.getExecutor()))
						.compose(this.<String>bindUntilEvent(ActivityEvent.PAUSE))
						.map(new Func1<String, QRResult>() {
							@Override
							public QRResult call(String str) {
								return QRUtils.decode(str, new MultiFormatReader());
							}
						})
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(new Action1<QRResult>() {
							@Override
							public void call(QRResult qrResult) {
								model.resultDialog(qrResult);
							}
						});
			}
			cursor.close();
		} else if (requestCode == WGlobalState.SYNC_FIND_IN_STORE) {
			if (resultCode == RESULT_OK) {
				startLocationUpdates();
			}
		} else {
		}
	}

	public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
		if (cameraManager.getExecutor().isShutdown()) return;
		Observable
				.just(surfaceHolder)
				.compose(this.<SurfaceHolder>bindUntilEvent(ActivityEvent.PAUSE))
				.observeOn(Schedulers.from(cameraManager.getExecutor()))
				.map(new Func1<SurfaceHolder, Object>() {
					@Override
					public Object call(SurfaceHolder holder) {
						cameraManager.setRotate(getWindowManager().getDefaultDisplay().getRotation());
						cameraManager.connectCamera(holder);
						return null;
					}
				})
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<Object>() {
					@Override
					public void call(Object o) {
						model.setEmptyViewVisible(false);
						cameraManager.startCapture();
					}
				});
	}

	public void onSurfaceDestroyed() {
		cameraManager.releaseCamera();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		boolean allowed = true;
		switch (requestCode) {
			case ZBAR_PERMS_REQUEST_CODE:
				for (int res : grantResults) {
					// if user granted all permissions.
					allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
				}
				break;
			default:
				// if user not granted permissions.
				allowed = false;
				break;
		}
		if (allowed) {
			//user granted all permissions we can perform our task.
			if (Build.VERSION_CODES.LOLLIPOP >= Build.VERSION.SDK_INT) {
				cameraManager = new CameraManager(QRActivity.this.getApplication());
			} else {
				cameraManager = new CameraManager(QRActivity.this.getApplication());
			}
			model = new QRModel(new QRView(this));
			model.onCreate();

			cameraManager.setOnResultListener(new BaseCameraManager.OnResultListener() {
				@Override
				public void onResult(QRResult qrResult) {
					model.resultDialog(qrResult);
				}
			});

		} else {
			// we will give warning to user that they haven't granted permissions.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					Toast.makeText(this, "Camera Permissions denied.", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public void setupToolbar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		WTextView mTextToolbar = (WTextView) findViewById(R.id.toolbarText);
		mTextToolbar.setText(getString(R.string.scan_product));
		mTextToolbar.setGravity(Gravity.LEFT);
		mTextToolbar.setTextColor(Color.WHITE);
		setSupportActionBar(toolbar);
		final ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayShowTitleEnabled(false);
			ab.setDisplayUseLogoEnabled(false);
			ab.setDisplayShowTitleEnabled(false);
			ab.setHomeAsUpIndicator(R.drawable.close_white);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				finishActivity();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.textSelectColour:
			case R.id.imSelectedColor:
			case R.id.imColorArrow:
			case R.id.linColour:
				dismissSizeDialog();
				LayoutInflater mSlideUpPanelLayoutInflater
						= (LayoutInflater) getBaseContext()
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				View mPopWindow = mSlideUpPanelLayoutInflater.inflate(R.layout.product_size_row, null);
				mColorRecycleSize = (RecyclerView) mPopWindow.findViewById(R.id.recyclerviewSize);
				bindWithUI(mOtherSKU, true);
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
				break;

			case R.id.textProductSize:
			case R.id.mColorArrow:
			case R.id.textSelectSize:
			case R.id.linSize:
				dismissColourDialog();
				LayoutInflater layoutInflater
						= (LayoutInflater) getBaseContext()
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				View popupView = layoutInflater.inflate(R.layout.product_size_row, null);
				mRecyclerviewSize = (RecyclerView) popupView.findViewById(R.id.recyclerviewSize);
				LinearLayout mPopLinContainer = (LinearLayout) popupView.findViewById(R.id.linPopUpContainer);

				bindWithUI(mOtherSKU, false);

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

				break;

			case R.id.imCloseProduct:
				if (mPanelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
					mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
				} else {
					onBackPressed();
				}
				break;

			case R.id.btnShopOnlineWoolies:
				if (!TextUtils.isEmpty(mCheckOutLink)) {
					Utils.openExternalLink(QRActivity.this, Utils.addUTMCode(mCheckOutLink));

				}
				break;

			case R.id.btnManual:
				Intent openManual = new Intent(QRActivity.this, EnterBarcodeActivity.class);
				startActivity(openManual);
				overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				break;

			case R.id.llStoreFinder:
				mScrollProductDetail.scrollTo(0, 0);
				if (Utils.isLocationEnabled(QRActivity.this)) {
					permissionUtils.check_permission(permissions, "Explain here why the app needs permissions", 1);
				} else {
					Utils.displayValidationMessage(QRActivity.this, CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.LOCATION_OFF, "");
				}
				break;
		}
	}

	private void getProductDetail(final String productId, final String skuId) {
		mCurrentBgTask = MBGPRODUCTDETAIL;
		this.mProductId = productId;
		this.mSkuId = skuId;
		((WoolworthsApplication) QRActivity.this.getApplication()).getAsyncApi().getProductDetail(productId, skuId, new Callback<String>() {
			@Override
			public void success(String strProduct, retrofit.client.Response response) {
				hideProgressBar();
				WProduct wProduct = Utils.stringToJson(QRActivity.this, strProduct);
				if (wProduct != null) {
					switch (wProduct.httpCode) {
						case 200:
							ArrayList<WProductDetail> mProductList;
							productList = wProduct.product;
							mProductList = new ArrayList<>();
							if (productList != null) {
								mProductList.add(productList);
							}
							displayProductDetail(mProductList.get(0).productName,
									new GsonBuilder().create().toJson(mProductList), skuId);

							mSlideUpPanelLayout.setAnchorPoint(1.0f);
							mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
							mSlideUpPanelLayout.setScrollableViewHelper(new NestedScrollableViewHelper(mScrollProductDetail));
							break;
						default:
							hideProgressBar();
							break;
					}
				}
			}

			@Override
			public void failure(RetrofitError error) {
				if (error.toString().contains("Unable to resolve host"))
					mErrorHandlerView.showToast();
			}
		});

	}

	private void showProgressBar() {
		mProgressBar.setVisibility(View.VISIBLE);
		mTextInfo.setVisibility(View.GONE);
		mBtnManual.setEnabled(false);
	}

	private void hideProgressBar() {
		mProgressBar.setVisibility(View.GONE);
		mTextInfo.setVisibility(View.VISIBLE);
		mBtnManual.setEnabled(true);
	}

	private ArrayList<String> barcodeFormat() {
		ArrayList<String> barcodeFormat = new ArrayList<>();
		barcodeFormat.add("EAN_8");
		barcodeFormat.add("UPC_E");
		barcodeFormat.add("UPC_A");
		barcodeFormat.add("EAN_13");
		barcodeFormat.add("ISBN_13");
		barcodeFormat.add("CODE_128");
		return barcodeFormat;
	}

	public void getProductRequest(final String query) {
		this.mBarcodeNumber = query;
		this.mCurrentBgTask = MBGPRODUCT;

		getProductAsyncRequestAPI(query).execute();
	}

	public HttpAsyncTask<String, String, ProductView> getProductAsyncRequestAPI(final String query) {
		return new HttpAsyncTask<String, String, ProductView>() {
			@Override
			protected ProductView httpDoInBackground(String... params) {
				return ((WoolworthsApplication) QRActivity.this.getApplication()).getApi()
						.getProductSearchList(query, true, 0, Utils.PAGE_SIZE);
			}

			@Override
			protected ProductView httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hideProgressBar();
					}
				});
				mErrorHandlerView.networkFailureHandler(errorMessage);
				return new ProductView();
			}

			@Override
			protected void onPostExecute(ProductView product) {
				super.onPostExecute(product);
				try {
					ArrayList<ProductList> mProduct = product.products;

					if (mProduct != null) {
						if (mProduct.size() > 0) {
							getProductDetail(mProduct.get(0).productId, mProduct.get(0).sku);
						} else {
							hideProgressBar();
							errorScanCode();
						}
					}
				} catch (NullPointerException ignored) {
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mErrorHandlerView.hideErrorHandlerLayout();
				showProgressBar();
			}

			@Override
			protected Class<ProductView> httpDoInBackgroundReturnType() {
				return ProductView.class;
			}
		};
	}

	private void errorScanCode() {
		Utils.displayValidationMessage(this, CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.BARCODE_ERROR, "");
	}

	private void resumeScan() {
		model.onResume();
		if (qRview != null)
			qRview.setBackgroundColor(Color.TRANSPARENT);
	}

	protected void initProductDetailUI() {
		mScrollProductDetail = (NestedScrollView) findViewById(R.id.scrollProductDetail);
		mColorView = findViewById(R.id.colorView);
		mTextSelectSize = (WTextView) findViewById(R.id.textSelectSize);
		mTextColour = (WTextView) findViewById(R.id.textColour);
		WTextView mTextProductSize = (WTextView) findViewById(R.id.textProductSize);
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
		RelativeLayout mLinColor = (RelativeLayout) findViewById(R.id.linColour);
		mLinSize = (RelativeLayout) findViewById(R.id.linSize);
		mBtnShopOnlineWoolies = (WButton) findViewById(R.id.btnShopOnlineWoolies);
		ImageView mColorArrow = (ImageView) findViewById(R.id.mColorArrow);
		mImCloseProduct = (ImageView) findViewById(R.id.imCloseProduct);
		mImSelectedColor = (SimpleDraweeView) findViewById(R.id.imSelectedColor);
		mLlPagerDots = (LinearLayout) findViewById(R.id.pager_dots);
		ImageView mImColorArrow = (ImageView) findViewById(R.id.imColorArrow);
		mWebDescription = (WrapContentWebView) findViewById(R.id.webDescription);
		ingredientLine = findViewById(R.id.ingredientLine);

		mImNewImage = (SimpleDraweeView) findViewById(R.id.imNewImage);
		mImSave = (SimpleDraweeView) findViewById(R.id.imSave);
		mImReward = (SimpleDraweeView) findViewById(R.id.imReward);
		mVitalityView = (SimpleDraweeView) findViewById(R.id.imVitality);

		mTextSelectColor.setOnClickListener(this);
		mTextSelectSize.setOnClickListener(this);
		mImCloseProduct.setOnClickListener(this);
		mImSelectedColor.setOnClickListener(this);
		mLinSize.setOnClickListener(this);

		mImColorArrow.setOnClickListener(this);
		mColorArrow.setOnClickListener(this);
		mTextProductSize.setOnClickListener(this);
		mLinColor.setOnClickListener(this);
		mBtnShopOnlineWoolies.setOnClickListener(this);
	}

	protected void displayProductDetail(String mProductName, String mProductList, String skuId) {
		try {
			SessionDao sessionDao = new SessionDao(QRActivity.this,
					SessionDao.KEY.STORES_LATEST_PAYLOAD).get();
			mProductJSON = sessionDao.value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		TypeToken<List<WProductDetail>> token = new TypeToken<List<WProductDetail>>() {
		};

		mproductDetail = new Gson().fromJson(mProductList, token.getType());
		assert mproductDetail != null;
		mProduct = mproductDetail.get(0);
		productIsActive(mProduct);
		mOtherSKU = mProduct.otherSkus;
		getDefaultColor(mOtherSKU, skuId);
		mCheckOutLink = mProduct.checkOutLink;
		mDefaultImage = getImageByWidth(mProduct.externalImageRef);
		getHtmlData();
		promoImages(mProduct.promotionImages);
		displayProduct(mProductName);
		initColorParam(mDefaultColor);
		mScrollProductDetail.scrollTo(0, 0);
		String saveText = mProduct.saveText;
		if (TextUtils.isEmpty(saveText)) {
			mTextPromo.setVisibility(View.GONE);
		} else {
			mTextPromo.setVisibility(View.VISIBLE);
			mTextPromo.setText(mProduct.saveText);
		}

		if (mOtherSKU.size() > 1) {
			mColorView.setVisibility(View.VISIBLE);
			llColorSizeContainer.setVisibility(View.VISIBLE);
		} else {
			mColorView.setVisibility(View.GONE);
			llColorSizeContainer.setVisibility(View.GONE);
		}
	}

	protected void getHtmlData() {
		mObjProductDetail = mproductDetail.get(0);

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
		updatePrice();
	}

	public void updatePrice() {
		String fromPrice = String.valueOf(mObjProductDetail.fromPrice);
		String wasPrice = highestSKUWasPrice();
		//set size based on highest normal price
		if (TextUtils.isEmpty(wasPrice)) {
			highestSKUPrice();
		}
		productDetailPriceList(mTextPrice, mTextActualPrice, fromPrice, wasPrice, mObjProductDetail.productType);
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
					if (Utils.isLocationEnabled(QRActivity.this)) {
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
					if (Utils.isLocationEnabled(QRActivity.this)) {
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


	protected String isEmpty(String value) {
		if (TextUtils.isEmpty(value)) {
			return "";
		} else {
			return value;
		}
	}

	protected void initColorParam(String colour) {
		if (TextUtils.isEmpty(colour)) {
			colour = "";
		}
		mTextColour.setText(colour);
		mAuxiliaryImages = null;
		mAuxiliaryImages = new ArrayList<>();
		mAuxiliaryImages.add(mDefaultImage);
		selectedColor(mDefaultColorRef);
		retrieveJson(colour);
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
		OtherSku otherSku = uniqueColorList.get(position);
		String colour = otherSku.colour;
		String defaultUrl = otherSku.externalColourRef;
		if (TextUtils.isEmpty(colour)) {
			colour = "";
		}
		mTextColour.setText(colour);
		mGlobalState.setColorPopUpValue(otherSku);
		mAuxiliaryImages = null;
		mAuxiliaryImages = new ArrayList<>();
		mDefaultImage = getSkuExternalImageRef(colour);
		//show default image when imageUrl is empty
		selectedColor(defaultUrl);
		getSKUDefaultSize(colour);
		retrieveJson(colour);
		String size = mTextSelectSize.getText().toString();
		String price = updatePrice(colour, size);
		String wasPrice = updateWasPrice(colour, size);
		retrieveJson(colour);
		if (!TextUtils.isEmpty(price)) {
			productDetailPriceList(mTextPrice, mTextActualPrice,
					price, wasPrice, mObjProductDetail.productType);
		}
	}


	public String getSkuExternalImageRef(String colour) {
		if (mOtherSKU != null) {
			if (mOtherSKU.size() > 0) {
				List<OtherSku> otherSku = mOtherSKU;
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
		if (mOtherSKU != null) {
			if (mOtherSKU.size() > 0) {
				List<OtherSku> otherSku = mOtherSKU;
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
		if (productIsColored) {
			if (mPColourWindow != null) {
				if (mPColourWindow.isShowing()) {
					mPColourWindow.dismiss();
				}
			}
			colorParams(position);
			mGlobalState.setColorWasPopup(true);

		} else {
			if (mPSizeWindow != null) {
				if (mPSizeWindow.isShowing()) {
					mPSizeWindow.dismiss();
				}
			}
			if (uniqueSizeList != null) {
				OtherSku otherSku = uniqueSizeList.get(position);
				String selectedSize = otherSku.size;
				mTextSelectSize.setText(selectedSize);
				mGlobalState.setSizeWasPopup(true);
				mGlobalState.setSizePopUpValue(otherSku);
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
		ProductSizeAdapter productSizeAdapter;
		ProductColorAdapter productColorAdapter;
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

			productSizeAdapter = new ProductSizeAdapter(uniqueSizeList, this);
			mRecyclerviewSize.addItemDecoration(new SimpleDividerItemDecoration(this));
			mRecyclerviewSize.setLayoutManager(mSlideUpPanelLayoutManager);
			mRecyclerviewSize.setNestedScrollingEnabled(false);
			mRecyclerviewSize.setAdapter(productSizeAdapter);
		} else {

			//sort ascending
			Collections.sort(otherSkus, new Comparator<OtherSku>() {
				@Override
				public int compare(OtherSku lhs, OtherSku rhs) {
					return lhs.colour.compareToIgnoreCase(rhs.colour);
				}
			});

			//remove duplicates
			uniqueColorList = new ArrayList<>();
			for (OtherSku os : otherSkus) {
				if (!colourValueExist(uniqueColorList, os.colour)) {
					uniqueColorList.add(os);
				}
			}
			productColorAdapter = new ProductColorAdapter(uniqueColorList, this);
			mColorRecycleSize.addItemDecoration(new SimpleDividerItemDecoration(this));
			mColorRecycleSize.setLayoutManager(mSlideUpPanelLayoutManager);
			mColorRecycleSize.setNestedScrollingEnabled(false);
			mColorRecycleSize.setAdapter(productColorAdapter);
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
				//ivArrayDotsPager[i].setAlpha(0.4f);
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

	protected void displayProduct(String mProductName) {
		if (TextUtils.isEmpty(mProductName)) {
			return;
		}
		int index = 0;
		for (WProductDetail prod : mproductDetail) {
			if (prod.productName.equals(mProductName)) {
				selectedProduct(index);
			}
			index++;
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


	protected void promoImages(PromotionImages imPromo) {
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

	private void dismissSizeDialog() {
		if (mPSizeWindow != null) {
			if (mPSizeWindow.isShowing()) {
				mPSizeWindow.dismiss();
			}
		}
	}

	private void dismissColourDialog() {
		if (mPColourWindow != null) {
			if (mPColourWindow.isShowing()) {
				mPColourWindow.dismiss();
			}
		}
	}

	private boolean colourValueExist(ArrayList<OtherSku> list, String name) {
		for (OtherSku item : list) {
			if (item.colour.equals(name)) {
				return true;
			}
		}
		return false;
	}

	private boolean sizeValueExist(ArrayList<OtherSku> list, String name) {
		for (OtherSku item : list) {
			if (item.size.equals(name)) {
				return true;
			}
		}
		return false;
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

	protected void getDefaultColor(List<OtherSku> otherSkus, String skuId) {
		for (OtherSku otherSku : otherSkus) {
			if (skuId.equalsIgnoreCase(otherSku.sku)) {
				mDefaultColor = otherSku.colour;
				mDefaultColorRef = otherSku.externalColourRef;
				mDefaultSize = otherSku.size;
			}
		}
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


	private String updatePrice(String colour, String size) {
		String price = "";
		if (mOtherSKU != null) {
			if (mOtherSKU.size() > 0) {
				for (OtherSku option : mOtherSKU) {
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
		if (mOtherSKU != null) {
			if (mOtherSKU.size() > 0) {
				for (OtherSku option : mOtherSKU) {
					if (colour.equalsIgnoreCase(option.colour) &&
							size.equalsIgnoreCase(option.size)) {
						return option.wasPrice;
					}
				}
			}
		}
		return wasPrice;
	}


	public String highestSKUWasPrice() {
		String wasPrice = "";
		ArrayList<Double> priceList = new ArrayList<>();
		for (OtherSku os : mObjProductDetail.otherSkus) {
			if (!TextUtils.isEmpty(os.wasPrice)) {
				priceList.add(Double.valueOf(os.wasPrice));
			}
		}
		if (priceList.size() > 0) {
			wasPrice = String.valueOf(Collections.max(priceList));
			for (OtherSku os : mObjProductDetail.otherSkus) {
				if (wasPrice.equalsIgnoreCase(os.wasPrice)) {
					setSelectedTextSize(os.size);
				}
			}
			return wasPrice;
		}
		return wasPrice;
	}

	public String highestSKUPrice() {
		String price = "";
		ArrayList<Double> priceList = new ArrayList<>();
		for (OtherSku os : mObjProductDetail.otherSkus) {
			if (!TextUtils.isEmpty(os.price)) {
				priceList.add(Double.valueOf(os.price));
			}
		}
		if (priceList.size() > 0) {
			price = String.valueOf(Collections.max(priceList));
			for (OtherSku os : mObjProductDetail.otherSkus) {
				if (price.equalsIgnoreCase(os.price)) {
					setSelectedTextSize(os.size);
				}
			}
			return price;
		}
		return price;
	}

	@Override
	public void SelectedImage(int position, View view) {
		Intent openMultipleImage = new Intent(this, MultipleImageActivity.class);
		openMultipleImage.putExtra("position", position);
		openMultipleImage.putExtra("auxiliaryImages", mAuxiliaryImages);
		startActivity(openMultipleImage);
		overridePendingTransition(0, 0);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finishActivity();
	}

	private void finishActivity() {
		finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}


	private boolean productHasColour() {
		return getColorList().size() > 0 ? true : false;
	}

	private boolean productHasSize() {
		return getSizeList().size() > 0 ? true : false;
	}

	public void colourIntent() {
		mGlobalState.setColourSKUArrayList(getColorList());
		Intent mIntent = new Intent(this, ConfirmColorSizeActivity.class);
		mIntent.putExtra("COLOR_LIST", toJson(getColorList()));
		mIntent.putExtra("OTHERSKU", toJson(mOtherSKU));
		mIntent.putExtra("PRODUCT_HAS_COLOR", true);
		mIntent.putExtra("PRODUCT_HAS_SIZE", true);
		mIntent.putExtra("PRODUCT_NAME", mObjProductDetail.productName);
		startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
		overridePendingTransition(0, 0);
	}


	public void sizeIntent(String colour) {
		mGlobalState.setColourSKUArrayList(getColorList());
		Intent mIntent = new Intent(this, ConfirmColorSizeActivity.class);
		mIntent.putExtra("SELECTED_COLOUR", colour);
		mIntent.putExtra("OTHERSKU", toJson(mOtherSKU));
		mIntent.putExtra("PRODUCT_HAS_COLOR", false);
		mIntent.putExtra("PRODUCT_HAS_SIZE", true);
		mIntent.putExtra("PRODUCT_NAME", mObjProductDetail.productName);
		startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
		overridePendingTransition(0, 0);
	}

	public void colorIntent(String size) {
		Intent mIntent = new Intent(this, ConfirmColorSizeActivity.class);
		mIntent.putExtra("SELECTED_COLOUR", size);
		mIntent.putExtra("COLOR_LIST", toJson(mGlobalState.getColourSKUArrayList()));
		mIntent.putExtra("OTHERSKU", toJson(mOtherSKU));
		mIntent.putExtra("PRODUCT_HAS_COLOR", true);
		mIntent.putExtra("PRODUCT_HAS_SIZE", false);
		mIntent.putExtra("PRODUCT_NAME", mObjProductDetail.productName);
		startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
		overridePendingTransition(0, 0);
	}

	public void sizeIntent() {
		mGlobalState.setColourSKUArrayList(getColorList());
		Intent mIntent = new Intent(this, ConfirmColorSizeActivity.class);
		mIntent.putExtra("COLOR_LIST", toJson(getColorList()));
		mIntent.putExtra("OTHERSKU", toJson(mOtherSKU));
		mIntent.putExtra("PRODUCT_HAS_COLOR", false);
		mIntent.putExtra("PRODUCT_HAS_SIZE", true);
		mIntent.putExtra("PRODUCT_NAME", mObjProductDetail.productName);
		startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
		overridePendingTransition(0, 0);
	}

	public void sizeOnlyIntent(String colour) {
		mGlobalState.setColourSKUArrayList(getColorList());
		Intent mIntent = new Intent(this, ConfirmColorSizeActivity.class);
		mIntent.putExtra("SELECTED_COLOUR", colour);
		mIntent.putExtra("OTHERSKU", toJson(mOtherSKU));
		mIntent.putExtra("PRODUCT_HAS_COLOR", false);
		mIntent.putExtra("PRODUCT_HAS_SIZE", true);
		mIntent.putExtra("PRODUCT_NAME", mObjProductDetail.productName);
		startActivityForResult(mIntent, WGlobalState.SYNC_FIND_IN_STORE);
		overridePendingTransition(0, 0);
	}

	public void noSizeColorIntent() {
		mScrollProductDetail.scrollTo(0, 0);
		mGlobalState.setSelectedSKUId(mSkuId);
		startLocationUpdates();
	}

	private void disableStoreFinder() {
		setLayoutWeight(mBtnShopOnlineWoolies, 1.0f);
		llStoreFinder.setVisibility(View.GONE);
	}

	private void productIsActive(WProductDetail productList) {
		String productType = productList.productType;
		WGlobalState mcs = mWoolworthsApplication.getWGlobalState();
		if ((productType.equalsIgnoreCase("clothingProducts") & mcs.clothingIsEnabled()) || (productType.equalsIgnoreCase("foodProducts") & mcs.isFoodProducts())) {
			setLayoutWeight(mBtnShopOnlineWoolies, 0.5f);
			setLayoutWeight(llStoreFinder, 0.5f);
			llStoreFinder.setVisibility(View.VISIBLE);
		} else {
			setLayoutWeight(mBtnShopOnlineWoolies, 1.0f);
			llStoreFinder.setVisibility(View.GONE);
		}
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


	public void setLayoutWeight(View v, float weight) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		params.weight = weight;
		params.setMarginStart((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
		params.setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
		v.setLayoutParams(params);
	}

	private boolean productHasOneColour() {
		return getColorList().size() == 1;
	}

	private boolean productHasOneSize() {
		return getSizeList().size() == 1;
	}

	private ArrayList<OtherSku> commonSizeList(String colour) {
		List<OtherSku> otherSkus = mOtherSKU;
		ArrayList<OtherSku> commonSizeList = new ArrayList<>();

		if (productHasColour()) { //product has color
			// filter by colour
			ArrayList<OtherSku> sizeList = new ArrayList<>();
			for (OtherSku sku : otherSkus) {
				if (sku.colour.equalsIgnoreCase(colour)) {
					sizeList.add(sku);
				}
			}
			//remove duplicates
			for (OtherSku os : sizeList) {
				if (!sizeValueExist(commonSizeList, os.size)) {
					commonSizeList.add(os);
				}
			}
		} else { // no color found
			//remove duplicates
			ArrayList<OtherSku> sizeList = new ArrayList<>();
			for (OtherSku sku : otherSkus) {
				if (sku.colour.trim().contains(colour)) {
					sizeList.add(sku);
				}
			}

			for (OtherSku os : sizeList) {
				if (!sizeValueExist(commonSizeList, os.size)) {
					commonSizeList.add(os);
				}
			}
		}
		return commonSizeList;
	}

	private void sizeColorSelector() {
		if (mProductHasColour) {
			if (mProductHasOneColour) {
				// one colour only
				String skuColour = getColorList().get(0).colour;
				ArrayList<OtherSku> getSize;
				if (!TextUtils.isEmpty(skuColour)) {
					getSize = commonSizeList(skuColour);
				} else {
					getSize = getSizeList();
				}
				if (getSize.size() > 0) {
					if (getSize.size() == 1) {
						mSkuId = getSize.get(0).sku;
						noSizeColorIntent();
					} else {
						sizeOnlyIntent(skuColour);
					}
				} else {
					mSkuId = productList.sku;
					noSizeColorIntent();
				}
			} else {
				// contain several colours
				colourIntent();
			}
		} else {
			if (mProductHasSize) {
				if (mProductHasOneSize) { //one size
					ArrayList<OtherSku> getSize = getSizeList();
					mSkuId = getSize.get(0).sku;
					noSizeColorIntent();
				} else { // more sizes
					sizeIntent();
				}
			} else {
				mSkuId = productList.sku;
				noSizeColorIntent();
			}
		}
	}


	@Override
	public void PermissionGranted(int request_code) {
		mScrollProductDetail.scrollTo(0, 0);
		Log.i("PERMISSION", "GRANTED");
		if (Utils.isLocationEnabled(QRActivity.this)) {
			mProductHasColour = productHasColour();
			mProductHasSize = productHasSize();
			mProductHasOneColour = productHasOneColour();
			mProductHasOneSize = productHasOneSize();

			boolean colorWasPopUp = mGlobalState.colorWasPopup();
			boolean sizeWasPopUp = mGlobalState.sizeWasPopup();

			OtherSku popupColorSKu = mGlobalState.getColorPopUpValue();
			OtherSku popupSizeSKu = mGlobalState.getSizePopUpValue();

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
				mSkuId = mGlobalState.getSizePopUpValue().sku;
				noSizeColorIntent();
			}

		}
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

	public void startLocationUpdates() {
		showFindInStoreProgress();
		FusedLocationSingleton.getInstance().startLocationUpdates();
		// register observer for location updates
		LocalBroadcastManager.getInstance(QRActivity.this).registerReceiver(mLocationUpdated,
				new IntentFilter(FusedLocationSingleton.INTENT_FILTER_LOCATION_UPDATE));
	}

	public void stopLocationUpdate() {
		// stop location updates
		FusedLocationSingleton.getInstance().stopLocationUpdates();
		// unregister observer
		LocalBroadcastManager.getInstance(QRActivity.this).unregisterReceiver(mLocationUpdated);
	}

	private void showFindInStoreProgress() {
		llStoreFinder.setEnabled(false);
		tvBtnFinder.setVisibility(View.GONE);
		mButtonProgress.getIndeterminateDrawable().setColorFilter(Color.WHITE,
				PorterDuff.Mode.MULTIPLY);
		mButtonProgress.setVisibility(View.VISIBLE);
	}

	private void dismissFindInStoreProgress() {
		llStoreFinder.setEnabled(true);
		tvBtnFinder.setVisibility(View.VISIBLE);
		mButtonProgress.setVisibility(View.GONE);
	}

	private BroadcastReceiver mLocationUpdated = new BroadcastReceiver() {
		@RequiresApi(api = Build.VERSION_CODES.M)
		@Override
		public void onReceive(Context context, final Intent intent) {
			try {
				mLocation = intent.getParcelableExtra(FusedLocationSingleton.LBM_EVENT_LOCATION_UPDATE);
				Utils.saveLastLocation(mLocation, QRActivity.this);
				stopLocationUpdate();
				callbackInStoreFinder();
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	};

	private void callbackInStoreFinder() {
		locationItemTask = new LocationItemTask(QRActivity.this, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				if (object != null) {
					List<StoreDetails> location = ((LocationResponse) object).Locations;
					if (location != null && location.size() > 0) {
						mGlobalState.setStoreDetailsArrayList(location);
						Intent intentInStoreFinder = new Intent(QRActivity.this, WStockFinderActivity.class);
						intentInStoreFinder.putExtra("PRODUCT_NAME", mObjProductDetail.productName);
						startActivity(intentInStoreFinder);
						overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
					} else {
						//no stock error message
						Utils.displayValidationMessage(QRActivity.this, CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.NO_STOCK, "");
					}
				}
				dismissFindInStoreProgress();
			}

			@Override
			public void onFailure(final String e) {
				QRActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dismissFindInStoreProgress();
						Log.e("callbackInStoreFinder", "error " + e);
						if (e.contains("Connect")) {
							//mErrorHandlerView.showToast();
						}
					}
				});
			}
		});
		locationItemTask.execute();
	}

	private void cancelInStoreTask() {
		if (locationItemTask != null) {
			if (!locationItemTask.isCancelled()) {
				locationItemTask.cancel(true);
			}
		}
		dismissFindInStoreProgress();
	}


	private void displayColor(OtherSku otherSku) {
		ArrayList<OtherSku> colorList = commonColorList(otherSku.size);
		if (colorList != null) {
			int colorListSize = colorList.size();
			if (colorListSize > 0) {
				if (colorListSize == 1) {
					// one color only
					mSkuId = colorList.get(0).sku;
					noSizeColorIntent();
				} else {
					// color > 1
					mGlobalState.setColourSKUArrayList(colorList);
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

	private void sizeOnlyIntent(OtherSku otherSku) {
		ArrayList<OtherSku> sizeList = commonSizeList(otherSku.colour);
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


	private ArrayList<OtherSku> commonColorList(String size) {
		List<OtherSku> otherSkus = mOtherSKU;
		ArrayList<OtherSku> commonSizeList = new ArrayList<>();

		if (productHasColour()) { //product has color
			// filter by colour
			ArrayList<OtherSku> sizeList = new ArrayList<>();
			for (OtherSku sku : otherSkus) {
				if (sku.size.equalsIgnoreCase(size)) {
					sizeList.add(sku);
				}
			}

			//remove duplicates
			for (OtherSku os : sizeList) {
				if (!sizeValueExist(commonSizeList, os.colour)) {
					commonSizeList.add(os);
				}
			}
		} else { // no color found
			//remove duplicates
			for (OtherSku os : otherSkus) {
				if (!sizeValueExist(commonSizeList, os.size)) {
					commonSizeList.add(os);
				}
			}
		}
		return commonSizeList;
	}


	public void resetColorSizePopup() {
		mGlobalState.setColorWasPopup(false);
		mGlobalState.setSizeWasPopup(false);
	}
}