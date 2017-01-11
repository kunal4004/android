package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.awfs.coordination.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
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
    private NestedScrollView mNestedScrollview;

    WoolworthsApplication mWoolworthsApplication;
    private UpdateBankDetail mUpdateBankDetail;
    ConnectionDetector connectionDetector;
    private WTextView mTextACreditLimit;
    private SlidingUpViewLayout slidingUpViewLayout;
    private WTextView mTextProceedToSolvency;
    private LayoutInflater mLayoutInflater;
    private WButton mBtnCancel;
    private PopupWindow pWindow;

    Handler handler = new Handler();
    private PopupWindow darkenScreen;
    private PopupWindow mDarkenScreen;
    private PopupWindow mPopWindow;
    private boolean isConfidential=true;
    private WTextView mTextApplicationNotProceed;
    private WTextView mTextOverlayDescription;
    private boolean isSolvency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cli_supply_info);
        Utils.updateStatusBarBackground(CLISupplyInfoActivity.this);
        connectionDetector = new ConnectionDetector();
        mWoolworthsApplication = (WoolworthsApplication)getApplication();
        mUpdateBankDetail = mWoolworthsApplication.updateBankDetail;
        mLayoutInflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        slidingUpViewLayout = new SlidingUpViewLayout(this,mLayoutInflater);

        initViews();
        setActionBar();
        setListener();
        setCLIContent();
        mArrCreditLimit = getCreditLimitInfo();
        setRecycleView(mArrCreditLimit);
        hideSoftKeyboard();
        radioCheckStateChanged();
    }

    private void radioCheckStateChanged() {
        mRadApplySolvency.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radioNoSolvency:
                        isConfidential=true;
                        Log.e("SolvencyYes","isSolvencyTrue");
                        break;
                    case R.id.radioYesSolvency:
                        isConfidential = false;
                        Log.e("SolvencyYes","isSolvencyFalse");
                        displayConfidentialPopUp();
                        break;
                }
                hideSoftKeyboard();
            }
        });

        mRadConfidentialCredit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radioYesConfidentialCredit:
                        break;
                    case R.id.radioNoConfidentialCredit:
                           displaySolvencyPopUp();
                        break;
                }
                hideSoftKeyboard();
            }
        });
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mStepIndicator = (StepIndicator) findViewById(R.id.step_indicator);
        mTextAmount = (WEditTextView) findViewById(R.id.textAmount);
        mBtnContinue = (WButton) findViewById(R.id.btnContinue);
        mRecycleList = (RecyclerView) findViewById(R.id.recycleList);
        mNestedScrollview =(NestedScrollView)findViewById(R.id.mNestedScrollview);
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
        mTextACreditLimit.setText(getString(R.string.cli_additional_credit_amount));
        mTextACreditLimit.setAllCaps(true);
        mTextAmount.addTextChangedListener(new NumberTextWatcher(mTextAmount));
        mTextProceedToSolvency.setVisibility(View.VISIBLE);
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
                hideSoftKeyboard();
                    String creditAmount = mTextAmount.getText().toString();

                        if(!TextUtils.isEmpty(creditAmount))
                            mCreditLimitAmount = Integer.valueOf(creditAmount.replaceAll("[^0-9.]", ""));
                        else
                            mCreditLimitAmount=0;
                        if (mRadApplySolvency.getCheckedRadioButtonId() == -1) {
                            slidingUpViewLayout.openOverlayView(getString(R.string.cli_solvency_error), SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
                        } else {
                            if (mRadConfidentialCredit.getCheckedRadioButtonId() == -1) {
                                slidingUpViewLayout.openOverlayView(getString(R.string.cli_solvency_error), SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
                            } else {

                                String selectedRadSolvency = selectedRadioGroup(mRadApplySolvency);
                                String selectedRadConfidential = selectedRadioGroup(mRadConfidentialCredit);

                                if (selectedRadSolvency.equalsIgnoreCase("NO")){
                                    isConfidential=true;
                                }else{
                                    isConfidential=false;
                                }

                                if (selectedRadConfidential.equalsIgnoreCase("YES")){
                                    isSolvency=true;
                                }else{
                                    isSolvency=false;
                                }
                                if (isConfidential&&!isSolvency) {

                                    displaySolvencyPopUp();

                                } else if (!isConfidential&&isSolvency){
                                    displayConfidentialPopUp();
                                } else if (!isConfidential&&!isSolvency){
                                    displayConfidentialPopUp();
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

//                                for (int index = 0; index < mArrCreditLimit.size(); index++) {
//                                    String amount = mArrCreditLimit.get(index).getAmount();
////                                    if (TextUtils.isEmpty(amount) || amount.equalsIgnoreCase("0")) {
////                                        WErrorDialog.setErrorMessage(CLISupplyInfoActivity.this,
////                                                getString(R.string.cli_solvency_error));
////                                        return;
////                                    }
                                    //}
                                    createOfferRequest();
                                }
                            }
                }
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

        if (mArrCreditLimit != null) {
            slidingUpView = slidingUpViewLayout.openOverlayView(mArrCreditLimit.get(position).getDescription(),
                    SlidingUpViewLayout.OVERLAY_TYPE.INFO);
            slidingUpViewLayout.setPopupWindowTouchModal(slidingUpView,true);
        }

    }

    @Override
    public void scrollToBottom() {
        mNestedScrollview.fullScroll(View.FOCUS_DOWN);
        hideSoftKeyboard();
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
                slidingUpViewLayout.dismissLayout();
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

    public void createOfferRequest() {
        if (connectionDetector.isOnline(CLISupplyInfoActivity.this)) {
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
                                    slidingUpViewLayout.openOverlayView(createOfferResponse.response.desc, SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
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
        }else{
            slidingUpViewLayout.openOverlayView(getString(R.string.connect_to_server), SlidingUpViewLayout.OVERLAY_TYPE.ERROR);
        }
    }

    public int getNumbers(int position) {
        if (mArrCreditLimit != null) {
            return Integer.valueOf(mArrCreditLimit.get(position).getAmount().replaceAll("[^0-9.]", ""));
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
        try {
            if (getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }catch (NullPointerException ex){}
    }

    public class NumberTextWatcher implements TextWatcher {

        private DecimalFormat df;
        private DecimalFormat dfnd;
        private boolean hasFractionalPart;

        private EditText et;

        public NumberTextWatcher(EditText et)
        {
            df = new DecimalFormat("#,###.##");
            df.setDecimalSeparatorAlwaysShown(true);
            dfnd = new DecimalFormat("#,###");
            this.et = et;
            hasFractionalPart = false;
        }

        @SuppressWarnings("unused")
        private static final String TAG = "NumberTextWatcher";

        @Override
        public void afterTextChanged(Editable s)
        {
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
                    } catch (ParseException e) {
                    }
                    int cp = et.getSelectionStart();
                    String finalAmount="";
                    if (hasFractionalPart) {
                        finalAmount = "R " + df.format(n).replace(".", " ").replace(","," ");
                    } else {
                        finalAmount = "R " + dfnd.format(n).replace(".", " ").replace(","," ");
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
            }catch(NumberFormatException nfe){
            }
            et.addTextChangedListener(this);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if (s.toString().contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator())))
            {
                hasFractionalPart = true;
            } else {
                hasFractionalPart = false;
            }
        }
    }

    public PopupWindow displaySolvencyPopUp() {
        //darken the current screen
        View view = getLayoutInflater().inflate(R.layout.open_nativemaps_layout, null);
        darkenScreen = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        darkenScreen.setAnimationStyle(R.style.Darken_Screen);
        darkenScreen.showAtLocation(view, Gravity.CENTER, 0, 0);
        darkenScreen.setOutsideTouchable(false);
        //Then popup window appears
        final View popupView = getLayoutInflater().inflate(R.layout.cli_insolvency_popup, null);
        mBtnCancel = (WButton) popupView.findViewById(R.id.btnCancel);
        mBtnContinue = (WButton) popupView.findViewById(R.id.btnContinue);
        pWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pWindow.setAnimationStyle(R.style.Animations_popup);
        pWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        pWindow.setOutsideTouchable(false);
        //Dismiss popup when touch outside
        pWindow.setTouchable(false);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pWindow.dismiss();
                    }
                }, 200);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        darkenScreen.dismiss();
                    }
                }, 300);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                }, 400);

            }
        });
        mBtnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pWindow.dismiss();
                darkenScreen.dismiss();
            }
        });
        return darkenScreen;
    }

    public PopupWindow displayConfidentialPopUp() {
        //darken the current screen
        View view = getLayoutInflater().inflate(R.layout.open_nativemaps_layout, null);
        mDarkenScreen = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mDarkenScreen.setAnimationStyle(R.style.Darken_Screen);
        mDarkenScreen.showAtLocation(view, Gravity.CENTER, 0, 0);
        mDarkenScreen.setOutsideTouchable(false);
        //Then popup window appears
        final View popupView = getLayoutInflater().inflate(R.layout.cli_confidential_popup, null);
        mBtnCancel = (WButton) popupView.findViewById(R.id.btnCancel);
        mBtnContinue = (WButton) popupView.findViewById(R.id.btnContinue);
        mTextApplicationNotProceed = (WTextView) popupView.findViewById(R.id.textApplicationNotProceed);
        mTextOverlayDescription = (WTextView) popupView.findViewById(R.id.overlayDescription);
        mTextApplicationNotProceed.setText(getString(R.string.cli_cancel_application));
        mTextOverlayDescription.setText(getString(R.string.cli_cancel_application_desc));
        mPopWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mPopWindow.setAnimationStyle(R.style.Animations_popup);
        mPopWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        mPopWindow.setOutsideTouchable(false);
        //Dismiss popup when touch outside
        mPopWindow.setTouchable(false);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPopWindow.dismiss();
                    }
                }, 200);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDarkenScreen.dismiss();
                    }
                }, 300);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                }, 400);

            }
        });


        mBtnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopWindow.dismiss();
                mDarkenScreen.dismiss();
            }
        });

        mBtnCancel.setText("YES");
        mBtnContinue.setText("NO");

        return darkenScreen;
    }
}
