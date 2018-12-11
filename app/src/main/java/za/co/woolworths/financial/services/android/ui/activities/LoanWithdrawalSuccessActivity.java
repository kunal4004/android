package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by dimitrij on 2017/01/10.
 */

public class LoanWithdrawalSuccessActivity extends BaseActivity {

    private WButton mBtnOk;
    private RelativeLayout mRelLoanWithdrawalSuccess;
    private Toolbar mToolbar;
    private SharePreferenceHelper mSharePreferenceHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(LoanWithdrawalSuccessActivity.this, R.color.purple);
        setContentView(R.layout.loan_withdrawal_activity);
        mSharePreferenceHelper = SharePreferenceHelper.getInstance(LoanWithdrawalSuccessActivity.this);
        setActionBar();
        mRelLoanWithdrawalSuccess =  (RelativeLayout)findViewById(R.id.linLoanWithdrawalSuccess);
        mRelLoanWithdrawalSuccess.setVisibility(View.VISIBLE);
        mBtnOk =  (WButton)findViewById(R.id.btnOk);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canGoBack();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.WITHDRAW_CASH_SUCCESSFUL);
    }

    @Override
    public void onBackPressed() {
        canGoBack();
    }

    public void canGoBack(){
        mSharePreferenceHelper.removeValue("lw_installment_amount");
        mSharePreferenceHelper.removeValue("lwf_drawDownAmount");
        mSharePreferenceHelper.removeValue("lw_months");
        mSharePreferenceHelper.removeValue("lw_product_offering_id");
        mSharePreferenceHelper.removeValue("lw_amount_drawn_cent");
        mSharePreferenceHelper.removeValue("lw_credit_limit");
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void setActionBar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayUseLogoEnabled(false);
        mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeAsUpIndicator(R.drawable.close_white);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                canGoBack();
                return true;
        }
        return false;
    }
}
