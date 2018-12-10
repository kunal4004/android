package za.co.woolworths.financial.services.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.StringTokenizer;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.Utils;

public class LoanWithdrawalActivity extends BaseActivity implements NetworkChangeListener {

	private WLoanEditTextView mEditWithdrawalAmount;
	private WTextView mTextAvailableFund;
	private WTextView mTextCreditLimit;
	private Menu mMenu;
	private RelativeLayout mRelLoanWithdrawal;
	private SharePreferenceHelper mSharePreferenceHelper;
	Handler handler = new Handler();//not nice
	private String mDrawnDownAmount;
	private String mCreditLimit;
	private String mAvailableFunds;
	private int wminDrawnDownAmount = 0;
	private ProgressBar mLoanWithdrawalProgress;
	private boolean arrowIsVisible = false;
	private AsyncTask<String, String, IssueLoanResponse> issueLoanRequest;
	private ErrorHandlerView mErrorHandlerView;
	private NetworkChangeListener networkChangeListener;
	private BroadcastReceiver connectionBroadcast;
	private boolean loanWithdrawalClicked = false;
	private boolean mBackSpace;
	private LinearLayout llDrawndownAmount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(LoanWithdrawalActivity.this, R.color.purple);
		setContentView(R.layout.loan_withdrawal_activity);
		mErrorHandlerView = new ErrorHandlerView(this, (RelativeLayout) findViewById(R.id.no_connection_layout));
		mSharePreferenceHelper = SharePreferenceHelper.getInstance(LoanWithdrawalActivity.this);

		try {
			networkChangeListener = LoanWithdrawalActivity.this;
		} catch (ClassCastException ignored) {
		}
		connectionBroadcast = Utils.connectionBroadCast(LoanWithdrawalActivity.this, networkChangeListener);

		setActionBar();
		initViews();
		setContent();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			wminDrawnDownAmount = bundle.getInt("minDrawnDownAmount") / 100;
		}

		String shareDrawDownAmount = mSharePreferenceHelper.getValue("lw_amount_drawn_cent");
		if (TextUtils.isEmpty(shareDrawDownAmount)) {
			mEditWithdrawalAmount.setText("");
		} else {
			String drawnDownAmount = String.valueOf(Integer.valueOf(shareDrawDownAmount) / 100);
			mEditWithdrawalAmount.setText(drawnDownAmount);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					menuItemVisible(mMenu, true);
				}
			}, 1000);
		}
	}

	private void initViews() {
		mTextAvailableFund = (WTextView) findViewById(R.id.textAvailableFunds);
		mTextCreditLimit = (WTextView) findViewById(R.id.textCreditLimit);
		mEditWithdrawalAmount = (WLoanEditTextView) findViewById(R.id.editWithdrawAmount);
		llDrawndownAmount = (LinearLayout) findViewById(R.id.llDrawndownAmount);
		mRelLoanWithdrawal = (RelativeLayout) findViewById(R.id.relLoanWithdrawal);
		mLoanWithdrawalProgress = (ProgressBar) findViewById(R.id.mLoanWithdrawalProgress);
		ScrollView mScrollLoanWithdrawal = (ScrollView) findViewById(R.id.scrollLoanWithdrawal);
		RelativeLayout linLoanWithdrawalSuccess = (RelativeLayout) findViewById(R.id.linLoanWithdrawalSuccess);
		mScrollLoanWithdrawal.setVisibility(View.GONE);
		linLoanWithdrawalSuccess.setVisibility(View.GONE);
	}

	private void setActionBar() {
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		ActionBar mActionBar = getSupportActionBar();
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowTitleEnabled(false);
			mActionBar.setDisplayUseLogoEnabled(false);
			mActionBar.setHomeAsUpIndicator(R.drawable.close_white);
		}
	}

	private void setContent() {
		mEditWithdrawalAmount.setSelection(mEditWithdrawalAmount.getText().length());
		mTextAvailableFund.setText(mSharePreferenceHelper.getValue("lw_available_fund"));
		mTextCreditLimit.setText(mSharePreferenceHelper.getValue("lw_credit_limit"));
		mEditWithdrawalAmount.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
		mRelLoanWithdrawal.setVisibility(View.VISIBLE);
		mEditWithdrawalAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					handled = true;
					if (arrowIsVisible) {
						setLoanWithdrawalClick(true);
						setAmount();
					}
				}
				return handled;
			}
		});

		mEditWithdrawalAmount.addTextChangedListener(new NumberTextWatcherForThousand
				(mEditWithdrawalAmount));

		WLoanEditTextView.OnKeyPreImeListener onKeyPreImeListener =
				new WLoanEditTextView.OnKeyPreImeListener() {
					@Override
					public void onBackPressed() {
						LoanWithdrawalActivity.this.onBackPressed();
					}
				};

		mEditWithdrawalAmount.setOnKeyPreImeListener(onKeyPreImeListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.loan_withdrawal_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.mMenu = menu;
		menuItemVisible(menu, false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				previousScreen();
				return true;
			case R.id.itemNextArrow:
				if (NetworkManager.getInstance().isConnectedToNetwork(this)) {
					showSoftKeyboard();
				}
				setLoanWithdrawalClick(true);
				setAmount();
				break;
		}
		return false;
	}

	private void setAmount() {
		if (getDrawnDownAmount() < wminDrawnDownAmount) {
			Utils.displayValidationMessage(LoanWithdrawalActivity.this,
					CustomPopUpWindow.MODAL_LAYOUT.LOW_LOAN_AMOUNT,
					String.valueOf(wminDrawnDownAmount));
		} else if (getDrawnDownAmount() >= wminDrawnDownAmount
				&& getDrawnDownAmount() <= getAvailableFund()) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mDrawnDownAmount = mEditWithdrawalAmount.getText().toString();
					mCreditLimit = amountInCents(mTextCreditLimit.getText().toString());
					mAvailableFunds = amountInCents(mTextAvailableFund.getText().toString());
					loanRequest();
				}
			}, 200);
		} else {
			Utils.displayValidationMessage(LoanWithdrawalActivity.this,
					CustomPopUpWindow.MODAL_LAYOUT.HIGH_LOAN_AMOUNT, "");
		}
	}

	@Override
	public void onBackPressed() {
		previousScreen();
	}

	public void previousScreen() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				hideKeyboard();
			}
		}, 100);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
				overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
			}
		}, 200);
	}

	public void menuItemVisible(Menu menu, boolean isVisible) {
		arrowIsVisible = isVisible;
		try {
			MenuItem menuItem = menu.findItem(R.id.itemNextArrow);
			if (isVisible) {
				menuItem.setEnabled(true);
				menuItem.getIcon().setAlpha(255);
			} else {
				menuItem.setEnabled(false);
				menuItem.getIcon().setAlpha(50);
			}
		} catch (Exception ignored) {
		}
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

	private void loanRequest() {
		issueLoanRequest = loanRequestAPI();
		issueLoanRequest.execute();
	}

	private HttpAsyncTask<String, String, IssueLoanResponse> loanRequestAPI() {
		return new HttpAsyncTask<String, String, IssueLoanResponse>() {
			@Override
			protected IssueLoanResponse httpDoInBackground(String... params) {
				int productOfferingId = Integer.valueOf(mSharePreferenceHelper.getValue("lw_product_offering_id"));
				String withdrawalAmount = mEditWithdrawalAmount.getText().toString();
				String removeDot = withdrawalAmount.substring(0, withdrawalAmount.indexOf("."));
				String sDrawDownAmount = removeDot.replaceAll("[\\D]", "");
				int drawnDownAmount = Integer.parseInt(sDrawDownAmount);
				int drawnDownAmountCent = Integer.parseInt(withdrawalAmount.replaceAll("[\\D]", ""));
				int creditLimit = getCreditAmount();
				String repaymentPeriod = String.valueOf(repaymentPeriod(drawnDownAmount));
				mSharePreferenceHelper.save(repaymentPeriod, "lw_months");
				mSharePreferenceHelper.save(String.valueOf(drawnDownAmountCent), "lw_amount_drawn_cent");
				IssueLoanRequest issueLoanRequest = new IssueLoanRequest(productOfferingId,
						drawnDownAmountCent,
						repaymentPeriod(creditLimit),
						creditLimit);
				return ((WoolworthsApplication) LoanWithdrawalActivity.this.getApplication())
						.getApi().issueLoan
								(issueLoanRequest);
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showSoftKeyboard();
				showProgressBar();
			}

			@Override
			protected void onPostExecute(IssueLoanResponse issueLoanResponse) {
				super.onPostExecute(issueLoanResponse);
				try {
					hideProgressBar();
					int httpCode = issueLoanResponse.httpCode;
					switch (httpCode) {
						case 200:
							loanWithdrawalClicked = false;
							mSharePreferenceHelper.save(String.valueOf(issueLoanResponse.installmentAmount), "lw_installment_amount");
							String mAmount = mEditWithdrawalAmount.getText().toString();
							Intent openConfirmWithdrawal = new Intent(LoanWithdrawalActivity.this, LoanWithdrawalConfirmActivity.class);
							openConfirmWithdrawal.putExtra("drawnDownAmount", mAmount);
							openConfirmWithdrawal.putExtra("availableFunds", mAvailableFunds);
							openConfirmWithdrawal.putExtra("creditLimit", mCreditLimit);
							openConfirmWithdrawal.putExtra("minDrawnDownAmount", wminDrawnDownAmount);
							openConfirmWithdrawal.putExtra("repaymentPeriod", repaymentPeriod(getCreditAmount()));
							startActivity(openConfirmWithdrawal);
							finish();
							break;
						case 440:
							SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, issueLoanResponse.response.stsParams, LoanWithdrawalActivity.this);
							break;

						default:
							try {
								hideKeyboard();
								String responseDesc = issueLoanResponse.response.desc;
								if (responseDesc != null) {
									if (!TextUtils.isEmpty(responseDesc)) {
										loanWithdrawalClicked = false;
										Utils.displayValidationMessage(LoanWithdrawalActivity.this,
												CustomPopUpWindow.MODAL_LAYOUT.ERROR,
												responseDesc);
									}
								}
							} catch (NullPointerException ignored) {
								showSoftKeyboard();
							}
							break;

					}
				} catch (
						Exception ignored)

				{
				}
			}

			@Override
			protected IssueLoanResponse httpError(final String errorMessage, HttpErrorCode
					httpErrorCode) {
				IssueLoanResponse issueLoanResponse = new IssueLoanResponse();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setLoanWithdrawalClick(true);
						hideProgressBar();
						showSoftKeyboard();
					}
				});
				return issueLoanResponse;
			}

			@Override
			protected Class<IssueLoanResponse> httpDoInBackgroundReturnType() {
				return IssueLoanResponse.class;
			}
		}

				;
	}

	private int repaymentPeriod(int amount) {
		if (amount < 1000000) {
			return 36;
		} else {
			return 60;
		}
	}

	public int getCreditAmount() {
		String creditAmount = mSharePreferenceHelper.getValue("lw_credit_limit").replaceAll("[\\D]", "");
		if (TextUtils.isEmpty(creditAmount))
			return 0;
		else
			return Integer.valueOf(creditAmount);
	}

	public int getAvailableFund() {
		String availableFund = mSharePreferenceHelper.getValue("lw_available_fund");
		String mAvaibleFund = availableFund.substring(0, availableFund.indexOf(".")).replaceAll("[\\D]", "");
		if (TextUtils.isEmpty(mAvaibleFund))
			return 0;
		else
			return Integer.valueOf(mAvaibleFund);
	}

	public int getDrawnDownAmount() {
		String mDrawnAmount = mEditWithdrawalAmount.getText().toString();
		String mCurrentDrawnAmount = mDrawnAmount.substring(0, mDrawnAmount.indexOf(".")).replaceAll("[\\D]", "");
		if (TextUtils.isEmpty(mCurrentDrawnAmount)) {
			return 0;
		} else {
			return Integer.valueOf(mCurrentDrawnAmount);
		}
	}

	public String amountInCents(String edit) {
		return edit.replaceAll("[\\D]", "");
	}

	public void showSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(mEditWithdrawalAmount, InputMethodManager.SHOW_IMPLICIT);
	}

	private void showProgressBar() {
		mLoanWithdrawalProgress.setVisibility(View.VISIBLE);
		mLoanWithdrawalProgress.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
		mEditWithdrawalAmount.setVisibility(View.GONE);
		llDrawndownAmount.setVisibility(View.GONE);
		menuItemVisible(mMenu, false);
	}

	private void hideProgressBar() {
		mLoanWithdrawalProgress.setVisibility(View.GONE);
		mLoanWithdrawalProgress.getIndeterminateDrawable().setColorFilter(null);
		mEditWithdrawalAmount.setVisibility(View.VISIBLE);
		llDrawndownAmount.setVisibility(View.VISIBLE);
		menuItemVisible(mMenu, true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (issueLoanRequest != null) {
			if (!issueLoanRequest.isCancelled()) {
				issueLoanRequest.cancel(true);
			}
		}
	}


	public class NumberTextWatcherForThousand implements TextWatcher {
		int previousLength;
		boolean backSpace;
		EditText edtLoanWiddrawal;

		private NumberTextWatcherForThousand(EditText edtLoanWiddrawal) {
			this.edtLoanWiddrawal = edtLoanWiddrawal;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			previousLength = s.length();
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (TextUtils.isEmpty(s)) {
				menuItemVisible(mMenu, false);
			} else {
				menuItemVisible(mMenu, true);
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			int inilen, endlen;
			inilen = edtLoanWiddrawal.getText().length();
			backSpace = previousLength > s.length();
			if (backSpace) {
				edtLoanWiddrawal.removeTextChangedListener(this);
				if (s.length() > 0) {
					String loanAmount = s.toString()
							.replace(".0", "")
							.replace(" ", "");
					loanAmount = loanAmount.substring(0, loanAmount.length() - 1);
					if (TextUtils.isEmpty(loanAmount)) {
						edtLoanWiddrawal.setText("");
						backSpace = false;
						menuItemVisible(mMenu, false);
						edtLoanWiddrawal.addTextChangedListener(this);
						return;
					}
					int cp = edtLoanWiddrawal.getSelectionStart();
					loanAmount = getDecimalFormat(trimCommaOfString(loanAmount)).concat(".00");
					edtLoanWiddrawal.setText(loanAmount);
					endlen = edtLoanWiddrawal.getText().length();
					int sel = (cp + (endlen - inilen));
					if (sel > 0) {
						edtLoanWiddrawal.setSelection(endlen);
					}
				}
				edtLoanWiddrawal.addTextChangedListener(this);
			} else {
				try {
					edtLoanWiddrawal.removeTextChangedListener(this);
					String value = edtLoanWiddrawal.getText().toString();

					if (!value.equals("")) {

						if (value.startsWith(".")) { //adds "0." when only "." is pressed on beginning of writing
							edtLoanWiddrawal.setText("0.");
						}
						if (value.startsWith("0") && !value.startsWith("0.")) {
							edtLoanWiddrawal.setText(""); //Prevents "0" while starting but not "0."
						}

						String str = edtLoanWiddrawal.getText().toString().replaceAll(" ", "");
						if (!value.equals(""))
							edtLoanWiddrawal.setText(getDecimalFormat(str).concat(".00"));
						edtLoanWiddrawal.setSelection(edtLoanWiddrawal.getText().toString().length());
					}
					edtLoanWiddrawal.addTextChangedListener(this);
				} catch (Exception ex) {
					edtLoanWiddrawal.addTextChangedListener(this);
				}
			}
		}


		private String getDecimalFormat(String value) {
			value = value.replace(".00", "");
			StringTokenizer lst = new StringTokenizer(value, ".");
			String str1 = value;
			String str2 = "";
			if (lst.countTokens() > 1) {
				str1 = lst.nextToken();
				str2 = lst.nextToken();
			}
			String str3 = "";
			int i = 0;
			int j = -1 + str1.length();
			if (str1.charAt(-1 + str1.length()) == '.') {
				j--;
				str3 = ".";
			}
			for (int k = j; ; k--) {
				if (k < 0) {
					if (str2.length() > 0)
						str3 = str3 + "." + str2;
					return str3;
				}
				if (i == 3) {
					str3 = " " + str3;
					i = 0;
				}
				str3 = str1.charAt(k) + str3;
				i++;
			}

		}


		//Trims all the comma of the string and returns
		private String trimCommaOfString(String string) {
//        String returnString;
			if (string.contains(" ")) {
				return string.replace(" ", "");
			} else {
				return string;
			}

		}
	}



	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(connectionBroadcast);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.WITHDRAW_CASH);
		showSoftKeyboard();
		registerReceiver(connectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	}

	@Override
	public void onConnectionChanged() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (NetworkManager.getInstance().isConnectedToNetwork(LoanWithdrawalActivity.this)) {
					try {
						if (loanAmount() && getLoanWithdrawalClick()) {
							setAmount();
							setLoanWithdrawalClick(false);
						}
					} catch (Exception ignored) {
					}
				} else {
					mErrorHandlerView.showToast();
					showSoftKeyboard();
				}
			}
		});
	}

	private boolean loanAmount() {
		boolean setAmount;
		if (getDrawnDownAmount() < wminDrawnDownAmount) {
			setAmount = false;
		} else if (getDrawnDownAmount() >= wminDrawnDownAmount
				&& getDrawnDownAmount() <= getAvailableFund()) {
			return true;

		} else {
			setAmount = false;
		}
		return setAmount;
	}

	public boolean getLoanWithdrawalClick() {
		return loanWithdrawalClicked;
	}

	public void setLoanWithdrawalClick(boolean pLoanWithdrawalClicked) {
		loanWithdrawalClicked = pLoanWithdrawalClicked;
	}

	public String addDecimal(String value) {
		return value.replace(".", "").replace(",", " ") + ".00";
	}

	public String removeDecimal(String value) {
		return value.replace(" .", ".")
				.replace(" ", "")
				.replace(",", "")
				.replace(".00", "");
	}
}