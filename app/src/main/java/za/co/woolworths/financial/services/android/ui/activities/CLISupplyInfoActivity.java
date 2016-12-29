package za.co.woolworths.financial.services.android.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.awfs.coordination.R;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferResponse;
import za.co.woolworths.financial.services.android.models.dto.CreditLimit;
import za.co.woolworths.financial.services.android.models.dto.OfferResponse;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.ui.adapters.CLICreditLimitAdapter;
import za.co.woolworths.financial.services.android.ui.views.StepIndicator;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SlidingUpViewLayout;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WErrorDialog;
import za.co.woolworths.financial.services.android.util.binder.view.CLICreditLimitContentBinder;


/**
 * Created by dimitrij on 2016/12/20.
 */

public class CLISupplyInfoActivity extends AppCompatActivity implements View.OnClickListener,
        CLICreditLimitContentBinder.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private StepIndicator mStepIndicator;
    private WEditTextView mTextAmount;
    private WButton mBtnContinue;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecycleList;
    private CLICreditLimitAdapter mCLICreditLimitAdapter;
    private RadioGroup mRadApplySolvency;
    private RadioButton mRadioYesSolvency;
    private RadioButton mRadioNoSolvency;
    private RadioGroup mRadConfidentialCredit;
    private RadioButton mRadioYesConfidentialCredit;
    private RadioButton mRadioNoConfidentialCredit;
    private WTextView mTextApplySolvency;
    private ActionBar mActionBar;
    private ImageView mImageCreditAmount;
    private Toolbar mToolbar;
    private List<CreditLimit> mArrCreditLimit;
    final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    private ProgressDialog mCreateOfferProgressDialog;
    private PopupWindow slidingUpView;
    private int mCreditLimitAmount=0;
    private CreateOfferRequest mCreateOfferRequest;
    private String current="";

    WoolworthsApplication mWoolworthsApplication;
    private UpdateBankDetail mUpdateBankDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cli_supply_info);
        Utils.updateStatusBarBackground(CLISupplyInfoActivity.this);
        mWoolworthsApplication = (WoolworthsApplication)getApplication();
        mUpdateBankDetail = mWoolworthsApplication.updateBankDetail;

        initViews();
        setActionBar();
        setListener();
        setCLIContent();
        mArrCreditLimit = getCreditLimitInfo();
        setRecycleView(mArrCreditLimit);
        hideSoftKeyboard();
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mStepIndicator = (StepIndicator) findViewById(R.id.step_indicator);
        mTextAmount = (WEditTextView) findViewById(R.id.textAmount);
        mBtnContinue = (WButton) findViewById(R.id.btnContinue);
        mRecycleList = (RecyclerView) findViewById(R.id.recycleList);
        mRadApplySolvency = (RadioGroup) findViewById(R.id.radApplySolvency);
        mRadioYesSolvency = (RadioButton) findViewById(R.id.radioYesSolvency);
        mRadioNoSolvency = (RadioButton) findViewById(R.id.radioNoSolvency);
        mRadConfidentialCredit = (RadioGroup) findViewById(R.id.radConfidentialCredit);
        mRadioYesConfidentialCredit = (RadioButton) findViewById(R.id.radioYesConfidentialCredit);
        mRadioNoConfidentialCredit = (RadioButton) findViewById(R.id.radioNoConfidentialCredit);
        mTextApplySolvency = (WTextView) findViewById(R.id.textApplySolvency);
        mImageCreditAmount = (ImageView) findViewById(R.id.imgInfo);
    }

    private void setActionBar() {
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayUseLogoEnabled(false);
        mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeAsUpIndicator(R.drawable.close_24);
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
        mStepIndicator.setDefaultView(0);
        mBtnContinue.setText(getString(R.string.cli_yes_continue));
        mStepIndicator.setEnabled(false);
        mTextApplySolvency.setText(getString(R.string.cli_apply_insolvency));
        mTextAmount.setVisibility(View.VISIBLE);
        mImageCreditAmount.setVisibility(View.GONE);
        mImageCreditAmount.setVisibility(View.GONE);
        mTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().equals(current)) {
                    mTextAmount.removeTextChangedListener(this);

                    String replaceable = String.format("[%s .\\s]", NumberFormat.getCurrencyInstance().getCurrency().getSymbol());
                    String cleanString = s.toString().replaceAll(replaceable, "").replace("R","").replace(",","");
                    double parsed;
                    try {
                        parsed = Double.parseDouble(cleanString);
                    } catch (NumberFormatException e) {
                        parsed = 0.00;
                    }

                    String formatted = Utils.formatCurrency(parsed);

                    current = formatted;
                    mTextAmount.setText(formatted);
                    mTextAmount.setSelection(formatted.length());

                    // Do whatever you want with position
                    mTextAmount.addTextChangedListener(this);
                }
            }
        });
    }

    private void setRecycleView(List<CreditLimit> creditLimit) {
        mCLICreditLimitAdapter = new CLICreditLimitAdapter(creditLimit, this);
        mLayoutManager = new LinearLayoutManager(this);
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
                String creditAmount = mTextAmount.getText().toString();
                if(TextUtils.isEmpty(creditAmount)){
                    WErrorDialog.setErrorMessage(CLISupplyInfoActivity.this,
                            getString(R.string.cli_solvency_error));
                } else {
                    mCreditLimitAmount = Integer.valueOf(creditAmount.replace(" ", "").replace("R", "").replace(",", ""));
                    if (mRadApplySolvency.getCheckedRadioButtonId() == -1) {
                        WErrorDialog.setErrorMessage(CLISupplyInfoActivity.this,
                                getString(R.string.cli_solvency_error));
                    } else {
                        if (mRadConfidentialCredit.getCheckedRadioButtonId() == -1) {
                            WErrorDialog.setErrorMessage(CLISupplyInfoActivity.this,
                                    getString(R.string.cli_solvency_error));
                        } else {
                            mCreateOfferRequest = new CreateOfferRequest(mWoolworthsApplication.getProductOfferingId(),
                                    mCreditLimitAmount,
                                    getNumbers(0),
                                    getNumbers(1),
                                    getNumbers(2),
                                    getNumbers(3),
                                    getNumbers(4),
                                    getNumbers(5),
                                    getNumbers(6),
                                    getNumbers(7));

                            for (int index = 0; index < mArrCreditLimit.size(); index++) {
                                String amount = mArrCreditLimit.get(index).getAmount();
                                if (TextUtils.isEmpty(amount) || amount.equalsIgnoreCase("0")) {
                                    WErrorDialog.setErrorMessage(CLISupplyInfoActivity.this,
                                            getString(R.string.cli_solvency_error));
                                    return;
                                }
                            }
                            String selectedRadSolvency = selectedRadioGroup(mRadApplySolvency);
                            String selectedRadConfidential = selectedRadioGroup(mRadConfidentialCredit);
                            createOfferRequest(selectedRadSolvency, selectedRadConfidential);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    public List<CreditLimit> getCreditLimitInfo() {
        ArrayList<CreditLimit> creditLimits = new ArrayList<>();
        if (creditLimits != null)
            creditLimits.clear();
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
            checked.setTypeface(Typeface.DEFAULT_BOLD);
        } catch (NullPointerException ex) {
        }
        try {
            RadioButton checked = (RadioButton) findViewById(mRadConfidentialCredit.getCheckedRadioButtonId());
            checked.setTypeface(Typeface.DEFAULT_BOLD);
        } catch (NullPointerException ex) {
        }
    }

    @Override
    public void onClick(View v, int position) {

        SlidingUpViewLayout slidingUpViewLayout = new SlidingUpViewLayout(this);
        if (mArrCreditLimit != null) {
            slidingUpView = slidingUpViewLayout.openOverlayView(mArrCreditLimit.get(position).getDescription(),
                    SlidingUpViewLayout.OVERLAY_TYPE.INFO);
            slidingUpViewLayout.setPopupWindowTouchModal(slidingUpView,true);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        buttonView.setTypeface(isChecked ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRadioButtonBold();
    }


    @Override
    public void onBackPressed() {
        if (slidingUpView != null) {
            if (slidingUpView.isShowing()) {
                slidingUpView.dismiss();
            } else {
                canGoBack();
            }
        } else {
            canGoBack();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent openPreviousActivity = new Intent(CLISupplyInfoActivity.this, CLIActivity.class);
                startActivity(openPreviousActivity);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
        }
        return false;
    }

    public String selectedRadioGroup(RadioGroup radioGroup) {
        int radioID = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) radioGroup.findViewById(radioID);
        String selectedConfidentialCredit = (String) radioButton.getText();
        return selectedConfidentialCredit;
    }

    public void createOfferRequest(String solvency,String confidential) {
        new HttpAsyncTask<String, String, CreateOfferResponse>() {
            @Override
            protected CreateOfferResponse httpDoInBackground(String... params) {
            return ((WoolworthsApplication) getApplication()).getApi().createOfferRequest(mCreateOfferRequest);
            }

            @Override
            protected CreateOfferResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                CreateOfferResponse offerResponse = new CreateOfferResponse();
                offerResponse.response = new OfferResponse();
                stopProgressDialog();
                return offerResponse;
            }

            @Override
            protected void onPreExecute() {
                mCreateOfferProgressDialog = new ProgressDialog(CLISupplyInfoActivity.this);
                mCreateOfferProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.cli_creating_offer), 1, CLISupplyInfoActivity.this));
                mCreateOfferProgressDialog.setCancelable(false);
                mCreateOfferProgressDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(CreateOfferResponse createOfferResponse) {
                super.onPostExecute(createOfferResponse);
                if (createOfferResponse != null) {
                    int httpCode = createOfferResponse.httpCode;
                    if (createOfferResponse != null) {
                        switch (httpCode) {
                            case 200:
                                mUpdateBankDetail.setCliOfferID(createOfferResponse.cliOfferId);
                                openBankDetails();
                                break;
                            default:
                                 WErrorDialog.setErrorMessage(CLISupplyInfoActivity.this, createOfferResponse.response.desc);
                                break;
                        }
                    }
                }
                stopProgressDialog();
            }

            @Override
            protected Class<CreateOfferResponse> httpDoInBackgroundReturnType() {
                return CreateOfferResponse.class;
            }
        }.execute();
    }

    public int getNumbers(int position) {
        if (mArrCreditLimit != null) {
            return Integer.valueOf(mArrCreditLimit.get(position).getAmount().replace("R", "").replace(" ", ""));
        } else {
            return 0;
        }
    }

    public void stopProgressDialog() {
        if (mCreateOfferProgressDialog != null && mCreateOfferProgressDialog.isShowing()) {
            mCreateOfferProgressDialog.dismiss();
        }
    }

    public void openBankDetails() {
        Intent openCLIStepIndicatorActivity = new Intent(CLISupplyInfoActivity.this, CLIStepIndicatorActivity.class);
        startActivity(openCLIStepIndicatorActivity);
        finish();
        overridePendingTransition(0, 0);
    }

    public void canGoBack() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


}

