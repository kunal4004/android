package za.co.woolworths.financial.services.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.awfs.coordination.R;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferResponse;
import za.co.woolworths.financial.services.android.models.dto.CreditLimit;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.ui.adapters.CLICreditLimitAdapter;
import za.co.woolworths.financial.services.android.ui.views.ErrorHandlerView;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WEmpyViewDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.view.CLICreditLimitContentBinder;

public class CLISupplyInfoActivity extends BaseActivity implements View.OnClickListener,
        CLICreditLimitContentBinder.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private WEditTextView mTextAmount;
    private WButton mBtnContinue;
    private RecyclerView mRecycleList;
    private RadioGroup mRadApplySolvency;
    private RadioButton mRadioYesSolvency;
    private RadioButton mRadioNoSolvency;
    private RadioGroup mRadConfidentialCredit;
    private RadioButton mRadioYesConfidentialCredit;
    private RadioButton mRadioNoConfidentialCredit;
    private WTextView mTextApplySolvency;
    private ImageView mImageCreditAmount;
    private Toolbar mToolbar;
    private List<CreditLimit> mArrCreditLimit;
    final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    private CreateOfferRequest mCreateOfferRequest;
    private NestedScrollView mNestedScrollview;
    private WoolworthsApplication mWoolworthsApplication;
    private UpdateBankDetail mUpdateBankDetail;
    private WTextView mTextACreditLimit;
    private WTextView mTextProceedToSolvency;

    private Typeface mRdioGroupTypeFace;
    private Typeface mRdioGroupTypeFaceBold;
    private ProgressBar mProgressBar;
    private FragmentManager fm;
    private WEmpyViewDialogFragment mEmpyViewDialogFragment;
    private ErrorHandlerView mErrorHandlerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cli_supply_info);
        Utils.updateStatusBarBackground(CLISupplyInfoActivity.this);
        fm = getSupportFragmentManager();
        mWoolworthsApplication = (WoolworthsApplication) getApplication();
        mUpdateBankDetail = mWoolworthsApplication.updateBankDetail;
        mProgressBar = (ProgressBar) findViewById(R.id.mWoolworthsProgressBar);
        mRdioGroupTypeFace = Typeface.createFromAsset(getAssets(), "fonts/WFutura-Medium.ttf");
        mRdioGroupTypeFaceBold = Typeface.createFromAsset(getAssets(), "fonts/WFutura-SemiBold.ttf");
        initViews();
        setActionBar();
        setListener();
        setCLIContent();
        mArrCreditLimit = getCreditLimitInfo();
        setRecycleView(mArrCreditLimit);
        hideSoftKeyboard();
        radioCheckStateChanged();

        registerReceiver(confidentialBroadcastCheck, new IntentFilter("confidentialBroadcastCheck"));
        registerReceiver(insolvencyBroadcastCheck, new IntentFilter("insolvencyBroadcastCheck"));
    }

    private void radioCheckStateChanged() {
        mRadApplySolvency.setOnCheckedChangeListener(solvencyCheckListener);
        mRadConfidentialCredit.setOnCheckedChangeListener(confidentialCheckListener);
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextAmount = (WEditTextView) findViewById(R.id.textAmount);
        mBtnContinue = (WButton) findViewById(R.id.btnContinue);
        mRecycleList = (RecyclerView) findViewById(R.id.recycleList);
        mNestedScrollview = (NestedScrollView) findViewById(R.id.mNestedScrollview);
        mRadApplySolvency = (RadioGroup) findViewById(R.id.radApplySolvency);
        mRadioYesSolvency = (RadioButton) findViewById(R.id.radioYesSolvency);
        mRadioNoSolvency = (RadioButton) findViewById(R.id.radioNoSolvency);
        mRadConfidentialCredit = (RadioGroup) findViewById(R.id.radConfidentialCredit);
        mRadioYesConfidentialCredit = (RadioButton) findViewById(R.id.radioYesConfidentialCredit);
        mRadioNoConfidentialCredit = (RadioButton) findViewById(R.id.radioNoConfidentialCredit);
        mTextApplySolvency = (WTextView) findViewById(R.id.textApplySolvency);
        mTextACreditLimit = (WTextView) findViewById(R.id.textACreditLimit);
        mTextProceedToSolvency = (WTextView) findViewById(R.id.textProceedToSolvency);
        mImageCreditAmount = (ImageView) findViewById(R.id.imgInfo);
        mErrorHandlerView = new ErrorHandlerView(mWoolworthsApplication);
    }

    private void setActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
            mActionBar.setHomeAsUpIndicator(R.drawable.close_24);
        }
    }

    public void setListener() {
        mBtnContinue.setOnClickListener(this);
        mRadioYesSolvency.setOnCheckedChangeListener(this);
        mRadioNoSolvency.setOnCheckedChangeListener(this);
        mRadioYesConfidentialCredit.setOnCheckedChangeListener(this);
        mRadioNoConfidentialCredit.setOnCheckedChangeListener(this);
        mImageCreditAmount.setOnClickListener(this);
    }

    private void setCLIContent() {
        mBtnContinue.setText(getString(R.string.cli_yes_continue));
        mTextApplySolvency.setText(getString(R.string.cli_apply_insolvency));
        mTextAmount.setVisibility(View.VISIBLE);
        mImageCreditAmount.setVisibility(View.GONE);
        mImageCreditAmount.setVisibility(View.GONE);
        mTextACreditLimit.setText(getString(R.string.cli_additional_credit_amount));
        mTextACreditLimit.setAllCaps(true);
        mTextAmount.addTextChangedListener(new NumberTextWatcher(mTextAmount));
        mTextAmount.setSelection(mTextAmount.getText().length());
        mTextProceedToSolvency.setVisibility(View.VISIBLE);
    }

    private void setRecycleView(List<CreditLimit> creditLimit) {
        CLICreditLimitAdapter mCLICreditLimitAdapter = new CLICreditLimitAdapter(creditLimit, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleList.setLayoutManager(mLayoutManager);
        mRecycleList.setNestedScrollingEnabled(false);
        mRecycleList.setAdapter(mCLICreditLimitAdapter);
        mCLICreditLimitAdapter.setCLIContent();
        mRecycleList.setFocusable(false);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnContinue:
                mBtnContinue.startAnimation(buttonClick);
                hideSoftKeyboard();

                boolean pageIsValid = true;

                int selectedInsolventRadioButtonId = mRadApplySolvency.getCheckedRadioButtonId();
                int selectedAccessRequestRadioButton = mRadConfidentialCredit.getCheckedRadioButtonId();

                String creditAmount = mTextAmount.getText().toString().replaceAll("[^0-9.]", "");
                String grossMonthlyIncome = mArrCreditLimit.get(0).getAmount().replaceAll("[^0-9.]", "");
                String netMonthlyIncome = mArrCreditLimit.get(1).getAmount().replaceAll("[^0-9.]", "");
                String additionalMonthlyIncome = mArrCreditLimit.get(2).getAmount().replaceAll("[^0-9.]", "");
                String mortgagePayments = mArrCreditLimit.get(3).getAmount().replaceAll("[^0-9.]", "");
                String rentalPayments = mArrCreditLimit.get(4).getAmount().replaceAll("[^0-9.]", "");
                String maintenanceExpenses = mArrCreditLimit.get(5).getAmount().replaceAll("[^0-9.]", "");
                String monthlyCreditPayments = mArrCreditLimit.get(6).getAmount().replaceAll("[^0-9.]", "");
                String otherExpenses = mArrCreditLimit.get(7).getAmount().replaceAll("[^0-9.]", "");

                if (creditAmount.equals("")) {
                    pageIsValid = false;
                } else if (grossMonthlyIncome.equals("")) {
                    pageIsValid = false;
                } else if (netMonthlyIncome.equals("")) {
                    pageIsValid = false;
                } else if (additionalMonthlyIncome.equals("")) {
                    pageIsValid = false;
                } else if (mortgagePayments.equals("")) {
                    pageIsValid = false;
                } else if (rentalPayments.equals("")) {
                    pageIsValid = false;
                } else if (maintenanceExpenses.equals("")) {
                    pageIsValid = false;
                } else if (monthlyCreditPayments.equals("")) {
                    pageIsValid = false;
                } else if (otherExpenses.equals("")) {
                    pageIsValid = false;
                } else if (selectedInsolventRadioButtonId == -1 || selectedAccessRequestRadioButton == -1) {
                    pageIsValid = false;
                }

                if (!pageIsValid) {
                    Utils.displayValidationMessage(CLISupplyInfoActivity.this,
                            TransientActivity.VALIDATION_MESSAGE_LIST.MANDATORY_FIELD,
                            getString(R.string.cli_cancel_application));
                    return;
                }

                int mCreditLimitAmount = 0;
                if (!TextUtils.isEmpty(creditAmount)) {
                    mCreditLimitAmount = Integer.valueOf(creditAmount);
                }

                mCreateOfferRequest = new CreateOfferRequest(mWoolworthsApplication.getProductOfferingId(),
                        mCreditLimitAmount,
                        Integer.valueOf(grossMonthlyIncome),
                        Integer.valueOf(netMonthlyIncome),
                        Integer.valueOf(additionalMonthlyIncome),
                        Integer.valueOf(mortgagePayments),
                        Integer.valueOf(rentalPayments),
                        Integer.valueOf(maintenanceExpenses),
                        Integer.valueOf(monthlyCreditPayments),
                        Integer.valueOf(otherExpenses));
                createOfferRequest();
                break;
            default:
                break;
        }
    }

    public List<CreditLimit> getCreditLimitInfo() {
        ArrayList<CreditLimit> creditLimits = new ArrayList<>();
        String[] clAmountArray = getResources().getStringArray(R.array.cl_amount);
        String[] clArrayName = getResources().getStringArray(R.array.cl_label);
        String[] clArrayDescription = getResources().getStringArray(R.array.cl_overlay_label);

        for (int x = 0; x < clAmountArray.length; x++) {
            creditLimits.add(new CreditLimit(clArrayName[x], clAmountArray[x], clArrayDescription[x]));
        }
        return creditLimits;
    }

    public void setRadioButtonBold() {
        try {
            RadioButton checked = (RadioButton) findViewById(mRadApplySolvency.getCheckedRadioButtonId());
            checked.setTypeface(mRdioGroupTypeFaceBold);
        } catch (NullPointerException ignored) {
        }
        try {
            RadioButton checked = (RadioButton) findViewById(mRadConfidentialCredit.getCheckedRadioButtonId());
            checked.setTypeface(mRdioGroupTypeFaceBold);
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public void onClick(View v, int position) {
        if (mArrCreditLimit != null) {
            Utils.displayValidationMessage(CLISupplyInfoActivity.this,
                    TransientActivity.VALIDATION_MESSAGE_LIST.INFO,
                    mArrCreditLimit.get(position).getDescription());
        }
    }

    @Override
    public void scrollToBottom() {
        hideSoftKeyboard();
        mNestedScrollview.fullScroll(View.FOCUS_DOWN);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        buttonView.setTypeface(isChecked ? mRdioGroupTypeFaceBold : mRdioGroupTypeFace);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRadioButtonBold();
        if (mWoolworthsApplication.isTriggerErrorHandler()) {
            createOfferRequest();
        }
    }

    @Override
    public void onBackPressed() {
        canGoBack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent openPreviousActivity = new Intent(CLISupplyInfoActivity.this, CLIActivity.class);
                startActivity(openPreviousActivity);
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                return true;
        }
        return false;
    }

    private void createOfferRequest() {
        createOfferAsyncApi().execute();
    }

    public HttpAsyncTask<String, String, CreateOfferResponse> createOfferAsyncApi() {
        return new HttpAsyncTask<String, String, CreateOfferResponse>() {
            @Override
            protected CreateOfferResponse httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().createOfferRequest(mCreateOfferRequest);
            }

            @Override
            protected CreateOfferResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                networkFailureHandler();
                return new CreateOfferResponse();
            }

            @Override
            protected void onPreExecute() {
                showProgressBar();
                mErrorHandlerView.hideErrorHandlerLayout();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(CreateOfferResponse createOfferResponse) {
                super.onPostExecute(createOfferResponse);
                try {
                    if (createOfferResponse != null) {
                        int httpCode = createOfferResponse.httpCode;
                        switch (httpCode) {
                            case 200:
                                mUpdateBankDetail.setCliOfferID(createOfferResponse.cliOfferId);
                                openBankDetails();
                                break;
                            default:
                                if (!TextUtils.isEmpty(createOfferResponse.response.desc)) {
                                    Utils.displayValidationMessage(CLISupplyInfoActivity.this,
                                            TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
                                            createOfferResponse.response.desc);
                                }
                                break;
                        }
                    }
                } catch (NullPointerException ignored) {
                }
                stopProgressDialog();
            }

            @Override
            protected Class<CreateOfferResponse> httpDoInBackgroundReturnType() {
                return CreateOfferResponse.class;
            }
        };
    }

    public void openBankDetails() {
        Intent openCLIStepIndicatorActivity = new Intent(CLISupplyInfoActivity.this, CLIStepIndicatorActivity.class);
        startActivity(openCLIStepIndicatorActivity);
        finish();
        overridePendingTransition(0, 0);
    }

    public void canGoBack() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }


    public void hideSoftKeyboard() {
        try {
            if (getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (NullPointerException ignored) {
        }
    }

    private class NumberTextWatcher implements TextWatcher {

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
                String v = s.toString().replace(String.valueOf(df.getDecimalFormatSymbols().getGroupingSeparator()), "").replace(" ", "").replace("R ", "").replace("R", "");
                Number n = null;
                if (TextUtils.isEmpty(v)) {
                    et.setText("");
                } else {
                    try {
                        n = df.parse(v);
                    } catch (ParseException ignored) {
                    }
                    int cp = et.getSelectionStart();
                    String finalAmount;
                    if (hasFractionalPart) {
                        finalAmount = "" + df.format(n).replace(".", " ").replace(",", " ");
                    } else {
                        finalAmount = "" + dfnd.format(n).replace(".", " ").replace(",", " ");
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
            hasFractionalPart = s.toString().contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()));
        }
    }

    public void showProgressBar() {
        mEmpyViewDialogFragment = WEmpyViewDialogFragment.newInstance("blank");
        mEmpyViewDialogFragment.setCancelable(false);
        mEmpyViewDialogFragment.show(fm, "blank");
        mProgressBar.bringToFront();
        mProgressBar.setVisibility(View.VISIBLE);
        mBtnContinue.setVisibility(View.GONE);
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
    }

    public void stopProgressDialog() {
        if (mEmpyViewDialogFragment != null) {
            if (mEmpyViewDialogFragment.isVisible()) {
                mEmpyViewDialogFragment.dismiss();
            }
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        }
        mBtnContinue.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(confidentialBroadcastCheck);
        unregisterReceiver(insolvencyBroadcastCheck);
    }

    BroadcastReceiver confidentialBroadcastCheck = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mRadConfidentialCredit.setOnCheckedChangeListener(null);
            mRadConfidentialCredit.clearCheck();
            mRadConfidentialCredit.setOnCheckedChangeListener(confidentialCheckListener);
            mRadioNoConfidentialCredit.setTypeface(mRdioGroupTypeFace);
            mRadioYesConfidentialCredit.setTypeface(mRdioGroupTypeFace);
        }
    };


    BroadcastReceiver insolvencyBroadcastCheck = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mRadApplySolvency.setOnCheckedChangeListener(null);
            mRadApplySolvency.clearCheck();
            mRadApplySolvency.setOnCheckedChangeListener(solvencyCheckListener);
            mRadioYesSolvency.setTypeface(mRdioGroupTypeFace);
            mRadioNoSolvency.setTypeface(mRdioGroupTypeFace);
        }
    };


    private RadioGroup.OnCheckedChangeListener confidentialCheckListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radioYesConfidentialCredit:
                    break;
                case R.id.radioNoConfidentialCredit:
                    Utils.displayValidationMessage(CLISupplyInfoActivity.this,
                            TransientActivity.VALIDATION_MESSAGE_LIST.CONFIDENTIAL,
                            "");
                    break;
                default:
                    break;
            }
            hideSoftKeyboard();
        }
    };

    private RadioGroup.OnCheckedChangeListener solvencyCheckListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radioNoSolvency:
                    break;
                case R.id.radioYesSolvency:
                    Utils.displayValidationMessage(CLISupplyInfoActivity.this,
                            TransientActivity.VALIDATION_MESSAGE_LIST.INSOLVENCY,
                            "");
                    break;
                default:
                    break;
            }
            hideSoftKeyboard();
        }
    };

    public void networkFailureHandler() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopProgressDialog();
                mErrorHandlerView.startActivity(CLISupplyInfoActivity.this);
            }
        });
    }
}
