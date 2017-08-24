package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		updateStatusBarBackground(EnterBarcodeActivity.this);
		mContext = this;
		setContentView(R.layout.enter_barcode_activity);
		initUI();
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
		mToolbar = (Toolbar) findViewById(R.id.mToolbar);
		mEditBarcodeNumber = (WLoanEditTextView) findViewById(R.id.editBarcodeNumber);
		mBtnBarcodeConfirm = (WButton) findViewById(R.id.btnBarcodeConfirm);
		mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);
		mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
		mTextInfo = (WTextView) findViewById(R.id.textInfo);
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
				CustomPopUpDialogManager.VALIDATION_MESSAGE_LIST.BARCODE_ERROR, "");
	}

	private void getProductDetail(final String productId, final String skuId) {
		((WoolworthsApplication) EnterBarcodeActivity.this.getApplication()).getAsyncApi()
				.getProductDetail(productId,
						skuId, new Callback<String>() {
							@Override
							public void success(String strProduct, retrofit.client.Response response) {
								hideProgressBar();
								WProduct wProduct = Utils.stringToJson(mContext, strProduct);
								if (wProduct != null) {
									switch (wProduct.httpCode) {
										case 200:
											hideSoftKeyboard();
											ArrayList<WProductDetail> mProductList;
											WProductDetail productList = wProduct.product;
											mProductList = new ArrayList<>();
											if (productList != null) {
												mProductList.add(productList);
											}
											GsonBuilder builder = new GsonBuilder();
											Gson gson = builder.create();
											Intent openDetailView = new Intent(mContext, ProductDetailActivity.class);
											openDetailView.putExtra("product_name", mProductList.get(0).productName);
											openDetailView.putExtra("product_detail", gson.toJson(mProductList));
											startActivity(openDetailView);
											overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
											break;

										default:
											handleError();
											break;
									}
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
}
