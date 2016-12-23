package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.CreditLimit;
import za.co.woolworths.financial.services.android.ui.adapters.CLICreditLimitAdapter;
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout;
import za.co.woolworths.financial.services.android.ui.views.StepIndicator;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.view.CLICreditLimitContentBinder;

import static com.google.android.gms.wearable.DataMap.TAG;
import static za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity.mToolbar;

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
    private SlidingUpPanelLayout mSlideUpPanel;
    private ActionBar mActionBar;
    private AppBarLayout mAppBarLatout;
    private ImageView mImageCreditAmount;
    private LinearLayout mLinSlideUp;

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
        setRecycleView(getCreditLimitInfo());
        slideUpPanel();

    }

    private void slideUpPanel(){
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
                    mSlideUpPanel.setAnchorPoint(4.0f);
                    mAppBarLatout.animate().translationY(mAppBarLatout.getTop()).setInterpolator(new AccelerateInterpolator()).start();
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                }
            }
        });
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
        mSlideUpPanel = (SlidingUpPanelLayout)findViewById(R.id.mSlideUpPanel);
        mAppBarLatout = (AppBarLayout)findViewById(R.id.appbar);
        mImageCreditAmount = (ImageView)findViewById(R.id.imgInfo);
        mLinSlideUp = (LinearLayout)findViewById(R.id.llSlideUp);
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
        mTextAmount.setVisibility(View.VISIBLE);
        mTextApplySolvency.setText(getString(R.string.cli_apply_insolvency));


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
                Intent openCLIStepIndicatorActivity = new Intent(CLISupplyInfoActivity.this,CLIStepIndicatorActivity.class);
                startActivity(openCLIStepIndicatorActivity);
                finish();
                overridePendingTransition(0,0);
                break;
            case R.id.imgInfo:
                mLinSlideUp.setVisibility(View.VISIBLE);
                mSlideUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                break;
        }
    }

    public List<CreditLimit> getCreditLimitInfo(){
        ArrayList<CreditLimit>creditLimits = new ArrayList<>();
        if(creditLimits!=null)
            creditLimits.clear();
        String[] clAmountArray = getResources().getStringArray(R.array.cl_amount);
        String[] clArrayName = getResources().getStringArray(R.array.cl_label);
        for (int x=0;x<clAmountArray.length;x++){
            creditLimits.add(new CreditLimit(clArrayName[x],clAmountArray[x]));
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
    public void onClick(View v, int position) {}

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
}
