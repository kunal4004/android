package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ProductList;
import za.co.woolworths.financial.services.android.models.dto.ProductView;
import za.co.woolworths.financial.services.android.models.dto.WProduct;
import za.co.woolworths.financial.services.android.models.dto.WProductDetail;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.ProductDetailFragment;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;

public class EnterBarcodeActivity extends AppCompatActivity {

	private Toolbar mToolbar;
	private WLoanEditTextView mEditBarcodeNumber;
	private ProgressBar mProgressBar;
	private WTextView mTextInfo;
	private EnterBarcodeActivity mContext;

	private WButton mBtnBarcodeConfirm;
	private ErrorHandlerView mErrorHandlerView;
	private SlidingUpPanelLayout mSlideUpPanel;

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		updateStatusBarBackground(EnterBarcodeActivity.this);
		mContext = this;
		setContentView(R.layout.enter_barcode_activity);
		initUI();
		slideUpPanelListener();
		setActionBar();
		mErrorHandlerView = new ErrorHandlerView(this
				, (RelativeLayout) findViewById(R.id.no_connection_layout));

		mBtnBarcodeConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEditBarcodeNumber.getText().length() > 0) {
					getProductDetail();
				}
			}
		});

		mEditBarcodeNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					getProductDetail();
					handled = true;
				}
				return handled;
			}
		});

		mEditBarcodeNumber.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {

				if (s.length() > 0) {
					mBtnBarcodeConfirm.setVisibility(View.VISIBLE);
				} else {
					mBtnBarcodeConfirm.setVisibility(View.GONE);
				}
			}
		});

		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(EnterBarcodeActivity.this)) {
					getProductDetail();
				}
			}
		});
		showSoftKeyboard();
	}

	private void initUI() {
		mToolbar = findViewById(R.id.mToolbar);
		mSlideUpPanel = findViewById(R.id.slideUpPanel);
		mEditBarcodeNumber = findViewById(R.id.editBarcodeNumber);
		mBtnBarcodeConfirm = findViewById(R.id.btnBarcodeConfirm);
		mProgressBar = findViewById(R.id.mProgressBar);
		mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
		mTextInfo = findViewById(R.id.textInfo);
	}

	private void slidePanelUp() {
		mSlideUpPanel.setAnchorPoint(1.0f);
		mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
	}

	private void setActionBar() {
		setSupportActionBar(mToolbar);
		ActionBar mActionBar = getSupportActionBar();
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(false);
			mActionBar.setDisplayUseLogoEnabled(false);
			mActionBar.setHomeAsUpIndicator(R.drawable.back_white);
		}
	}

	@Override
	public void onBackPressed() {
		finishActivity();
	}

	@Override
	protected void onResume() {
		super.onResume();
		showSoftKeyboard();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finishActivity();
				return true;
		}
		return false;
	}

	private void finishActivity() {
		finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	/**
	 * Shows the soft keyboard
	 */
	public void showSoftKeyboard() {
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	private void hideSoftKeyboard() {
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	public HttpAsyncTask<String, String, ProductView> getProductRequest(final String query) {
		return new HttpAsyncTask<String, String, ProductView>() {
			@Override
			protected ProductView httpDoInBackground(String... params) {
				return ((WoolworthsApplication) getApplication()).getApi()
						.getProductSearchList(query,
								true, 0, Utils.PAGE_SIZE);
			}

			@Override
			protected ProductView httpError(final String errorMessage, HttpErrorCode
					httpErrorCode) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hideSoftKeyboard();
						hideProgressBar();
						mErrorHandlerView.networkFailureHandler(errorMessage);
					}
				});
				return new ProductView();
			}

			@Override
			protected void onPostExecute(ProductView product) {
				super.onPostExecute(product);
				List<ProductList> mProduct = product.products;
				if (mProduct != null) {
					if (mProduct.size() > 0 && mProduct.get(0).productId != null) {
						getProductDetail(mProduct.get(0).productId, mProduct.get(0).sku);
					} else {
						hideSoftKeyboard();
						hideProgressBar();
						errorScanCode();
					}
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showProgressBar();
				mErrorHandlerView.hideErrorHandlerLayout();
			}

			@Override
			protected Class<ProductView> httpDoInBackgroundReturnType() {
				return ProductView.class;
			}
		};
	}

	@Override
	public void finish() {
		super.finish();
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public void updateStatusBarBackground(Activity activity) {
		Window window = activity.getWindow();
		window.setStatusBarColor(ContextCompat.getColor(activity, R.color.black));
		View decor = activity.getWindow().getDecorView();
		decor.setSystemUiVisibility(0);
	}

	private void showProgressBar() {
		mProgressBar.setVisibility(View.VISIBLE);
		mTextInfo.setVisibility(View.GONE);
	}

	private void hideProgressBar() {
		mProgressBar.setVisibility(View.GONE);
		mTextInfo.setVisibility(View.VISIBLE);
	}

	private void errorScanCode() {
		Utils.displayValidationMessage(this,
				CustomPopUpWindow.MODAL_LAYOUT.BARCODE_ERROR, "");
	}

	private void getProductDetail(final String productId, final String skuId) {
		((WoolworthsApplication) EnterBarcodeActivity.this.getApplication()).getAsyncApi()
				.getProductDetail(productId,
						skuId, new Callback<String>() {
							@Override
							public void success(String strProduct, retrofit.client.Response response) {
								hideProgressBar();
								WProduct wProduct = Utils.stringToJson(mContext, strProduct);
								if (wProduct != null) switch (wProduct.httpCode) {
									case 200:
										hideSoftKeyboard();
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
											FragmentTransaction transaction = mContext.getSupportFragmentManager().beginTransaction();
											transaction.replace(R.id.barcode_fragment, productDetailFragment).commit();
											slidePanelUp();
										}
										break;

									default:
										handleError();
										break;
								}
							}

							@Override
							public void failure(RetrofitError error) {

							}
						});
	}

	private void handleError() {
		hideProgressBar();
		hideSoftKeyboard();
		errorScanCode();
	}


	private void getProductDetail() {
		if (mEditBarcodeNumber.getText().length() > 0) {
			getProductRequest(mEditBarcodeNumber.getText().toString()).execute();
		}
	}

	public void slideUpPanelListener() {
		mSlideUpPanel.setFadeOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			}
		});
		mSlideUpPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				if (slideOffset == 0.0) {
					mSlideUpPanel.setAnchorPoint(1.0f);
				}
			}

			@Override
			public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
											SlidingUpPanelLayout.PanelState newState) {
				switch (newState) {
					case COLLAPSED:
						showStatusBar();
						break;

					case EXPANDED:
						hideStatusBar();
						break;
					default:
						break;
				}
			}
		});
	}

	private void hideStatusBar() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void showStatusBar() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

}
