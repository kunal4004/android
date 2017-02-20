package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Context;
import android.content.Intent;
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
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.Locale;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.Utils;

public class LoanWithdrawalActivity extends BaseActivity {

    private WLoanEditTextView mEditWithdrawalAmount;
    private WTextView mTextAvailableFund;
    private WTextView mTextCreditLimit;
    private Menu mMenu;
    private RelativeLayout mRelLoanWithdrawal;
    private SharePreferenceHelper mSharePreferenceHelper;
    private ConnectionDetector mConnectionDetector;
    private PopWindowValidationMessage mPopWindowValidationMessage;
    Handler handler = new Handler();//not nice
    private String mDrawnDownAmount;
    private String mCreditLimit;
    private String mAvailableFunds;
    private WeakReference<WLoanEditTextView> mEditTextWeakReference;
    private WLoanEditTextView mEditText;
    private int wminDrawnDownAmount = 0;
    private ProgressBar mLoanWithdrawalProgress;
    private boolean arrowIsVisible = false;
    private AsyncTask<String, String, IssueLoanResponse> issueLoanRequest;
    private ScrollView mScrollLoanWithdrawal;
    private RelativeLayout mLinLoanWithdrawalSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(LoanWithdrawalActivity.this, R.color.purple);
        setContentView(R.layout.loan_withdrawal_activity);
        mConnectionDetector = new ConnectionDetector();
        mSharePreferenceHelper = SharePreferenceHelper.getInstance(LoanWithdrawalActivity.this);
        mPopWindowValidationMessage = new PopWindowValidationMessage(LoanWithdrawalActivity.this);
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
            mEditWithdrawalAmount.setSelection(2);
        } else {
            mEditWithdrawalAmount.setText(shareDrawDownAmount);
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
        mScrollLoanWithdrawal = (ScrollView) findViewById(R.id.scrollLoanWithdrawal);
        mLinLoanWithdrawalSuccess = (RelativeLayout) findViewById(R.id.linLoanWithdrawalSuccess);
        mScrollLoanWithdrawal.setVisibility(View.GONE);
        mLinLoanWithdrawalSuccess.setVisibility(View.GONE);
    }

    private void setActionBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
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
                        setAmount();
                    }
                }
                return handled;
            }
        });

        mEditWithdrawalAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mEditTextWeakReference = new WeakReference<>(mEditWithdrawalAmount);
                mEditText = mEditTextWeakReference.get();
                if (mEditText == null) return;
                String s = editable.toString();
                mEditText.removeTextChangedListener(this);
                String cleanString = s.replaceAll("[\\D]", "");
                if (TextUtils.isEmpty(cleanString)) {
                    cleanString = "0.00";
                }
                BigDecimal parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
                java.util.Currency usd = java.util.Currency.getInstance("USD");
                java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US);
                format.setCurrency(usd);
                String formatted = format.format(parsed);
                String newFormat = formatted.replace(",", " ");
                String symbol = format.getCurrency().getSymbol(Locale.US);
                int checkAmount = 0;
                if (!TextUtils.isEmpty(newFormat)) {
                    checkAmount = Double.valueOf(newFormat.replace(symbol, "").replace(" ", "")).intValue();
                }
                if (newFormat.length() > 0) {
                    newFormat = newFormat.replace(symbol, "R ");
                }
                if (checkAmount == 0) {
                    menuItemVisible(mMenu, false);
                } else {
                    menuItemVisible(mMenu, true);
                }
                if (newFormat.equalsIgnoreCase("R 0.00")) {
                    newFormat = "R ";
                }
                mEditText.setText(newFormat);
                if (newFormat.length() > 6 && !newFormat.equalsIgnoreCase("R 0.00")) {
                    mEditText.setSelection(newFormat.length());
                } else {
                    mEditText.setSelection(newFormat.length());
                }
                mEditText.addTextChangedListener(this);
            }
        });

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
                setAmount();
                break;
        }
        return false;
    }

    private void setAmount() {
        if (getDrawnDownAmount() < wminDrawnDownAmount) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideKeyboard();
                }
            }, 100);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPopWindowValidationMessage.displayValidationMessage(String.valueOf(wminDrawnDownAmount),
                            PopWindowValidationMessage.OVERLAY_TYPE.LOW_LOAN_AMOUNT)
                            .setOnDismissListener(new PopupWindow.OnDismissListener() {
                                @Override
                                public void onDismiss() {
                                    showSoftKeyboard();
                                    Utils.updateStatusBarBackground(LoanWithdrawalActivity.this, R.color.purple);
                                }
                            });
                }
            }, 200);
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
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideKeyboard();
                }
            }, 100);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPopWindowValidationMessage.displayValidationMessage("",
                            PopWindowValidationMessage.OVERLAY_TYPE.HIGH_LOAN_AMOUNT)
                            .setOnDismissListener(new PopupWindow.OnDismissListener() {
                                @Override
                                public void onDismiss() {
                                    showSoftKeyboard();
                                    Utils.updateStatusBarBackground(LoanWithdrawalActivity.this, R.color.purple);
                                }
                            });
                }
            }, 200);
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void loanRequest() {
        if (mConnectionDetector.isOnline(this)) {
            issueLoanRequest = new HttpAsyncTask<String, String, IssueLoanResponse>() {
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
                    return ((WoolworthsApplication) getApplication()).getApi().issueLoan(issueLoanRequest);
                }

                @Override
                protected void onPreExecute() {
                    showProgressBar();
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(IssueLoanResponse issueLoanResponse) {
                    super.onPostExecute(issueLoanResponse);
                    hideProgressBar();
                    if (issueLoanResponse.httpCode == 200) {
                        mSharePreferenceHelper.save(String.valueOf(issueLoanResponse.installmentAmount), "lw_installment_amount");
                        Intent openConfirmWithdrawal = new Intent(LoanWithdrawalActivity.this, LoanWithdrawalConfirmActivity.class);
                        openConfirmWithdrawal.putExtra("drawnDownAmount", mDrawnDownAmount);
                        openConfirmWithdrawal.putExtra("availableFunds", mAvailableFunds);
                        openConfirmWithdrawal.putExtra("creditLimit", mCreditLimit);
                        openConfirmWithdrawal.putExtra("repaymentPeriod", repaymentPeriod(getCreditAmount()));
                        startActivity(openConfirmWithdrawal);
                        finish();
                    } else {
                        try {
                            hideKeyboard();
                            String responseDesc = issueLoanResponse.response.desc;
                            if (responseDesc != null) {
                                mPopWindowValidationMessage.displayValidationMessage(responseDesc,
                                        PopWindowValidationMessage.OVERLAY_TYPE.ERROR)
                                        .setOnDismissListener(new PopupWindow.OnDismissListener() {
                                            @Override
                                            public void onDismiss() {
                                                showSoftKeyboard();
                                                Utils.updateStatusBarBackground(LoanWithdrawalActivity.this, R.color.purple);
                                            }
                                        });
                            }
                        } catch (NullPointerException ignored) {
                        }
                    }
                }

                @Override
                protected IssueLoanResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    IssueLoanResponse issueLoanResponse = new IssueLoanResponse();
                    hideProgressBar();
                    return issueLoanResponse;
                }

                @Override
                protected Class<IssueLoanResponse> httpDoInBackgroundReturnType() {
                    return IssueLoanResponse.class;
                }
            };

            issueLoanRequest.execute();

        } else {
            mPopWindowValidationMessage.displayValidationMessage(getString(R.string.connect_to_server),
                    PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
        }
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
}