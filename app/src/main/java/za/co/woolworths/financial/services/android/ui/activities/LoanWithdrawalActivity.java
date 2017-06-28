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
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.text.DecimalFormat;
import java.text.ParseException;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
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
	private boolean deleteKeyIsPressed = false;
	private ErrorHandlerView mErrorHandlerView;
	private NetworkChangeListener networkChangeListener;
	private BroadcastReceiver connectionBroadcast;

	private boolean loanWithdrawalClicked = false;

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
			mEditWithdrawalAmount.setText("R ");
		} else {
			mEditWithdrawalAmount.setText(String.valueOf(Integer.valueOf(shareDrawDownAmount) / 100));
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
						loanWithdrawalClicked = true;
						setAmount();
					}
				}
				return handled;
			}
		});


		mEditWithdrawalAmount.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_DEL) {
					Log.e("deletekey", mEditWithdrawalAmount.getText().toString());
					deleteKeyIsPressed = true;
				} else {
					deleteKeyIsPressed = false;
				}
				return false;
			}
		});

		mEditWithdrawalAmount.addTextChangedListener(new NumberTextWatcher(mEditWithdrawalAmount));

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
				if (new ConnectionDetector().isOnline(this)) {
					showSoftKeyboard();
				}
				loanWithdrawalClicked = true;
				setAmount();
				break;
		}
		return false;
	}

	private void setAmount() {
		if (getDrawnDownAmount() < wminDrawnDownAmount) {
			Utils.displayValidationMessage(LoanWithdrawalActivity.this,
					TransientActivity.VALIDATION_MESSAGE_LIST.LOW_LOAN_AMOUNT,
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
					TransientActivity.VALIDATION_MESSAGE_LIST.HIGH_LOAN_AMOUNT, "");
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
					if (issueLoanResponse.httpCode == 200) {
						loanWithdrawalClicked = false;
						mSharePreferenceHelper.save(String.valueOf(issueLoanResponse.installmentAmount), "lw_installment_amount");
						Intent openConfirmWithdrawal = new Intent(LoanWithdrawalActivity.this, LoanWithdrawalConfirmActivity.class);
						openConfirmWithdrawal.putExtra("drawnDownAmount", mDrawnDownAmount);
						openConfirmWithdrawal.putExtra("availableFunds", mAvailableFunds);
						openConfirmWithdrawal.putExtra("creditLimit", mCreditLimit);
						openConfirmWithdrawal.putExtra("minDrawnDownAmount", wminDrawnDownAmount);
						openConfirmWithdrawal.putExtra("repaymentPeriod", repaymentPeriod(getCreditAmount()));
						startActivity(openConfirmWithdrawal);
						finish();
					} else {
						try {
							hideKeyboard();
							String responseDesc = issueLoanResponse.response.desc;
							if (responseDesc != null) {
								if (!TextUtils.isEmpty(responseDesc)) {
									Utils.displayValidationMessage(LoanWithdrawalActivity.this,
											TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
											responseDesc);
								}
							}
						} catch (NullPointerException ignored) {
							showSoftKeyboard();
						}
					}
				} catch (Exception ignored) {
				}
			}

			@Override
			protected IssueLoanResponse httpError(final String errorMessage, HttpErrorCode httpErrorCode) {
				IssueLoanResponse issueLoanResponse = new IssueLoanResponse();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						loanWithdrawalClicked = true;
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
		};
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
		menuItemVisible(mMenu, false);
	}

	private void hideProgressBar() {
		mLoanWithdrawalProgress.setVisibility(View.GONE);
		mLoanWithdrawalProgress.getIndeterminateDrawable().setColorFilter(null);
		mEditWithdrawalAmount.setVisibility(View.VISIBLE);
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

	public class NumberTextWatcher implements TextWatcher {

		private DecimalFormat df;
		private DecimalFormat dfnd;
		private boolean hasFractionalPart;

		private EditText et;

		NumberTextWatcher(EditText et) {
			df = new DecimalFormat("#,###.##");
			df.setDecimalSeparatorAlwaysShown(true);
			dfnd = new DecimalFormat("#,###");
			this.et = et;
			hasFractionalPart = false;
		}

		@Override
		public void afterTextChanged(Editable s) {
			et.removeTextChangedListener(this);
			try {
				int inilen, endlen;
				inilen = et.getText().length();
				if (!deleteKeyIsPressed) {
					String v = s.toString().replace(String.valueOf(df.getDecimalFormatSymbols()
							.getGroupingSeparator()), "").replace(" .", ".")
							.replace(" ", "").replace("R ", "")
							.replace("R", "").replace(".00", "");
					Number n = null;
					if (TextUtils.isEmpty(v)) {
						et.setText("");
						menuItemVisible(mMenu, false);
					} else {
						try {
							n = df.parse(v);
						} catch (ParseException ignored) {
						}
						int cp = et.getSelectionStart();
						String finalAmount = null;
						if (hasFractionalPart) {
							finalAmount = "R " + df.format(n).replace(".", "").replace(",", " ") + ".00";
						} else {
							finalAmount = "R " + dfnd.format(n).replace(".", "").replace(",", " ") + ".00";
						}

						et.setText(finalAmount);
						endlen = et.getText().length();
						int sel = (cp + (endlen - inilen));
						if (sel > 0 && sel <= et.getText().length()) {
							et.setSelection(endlen);
						} else {
							// place cursor at the end?
							et.setSelection(et.getText().length());
						}
						menuItemVisible(mMenu, true);
					}
				} else {
					String withdrawalAmount = mEditWithdrawalAmount.getText().toString();
					String intWithdrawalAmount = withdrawalAmount
							.replace(String.valueOf(df.getDecimalFormatSymbols()
									.getGroupingSeparator()), "").replace(" .", ".")
							.replace(" ", "").replace("R ", "")
							.replace("R", "").replace(".00", "").replace(".0", "");
					intWithdrawalAmount = intWithdrawalAmount.substring(0, intWithdrawalAmount.length() - 1);
					Number n = null;
					if (TextUtils.isEmpty(intWithdrawalAmount)) {
						et.setText("");
						menuItemVisible(mMenu, false);
					} else {
						try {
							n = df.parse(intWithdrawalAmount);
						} catch (ParseException ignored) {
						}
						int cp = et.getSelectionStart();
						String finalAmount = null;

						if (hasFractionalPart) {
							finalAmount = "R " + df.format(n).replace(".", "").replace(",", " ") + ".00";
						} else {
							finalAmount = "R " + dfnd.format(n).replace(".", "").replace(",", " ") + ".00";
						}

						et.setText(finalAmount);
						endlen = et.getText().length();
						int sel = (cp + (endlen - inilen));
						if (sel > 0 && sel <= et.getText().length()) {
							et.setSelection(sel);
						} else {
							// place cursor at the end?
							et.setSelection(et.getText().length());
						}
						menuItemVisible(mMenu, true);
					}
				}
			} catch (NumberFormatException ignored) {
			}
			et.addTextChangedListener(this);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (before - count == 1) { // onDeleteButton pressed
			} else if (s.subSequence(start, start + count).toString().equals("\n")) {
				hasFractionalPart = s.toString().contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()));
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
		showSoftKeyboard();
		registerReceiver(connectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	}

	@Override
	public void onConnectionChanged() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (new ConnectionDetector().isOnline(LoanWithdrawalActivity.this)) {
					try {
						if (loanAmount() && loanWithdrawalClicked) {
							setAmount();
							loanWithdrawalClicked = false;
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
}