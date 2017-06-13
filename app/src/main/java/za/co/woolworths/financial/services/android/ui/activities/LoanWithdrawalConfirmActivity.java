package za.co.woolworths.financial.services.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanResponse;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class LoanWithdrawalConfirmActivity extends BaseActivity implements View.OnClickListener, NetworkChangeListener {

	private ScrollView mScrollLoanWithdrawal;
	private SharePreferenceHelper mSharePreferenceHelper;
	private WTextView mTextDrawnAmount;
	private WTextView mTextMonths;
	private WTextView mTextAdditionalMonthAmount;
	private WTextView mBtnConfirm;
	private Integer installment_amount;
	private String mDrawanDownAmount;
	private int mRepaymentPeriod;
	private ProgressBar mConfirmProgressBar;
	private AsyncTask<String, String, AuthoriseLoanResponse> authoriseLoanRequest;
	private int minDrawnDownAmount;
	ErrorHandlerView mErrorHandlerView;
	private NetworkChangeListener networkChangeListener;
	private BroadcastReceiver connectionBroadcast;
	private boolean loanWithdrawalClicked;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(LoanWithdrawalConfirmActivity.this, R.color.purple);
		setContentView(R.layout.loan_withdrawal_activity);

		mErrorHandlerView = new ErrorHandlerView(this, (RelativeLayout) findViewById(R.id.no_connection_layout));
		mSharePreferenceHelper = SharePreferenceHelper.getInstance(LoanWithdrawalConfirmActivity.this);
		Bundle intent = getIntent().getExtras();
		if (intent != null) {
			mDrawanDownAmount = intent.getString("drawnDownAmount");
			minDrawnDownAmount = intent.getInt("minDrawnDownAmount");
			mRepaymentPeriod = intent.getInt("repaymentPeriod");
		}

		String sInstallment_amount = mSharePreferenceHelper.getValue("lw_installment_amount");
		if (!TextUtils.isEmpty(sInstallment_amount))
			installment_amount = Integer.valueOf(mSharePreferenceHelper.getValue("lw_installment_amount"));
		setActionBar();
		initViews();
		clickListener();
		setContent();
		networkListener();
	}

	private void networkListener() {
		try {
			networkChangeListener = LoanWithdrawalConfirmActivity.this;
		} catch (ClassCastException ignored) {
		}
		connectionBroadcast = Utils.connectionBroadCast(LoanWithdrawalConfirmActivity.this, networkChangeListener);
	}

	private void initViews() {
		mScrollLoanWithdrawal = (ScrollView) findViewById(R.id.scrollLoanWithdrawal);
		mTextDrawnAmount = (WTextView) findViewById(R.id.currencyType);
		mTextMonths = (WTextView) findViewById(R.id.textMonths);
		mTextAdditionalMonthAmount = (WTextView) findViewById(R.id.textAdditionalMonthAmount);
		mBtnConfirm = (WTextView) findViewById(R.id.btnConfirm);
		mConfirmProgressBar = (ProgressBar) findViewById(R.id.mConfirmProgressBar);
	}

	private void clickListener() {
		mBtnConfirm.setOnClickListener(this);
	}

	private void setActionBar() {
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		ActionBar mActionBar = getSupportActionBar();
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(false);
			mActionBar.setDisplayUseLogoEnabled(false);
			mActionBar.setHomeAsUpIndicator(R.drawable.back_white);
		}
	}

	private void setContent() {
		mScrollLoanWithdrawal.setVisibility(View.VISIBLE);
		mTextDrawnAmount.setText(mDrawanDownAmount);
		mTextMonths.setText(String.valueOf(mRepaymentPeriod) + " months");
		mTextAdditionalMonthAmount.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(installment_amount), 1, this)));
	}

	@Override
	public void onBackPressed() {
		previousActivity();
	}

	public void previousActivity() {
		Intent openLoanWithdrawal = new Intent(LoanWithdrawalConfirmActivity.this, LoanWithdrawalActivity.class);
		openLoanWithdrawal.putExtra("minDrawnDownAmount", minDrawnDownAmount * 100);
		startActivity(openLoanWithdrawal);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		finish();
	}

	public void authoriseLoanWithdrawal() {
		authoriseLoanRequest = authoriseLoanWithdrawalAPI();
		authoriseLoanRequest.execute();
	}

	public HttpAsyncTask<String, String, AuthoriseLoanResponse> authoriseLoanWithdrawalAPI() {
		return new HttpAsyncTask<String, String, AuthoriseLoanResponse>() {
			@Override
			protected AuthoriseLoanResponse httpDoInBackground(String... params) {
				int productOfferingId = Integer.valueOf(mSharePreferenceHelper.getValue("lw_product_offering_id"));
				int drawDownAmount = Integer.valueOf(mSharePreferenceHelper.getValue("lw_amount_drawn_cent"));
				String sCreditLimit = (mSharePreferenceHelper.getValue("lw_credit_limit")).replace("R ", "").replace(" ", "").replace("R", "");
				int creditLimit = 0;
				if (sCreditLimit.length() > 0) {
					creditLimit = Integer.parseInt(sCreditLimit.substring(0, sCreditLimit.indexOf(".")));
				}
				AuthoriseLoanRequest authoriseLoanRequest
						= new AuthoriseLoanRequest(productOfferingId, drawDownAmount, mRepaymentPeriod, installment_amount, creditLimit * 100);
				return ((WoolworthsApplication) LoanWithdrawalConfirmActivity.this.getApplication()).getApi().authoriseLoan
						(authoriseLoanRequest);
			}

			@Override
			protected void onPreExecute() {
				showProgressBar();
				super.onPreExecute();
			}

			@Override
			protected void onPostExecute(AuthoriseLoanResponse authoriseLoanResponse) {
				super.onPostExecute(authoriseLoanResponse);
				try {
					hideProgressBar();
					if (authoriseLoanResponse.httpCode == 200) {
						Intent intent = new Intent(LoanWithdrawalConfirmActivity.this, LoanWithdrawalSuccessActivity.class);
						startActivity(intent);
						finish();
						setLoanWithdrawalClicked(false);
					} else {
						String desc = authoriseLoanResponse.response.desc;
						if (desc != null && !TextUtils.isEmpty(desc)) {
							Utils.displayValidationMessage(LoanWithdrawalConfirmActivity.this,
									TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
									desc);
						}
					}

				} catch (Exception ignored) {
				}
			}

			@Override
			protected AuthoriseLoanResponse httpError(final String errorMessage, HttpErrorCode httpErrorCode) {
				AuthoriseLoanResponse authoriseLoanResponse = new AuthoriseLoanResponse();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hideProgressBar();
					}
				});
				return authoriseLoanResponse;
			}

			@Override
			protected Class<AuthoriseLoanResponse> httpDoInBackgroundReturnType() {
				return AuthoriseLoanResponse.class;
			}
		};
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnConfirm:
				setLoanWithdrawalClicked(true);
				hideKeyboard();
				authoriseLoanWithdrawal();
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				previousActivity();
				return true;
		}
		return false;
	}


	//To remove negative signs from negative balance and add "CR" after the negative balance
	public String removeNegativeSymbol(SpannableString amount) {
		String currentAmount = amount.toString();
		if (currentAmount.contains("-")) {
			currentAmount = currentAmount.replace("-", "") + " CR";
		}
		return currentAmount;
	}

	/**
	 * Hides the soft keyboard
	 */
	public void hideKeyboard() {
		try {
			if (getCurrentFocus() != null) {
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			}
		} catch (NullPointerException ignored) {
		}
	}

	private void showProgressBar() {
		mConfirmProgressBar.setVisibility(View.VISIBLE);
		mBtnConfirm.setVisibility(View.GONE);
		mConfirmProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
	}

	private void hideProgressBar() {
		mConfirmProgressBar.setVisibility(View.GONE);
		mBtnConfirm.setVisibility(View.VISIBLE);
		mConfirmProgressBar.getIndeterminateDrawable().setColorFilter(null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (authoriseLoanRequest != null) {
			if (!authoriseLoanRequest.isCancelled()) {
				authoriseLoanRequest.cancel(true);
			}
		}
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
				if (new ConnectionDetector().isOnline(LoanWithdrawalConfirmActivity.this)) {
					if (isLoanWithdrawalClicked()) {
						hideKeyboard();
						authoriseLoanWithdrawal();
						setLoanWithdrawalClicked(false);
					}
				} else {
					mErrorHandlerView.showToast();
				}
			}
		});
	}

	public boolean isLoanWithdrawalClicked() {
		return loanWithdrawalClicked;
	}

	public void setLoanWithdrawalClicked(boolean pLoanWithdrawalClicked) {
		loanWithdrawalClicked = pLoanWithdrawalClicked;
	}
}
