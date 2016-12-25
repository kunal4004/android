package za.co.woolworths.financial.services.android.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferResponse;
import za.co.woolworths.financial.services.android.models.dto.CreditLimit;
import za.co.woolworths.financial.services.android.models.dto.OfferResponse;
import za.co.woolworths.financial.services.android.ui.adapters.CLICreditLimitAdapter;
import za.co.woolworths.financial.services.android.ui.views.StepIndicator;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SlidingUpViewLayout;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.view.CLICreditLimitContentBinder;


/**
 * Created by dimitrij on 2016/12/20.
 */

public class CLISupplyInfoActivity extends AppCompatActivity implements View.OnClickListener,CLICreditLimitContentBinder.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private StepIndicator mStepIndicator;
    private WTextView mTextAmount;
    private WButton mBtnContinue;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecycleList;
    private CLICreditLimitAdapter mCLICreditLimitAdapter;
    private CLISupplyInfoActivity mActivity;
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
    public CreateOfferResponse createOffer;
    final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    private ProgressDialog mCreateOfferProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cli_supply_info);
        mActivity = this;
        Utils.updateStatusBarBackground(CLISupplyInfoActivity.this);
        initViews();
        setActionBar();
        setListener();
        setCLIContent();
        mArrCreditLimit = getCreditLimitInfo();
        setRecycleView(mArrCreditLimit);
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mStepIndicator = (StepIndicator) findViewById(R.id.step_indicator);
        mTextAmount = (WTextView) findViewById(R.id.textAmount);
        mBtnContinue=(WButton)findViewById(R.id.btnContinue);
        mRecycleList=(RecyclerView)findViewById(R.id.recycleList);
        mRadApplySolvency =(RadioGroup)findViewById(R.id.radApplySolvency);
        mRadioYesSolvency = (RadioButton)findViewById(R.id.radioYesSolvency);
        mRadioNoSolvency = (RadioButton)findViewById(R.id.radioNoSolvency);
        mRadConfidentialCredit =(RadioGroup)findViewById(R.id.radConfidentialCredit);
        mRadioYesConfidentialCredit = (RadioButton)findViewById(R.id.radioYesConfidentialCredit);
        mRadioNoConfidentialCredit = (RadioButton)findViewById(R.id.radioNoConfidentialCredit);
        mTextApplySolvency = (WTextView)findViewById(R.id.textApplySolvency);
        mImageCreditAmount = (ImageView)findViewById(R.id.imgInfo);
    }

    private void setActionBar(){
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayUseLogoEnabled(false);
        mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeAsUpIndicator(R.drawable.close_24);
    }

    public void setListener(){
        mBtnContinue.setOnClickListener(this);
        mRadioYesSolvency.setOnCheckedChangeListener(this);
        mRadioNoSolvency.setOnCheckedChangeListener(this);
        mRadioYesConfidentialCredit.setOnCheckedChangeListener(this);
        mRadioNoConfidentialCredit.setOnCheckedChangeListener(this);
        mImageCreditAmount.setOnClickListener(this);
    }

    private void setCLIContent(){
        mStepIndicator.setDefaultView(0);
        mStepIndicator.setEnabled(false);
        mTextApplySolvency.setText(getString(R.string.cli_apply_insolvency));
        mTextAmount.setVisibility(View.VISIBLE);
        mImageCreditAmount.setVisibility(View.GONE);
    }

    private void setRecycleView(List<CreditLimit> creditLimit){
        mCLICreditLimitAdapter = new CLICreditLimitAdapter(creditLimit,this);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleList.setLayoutManager(mLayoutManager);
        mRecycleList.setNestedScrollingEnabled(false);
        mRecycleList.setAdapter(mCLICreditLimitAdapter);
        mCLICreditLimitAdapter.setCLIContent();
        mRecycleList.setFocusable(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnContinue:
                mBtnContinue.startAnimation(buttonClick);
                String selectedRadSolvency = selectedRadioGroup(mRadApplySolvency);
                String selectedRadConfidential = selectedRadioGroup(mRadConfidentialCredit);

                 createOfferRequest();
                break;
            default:
        }
    }

    public List<CreditLimit> getCreditLimitInfo(){
        ArrayList<CreditLimit>creditLimits = new ArrayList<>();
        if(creditLimits!=null)
            creditLimits.clear();
        String[] clAmountArray = getResources().getStringArray(R.array.cl_amount);
        String[] clArrayName = getResources().getStringArray(R.array.cl_label);
        String[] clArrayDescription = getResources().getStringArray(R.array.cl_overlay_label);

        for (int x=0;x<clAmountArray.length;x++){
            creditLimits.add(new CreditLimit(clArrayName[x],clAmountArray[x],clArrayDescription[x]));
        }
        return creditLimits;
    }

    public void setRadioButtonBold(){
        try {
            RadioButton checked = (RadioButton) findViewById(mRadApplySolvency.getCheckedRadioButtonId());
            checked.setTypeface(Typeface.DEFAULT_BOLD);
        }catch (NullPointerException ex){}
        try {
            RadioButton checked = (RadioButton) findViewById(mRadConfidentialCredit.getCheckedRadioButtonId());
            checked.setTypeface(Typeface.DEFAULT_BOLD);
        }catch (NullPointerException ex){}
    }

    @Override
    public void onClick(View v, int position) {
        SlidingUpViewLayout slidingUpViewLayout = new SlidingUpViewLayout(this);
        if (mArrCreditLimit!=null) {
            slidingUpViewLayout.openOverlayGotIT(mArrCreditLimit.get(position).getDescription());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent openPreviousActivity = new Intent(CLISupplyInfoActivity.this,CLIActivity.class);
                startActivity(openPreviousActivity);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return  true;
        }
        return false;
    }

    public String selectedRadioGroup(RadioGroup radioGroup){
        int radioID = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) radioGroup.findViewById(radioID);
        String selectedConfidentialCredit = (String) radioButton.getText();
        return selectedConfidentialCredit;
    }

    public void createOfferRequest(){
        new HttpAsyncTask<String, String, CreateOfferResponse>() {
            @Override
            protected CreateOfferResponse httpDoInBackground(String... params) {
               CreateOfferRequest createOfferRequest = new CreateOfferRequest(1,
                        60000,
                        getNumbers(0),
                        getNumbers(1),
                        getNumbers(2),
                        getNumbers(3),
                        getNumbers(4),
                        getNumbers(5),
                        getNumbers(6),
                        getNumbers(7));
                return ((WoolworthsApplication) getApplication()).getApi().createOfferRequest(createOfferRequest);
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
                if(createOfferResponse!=null) {
                    String response_code = createOfferResponse.response.code;
                    if (response_code!=null) {
                        switch (Integer.valueOf(response_code)) {
                            case 200:
                                Log.e("insideOnPostExecute", "success");
                                break;
                            default:
                              //  WErrorDialog.setErrorMessage(CLISupplyInfoActivity.this, createOfferResponse.response.desc);
                                break;
                        }
                    }
                }
                stopProgressDialog();
                openBankDetails();
            }

            @Override
            protected Class<CreateOfferResponse> httpDoInBackgroundReturnType() {
                return CreateOfferResponse.class;
            }
        }.execute();
    }

    public int getNumbers(int position){
        if(mArrCreditLimit!=null){
            return Integer.valueOf(mArrCreditLimit.get(position).getAmount().replace("R","").replace(" ",""));
        }else {
            return 0;
        }
    }

    public void stopProgressDialog(){
        if(mCreateOfferProgressDialog != null && mCreateOfferProgressDialog.isShowing()){
            mCreateOfferProgressDialog.dismiss();
        }
    }

    public void openBankDetails(){
        Intent openCLIStepIndicatorActivity = new Intent(CLISupplyInfoActivity.this,CLIStepIndicatorActivity.class);
        startActivity(openCLIStepIndicatorActivity);
        finish();
        overridePendingTransition(0,0);
    }
}

