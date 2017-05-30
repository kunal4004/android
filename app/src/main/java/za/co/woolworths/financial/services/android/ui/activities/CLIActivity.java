package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.CLI;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.Utils;

public class CLIActivity extends BaseActivity implements View.OnClickListener {

    private WTextView mTextToolbar;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCollapsingToolbarLayout = null;
    private WTextView mTextCreditLimit;
    private WTextView mTextBeforeStart;
    private WTextView mTextClIContent;
    private Intent mIntent;
    private int mPosition = 0;
    private WButton mBtnContinue;
    final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    private ImageView mImageAccount;
    private WoolworthsApplication woolworthsApplication;
    private AppBarLayout mAppBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cli);
        woolworthsApplication = (WoolworthsApplication) getApplication();
        Utils.updateStatusBarBackground(CLIActivity.this);
        initViews();
        setCurrentIndex();
        setListener();
        setActionBar();
        setPagerCard(woolworthsApplication.getCliCardPosition());
        setStatusBarColor(woolworthsApplication.getCliCardPosition());
    }

    public void setCurrentIndex() {
        mIntent = getIntent();
        if (mIntent != null) {
            mPosition = mIntent.getIntExtra("position", 0);
        }
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setCollapsible(false);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mTextToolbar = (WTextView) findViewById(R.id.toolbarText);
        mTextToolbar.setGravity(Gravity.LEFT);
        mTextCreditLimit = (WTextView) findViewById(R.id.textCreditLimit);
        mTextBeforeStart = (WTextView) findViewById(R.id.textBeforeStart);
        mTextClIContent = (WTextView) findViewById(R.id.textClIContent);
        mBtnContinue = (WButton) findViewById(R.id.btnContinue);
        mImageAccount = (ImageView) findViewById(R.id.myaccountsCard);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.mAppBarLayout);

    }

    public void setListener() {
        mBtnContinue.setOnClickListener(this);
    }

    private void setActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayUseLogoEnabled(false);
        //mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeAsUpIndicator(R.drawable.close_white);
    }

    private void setPagerCard(int id) {
        int[] cards = {R.drawable.w_store_card, R.drawable.w_credi_card, R.drawable.w_personal_loan_card};
        if (id == 1) {
            if (WoolworthsApplication.getCreditCardType().equalsIgnoreCase(Utils.SILVER_CARD)) {
                mImageAccount.setImageResource(R.drawable.w_silver_credit_card);
            } else if (WoolworthsApplication.getCreditCardType().equalsIgnoreCase(Utils.GOLD_CARD)) {
                mImageAccount.setImageResource(R.drawable.w_gold_credit_card);
            } else if (WoolworthsApplication.getCreditCardType().equalsIgnoreCase(Utils.BLACK_CARD)) {
                mImageAccount.setImageResource(R.drawable.w_credi_card);
            }
        } else {
            mImageAccount.setImageResource(cards[id]);
        }
        setCLIContent(id);

    }

    public void setCLIContent(int position) {
        ArrayList<CLI> arrCLI = arrCLIContent();
        CLI cli = arrCLI.get(position);
        int mColor = ContextCompat.getColor(CLIActivity.this, cli.getmColor());
        mTextToolbar.setText(cli.getmTitle());
        mCollapsingToolbarLayout.setBackgroundResource(cli.getmImage());
        mCollapsingToolbarLayout.setContentScrimColor(Color.TRANSPARENT);
        mCollapsingToolbarLayout.setScrimsShown(false);
        mTextCreditLimit.setText(cli.getmSubTitle());
        mTextBeforeStart.setText(cli.getmBoldText());
        mTextClIContent.setText(cli.getmDescription());
        mToolbar.setBackgroundColor(mColor);
        //closeButton.setBackgroundColor(mColor);
        Utils.updateStatusBarBackground(CLIActivity.this, cli.getmColor());
    }

    private ArrayList<CLI> arrCLIContent() {
        ArrayList<CLI> arrCLI = new ArrayList<>();
        //store card
        arrCLI.add(new CLI(R.drawable.accounts_storecard_background,
                R.color.cli_store_card,
                getString(R.string.store_card),
                getString(R.string.cli_credit_limit_increase),
                getString(R.string.cli_crd_before_we_get_started),
                getString(R.string.cli_activity_desc)));

        //black credit card
        arrCLI.add(new CLI(R.drawable.accounts_blackcreditcard_background,
                R.color.cli_credit_card,
                getString(R.string.credit_card),
                getString(R.string.cli_crd_credit_limit_increase),
                getString(R.string.cli_crd_before_we_get_started),
                getString(R.string.cli_activity_desc)));

        //personal loan
        arrCLI.add(new CLI(R.drawable.accounts_personalloancard_background,
                R.color.cli_personal_loan,
                getString(R.string.personal_loan),
                getString(R.string.cli_psl_credit_limit_increase),
                getString(R.string.cli_psl_before_we_get_started),
                getString(R.string.cli_activity_desc)));
        return arrCLI;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnContinue:
                mBtnContinue.startAnimation(buttonClick);
                Intent openCLIStepIndicator = new Intent(CLIActivity.this, CLISupplyInfoActivity.class);
                startActivity(openCLIStepIndicator);
                finish();
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
        }
    }

    private void setStatusBarColor(int position) {
        switch (position) {
            case 0:
                Utils.updateStatusBarBackground(this, R.color.cli_store_card);
                break;

            case 1:
                Utils.updateStatusBarBackground(this, R.color.cli_credit_card);
                break;

            case 2:
                Utils.updateStatusBarBackground(this, R.color.cli_personal_loan);
                break;
        }
    }

}
