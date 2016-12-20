package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.CLI;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountsCardsAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;

public class CLIActivity extends AppCompatActivity implements View.OnClickListener {

    private WCustomViewPager mViewPager;
    private WTextView mTextToolbar;
    private LinearLayout mCardsLayoutBackground;
    boolean isCreditCard = false;
    boolean isStoreCard = false;
    boolean isPersonalCard = false;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCollapsingToolbarLayout = null;
    private WTextView mTextCreditLimit;
    private WTextView mTextBeforeStart;
    private WTextView mTextClIContent;
    private Intent mIntent;
    private int mPosition=0;
    private WButton mBtnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cli);
        Utils.updateStatusBarBackground(CLIActivity.this);
        initViews();
        setCurrentIndex();
        setListener();
        setActionBar();
        setPagerCard();
    }

    public void setCurrentIndex(){
        mIntent = getIntent();
        if (mIntent!=null){
            mPosition = mIntent.getIntExtra("position",0);
        }
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mTextToolbar = (WTextView) findViewById(R.id.toolbarText);
        mTextCreditLimit = (WTextView) findViewById(R.id.textCreditLimit);
        mTextBeforeStart = (WTextView) findViewById(R.id.textBeforeStart);
        mTextClIContent = (WTextView) findViewById(R.id.textClIContent);
        mBtnContinue = (WButton)findViewById(R.id.btnContinue);
        mViewPager = (WCustomViewPager) findViewById(R.id.myAccountsCardPager);
    }

    public void setListener(){
        mBtnContinue.setOnClickListener(this);
    }

    private void setActionBar(){
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayUseLogoEnabled(false);
        mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeAsUpIndicator(R.drawable.close_white);
    }

    private void setPagerCard() {
        mViewPager.setAdapter(new MyAccountsCardsAdapter(CLIActivity.this));
        mViewPager.setPageMargin(16);
        mViewPager.setCurrentItem(mPosition);
        setCLIContent(mPosition);
        mViewPager.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });
    }

    public void setCLIContent(int position){
        ArrayList<CLI>arrCLI = arrCLIContent();
        CLI cli = arrCLI.get(position);
        int mColor = ContextCompat.getColor(CLIActivity.this, cli.getmColor());
        mTextToolbar.setText(cli.getmTitle());
        mCollapsingToolbarLayout.setBackgroundResource(cli.getmImage());
        mCollapsingToolbarLayout.setContentScrimColor(mColor);
        mTextCreditLimit.setText(cli.getmSubTitle());
        mTextBeforeStart.setText(cli.getmBoldText());
        mTextClIContent.setText(cli.getmDescription());
        Utils.updateStatusBarBackground(CLIActivity.this,cli.getmColor());
    }

    private ArrayList<CLI> arrCLIContent(){
        ArrayList<CLI>arrCLI = new ArrayList<>();
        //store card
        arrCLI.add(new CLI(R.drawable.accounts_storecard_background,
                R.color.cli_store_card,
                getString(R.string.store_card),
                getString(R.string.cli_credit_limit_increase),
                getString(R.string.cli_crd_before_we_get_started),
                getString(R.string.cli_store_card_content)));

        //gold credit card
//        arrCLI.add(new CLI(R.drawable.accounts_blackcreditcard_background,
//                R.color.cli_credit_card,
//                getString(R.string.credit_card),
//                getString(R.string.cli_gold_crd_credit_limit_increase),
//                getString(R.string.cli_gold_crd_before_we_get_started),
//                getString(R.string.cli_gold_crd_credit_card_content)));

        //black credit card
        arrCLI.add(new CLI(R.drawable.accounts_blackcreditcard_background,
                R.color.cli_credit_card,
                getString(R.string.credit_card),
                getString(R.string.cli_crd_credit_limit_increase),
                getString(R.string.cli_crd_before_we_get_started),
                getString(R.string.cli_crd_credit_card_content)));

        //personal loan
        arrCLI.add(new CLI(R.drawable.accounts_personalloancard_background,
                R.color.cli_personal_loan,
                getString(R.string.personal_loan),
                getString(R.string.cli_psl_credit_limit_increase),
                getString(R.string.cli_psl_before_we_get_started),
                getString(R.string.cli_psl_content)));
        return arrCLI;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnContinue:
                Toast.makeText(this,"continue",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
