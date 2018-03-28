package za.co.woolworths.financial.services.android.util.zxing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.MultiFormatReader;
import com.pacific.mvc.Activity;
import com.trello.rxlifecycle.ActivityEvent;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.EnterBarcodeActivity;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment;
import za.co.woolworths.financial.services.android.ui.views.NestedScrollableViewHelper;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.PermissionResultCallback;
import za.co.woolworths.financial.services.android.util.PermissionUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class QRActivity extends Activity<QRModel> implements View.OnClickListener, PermissionResultCallback {

	public static final int CODE_PICK_IMAGE = 0x100;
	private BaseCameraManager cameraManager;
	public final int ZBAR_PERMS_REQUEST_CODE = 12345678;
	private WButton mBtnManual;
	private TextView mTextInfo;
	private ProgressBar mProgressBar;
	private QRCodeView qRview;
	private SlidingUpPanelLayout mSlideUpPanelLayout;
	public String mCheckOutLink;
	public NestedScrollView mScrollProductDetail;
	private SlidingUpPanelLayout.PanelState mPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
	private final String MBGPRODUCTDETAIL = "PRODUCT_DETAIL";
	private final String MBGPRODUCT = "PRODUCT";
	private ErrorHandlerView mErrorHandlerView;
	public WoolworthsApplication mWoolworthsApplication;
	private PermissionUtils permissionUtils;
	private ArrayList<String> permissions;
	private String mCurrentBgTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this, R.color.black);
		setContentView(R.layout.barcode_scanner_layout);
		setupToolbar();

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
		slideUpPanel();
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
								QRActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
		qRview = findViewById(R.id.qr_view);
		mSlideUpPanelLayout = findViewById(R.id.sliding_layout);
		mTextInfo = findViewById(R.id.textInfo);
		mProgressBar = findViewById(R.id.ppBar);
		mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
		mBtnManual = findViewById(R.id.btnManual);
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
		Toolbar toolbar = findViewById(R.id.toolbar);
		WTextView mTextToolbar = findViewById(R.id.toolbarText);
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

	private void finishActivity() {
		Window window = getWindow();
		if (window != null) {
			window.getDecorView().setBackgroundColor(Color.BLACK);
		}
		finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	@Override
	public void onClick(View v) {
		MultiClickPreventer.preventMultiClick(v);
		switch (v.getId()) {
			case R.id.imClose:
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
					Utils.displayValidationMessage(QRActivity.this, CustomPopUpWindow.MODAL_LAYOUT.LOCATION_OFF, "");
				}
				break;
		}
	}

	private void getProductDetail(final String productId, final String skuId) {
		mCurrentBgTask = MBGPRODUCTDETAIL;
		((WoolworthsApplication) QRActivity.this.getApplication()).getAsyncApi().getProductDetail(productId, skuId, new Callback<String>() {
			@Override
			public void success(String strProduct, retrofit.client.Response response) {
				hideProgressBar();
				WProduct wProduct = Utils.stringToJson(QRActivity.this, strProduct);
				if (wProduct != null) {
					switch (wProduct.httpCode) {
						case 200:
							ArrayList<WProductDetail> mProductList;
							WProductDetail productList = wProduct.product;
							mProductList = new ArrayList<>();
							if (productList != null) {
								mProductList.add(productList);
							}
							if (mProductList.size() > 0 && mProductList.get(0).productId != null) {
								GsonBuilder builder = new GsonBuilder();
								Gson gson = builder.create();
								ProductDetailFragment productDetailFragment = new ProductDetailFragment();
								String strProductList = gson.toJson(mProductList.get(0));
								Bundle bundle = new Bundle();
								bundle.putString("strProductList", strProductList);
								bundle.putString("strProductCategory", mProductList.get(0).productName);
								bundle.putString("productResponse", strProduct);
								bundle.putBoolean("fetchFromJson", true);
								productDetailFragment.setArguments(bundle);
								FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
								transaction.replace(R.id.barcode_fragment, productDetailFragment).commit();
								mSlideUpPanelLayout.setAnchorPoint(1.0f);
								mSlideUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
								mSlideUpPanelLayout.setScrollableViewHelper(new NestedScrollableViewHelper(mScrollProductDetail));
							}
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
		Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.BARCODE_ERROR, "");
	}

	private void resumeScan() {
		model.onResume();
		if (qRview != null)
			qRview.setBackgroundColor(Color.TRANSPARENT);
	}

	@Override
	public void PermissionGranted(int request_code) {

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

	@Override
	public void onBackPressed() {
		finishActivity();
	}
}