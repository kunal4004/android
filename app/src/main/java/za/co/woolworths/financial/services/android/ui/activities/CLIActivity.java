package za.co.woolworths.financial.services.android.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import retrofit.RetrofitError;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.UserManager;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountResponse;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.CLI;
import za.co.woolworths.financial.services.android.models.dto.Offer;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.OfferActiveResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class CLIActivity extends AppCompatActivity implements View.OnClickListener {

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
    final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    private ImageView mImageAccount;
    private ProgressDialog mGetActiveOfferProgressDialog;

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
        setAppBarDragging(false);
      //  getActiveOffer();
        //loadOffer();
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
        mImageAccount = (ImageView)findViewById(R.id.myaccountsCard);
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
        int[] cards={R.drawable.w_store_card,R.drawable.w_credi_card,R.drawable.w_personal_loan_card};
        mImageAccount.setBackgroundResource(cards[mPosition]);
       setCLIContent(mPosition);
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

    private void setAppBarDragging(final boolean newValue) {
        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.mAppBarLayout);
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return newValue;
            }
        });
        params.setBehavior(behavior);
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

                mBtnContinue.startAnimation(buttonClick);


                switch (mPosition){
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                Intent openCLIStepIndicator = new Intent(CLIActivity.this,CLISupplyInfoActivity.class);
                startActivity(openCLIStepIndicator);
                finish();
               // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);

                break;
        }
    }

    private void getActiveOffer() {
        final Account account = new Gson().fromJson(((WoolworthsApplication)getApplication()).getUserManager().getAccount(UserManager.CREDIT_CARD), Account.class);
        new HttpAsyncTask<String, String, OfferActive>() {
            @Override
            protected OfferActive httpDoInBackground(String... params) {
                return ((WoolworthsApplication) getApplication()).getApi().getActiveOffer("3");
            }

            @Override
            protected OfferActive httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                OfferActive offerActive = new OfferActive();
                offerActive.response = new OfferActiveResponse();
                stopProgressDialog();
                return offerActive;
            }

            @Override
            protected void onPreExecute() {
                mGetActiveOfferProgressDialog = new ProgressDialog(CLIActivity.this);
                mGetActiveOfferProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.cli_loading), 1, CLIActivity.this));
                mGetActiveOfferProgressDialog.setCancelable(false);
                mGetActiveOfferProgressDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(OfferActive offerActive) {
                super.onPostExecute(offerActive);
                OfferActive offerAct = offerActive;
                stopProgressDialog();
            }

            @Override
            protected Class<OfferActive> httpDoInBackgroundReturnType() {
                return OfferActive.class;
            }
        }.execute();
    }

    public void stopProgressDialog(){
        if(mGetActiveOfferProgressDialog != null && mGetActiveOfferProgressDialog.isShowing()){
            mGetActiveOfferProgressDialog.dismiss();
        }
    }

    public void loadOffer(){
        new AsyncTask<Integer, String, OfferActive>() {
            @Override
            protected OfferActive doInBackground(Integer... params) {
                try {
                    return ((WoolworthsApplication) getApplication()).getApi().getActiveOffer("3");
                } catch (RetrofitError e) {
                    try {
                        retrofit.client.Response response = e.getResponse();
                        if (response != null) {
                            return new Gson().fromJson(new InputStreamReader(response.getBody().in()), OfferActive.class);
                        } else {
                            OfferActive accountResponse = new OfferActive();
                            accountResponse.httpCode = 408;
                            accountResponse.response = new OfferActiveResponse();
                            accountResponse.response.desc = getString(R.string.err_002);
                            return accountResponse;
                        }
                    } catch (IOException e1) {
                        return null;
                    }
                }
            }

            @Override
            protected void onPostExecute(OfferActive accountResponse) {
            super.onPostExecute(accountResponse);
            }
        }.execute();
    }
}
