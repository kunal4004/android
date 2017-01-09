package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.ui.adapters.CardsFragmentPagerAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.MyAccountsCardsAdapter;
import za.co.woolworths.financial.services.android.ui.fragments.BaseAccountFragment;
import za.co.woolworths.financial.services.android.ui.fragments.CLIFourthStepFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WCreditCardEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WCreditCardFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WPersonalLoanEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WPersonalLoanFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardFragment;
import za.co.woolworths.financial.services.android.ui.views.WFragmentViewPager;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WCustomViewPager;

public class MyAccountCardsActivity extends AppCompatActivity {

    WCustomViewPager pager;
    WFragmentViewPager fragmentPager;
    public WTextView toolbarTextView;
    public LinearLayout cardsLayoutBackground;
    boolean isCreditCard = false;
    boolean isStoreCard = false;
    boolean isPersonalCard = false;
    CardsFragmentPagerAdapter fragmentsAdapter;
    private CollapsingToolbarLayout collapsingToolbarLayout = null;
    private WoolworthsApplication mWoolworthsApplication;
    public static Activity myAccountCardsActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account_cards_test);
        myAccountCardsActivity = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        mWoolworthsApplication = (WoolworthsApplication)getApplication();
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbarTextView = (WTextView) findViewById(R.id.toolbarText);
        pager = (WCustomViewPager) findViewById(R.id.myAccountsCardPager);
        fragmentPager = (WFragmentViewPager) findViewById(R.id.fragmentpager);
        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.nest_scrollview);
        scrollView.setFillViewport(true);
        pager.setAdapter(new MyAccountsCardsAdapter(MyAccountCardsActivity.this));
        pager.setPageMargin(50);
        fragmentPager.setPagingEnabled(false);
        pager.setCurrentItem(getIntent().getIntExtra("position",0));
        setStatusBarColor(getIntent().getIntExtra("position",0));
        //fragmentPager.setCurrentItem(getIntent().getIntExtra("position",0));
        changeViewPagerAndActionBarBackground(getIntent().getIntExtra("position",0));
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mWoolworthsApplication.setCliCardPosition(position);
                fragmentPager.setCurrentItem(position);
                changeViewPagerAndActionBarBackground(position);
                setStatusBarColor(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        // dynamicToolbarColor();
       // dynamicToolbarColor("#4f5051");
        if(getIntent().hasExtra("accounts")) {
            AccountsResponse accountsResponse = new Gson().fromJson(getIntent().getExtras().getString("accounts"), AccountsResponse.class);
            handleAccountsResponse(accountsResponse);
        }
        else {
            fragmentsAdapter = new CardsFragmentPagerAdapter(getSupportFragmentManager());
            fragmentsAdapter.addFrag(new WStoreCardEmptyFragment());
            fragmentsAdapter.addFrag( new WCreditCardEmptyFragment());
            fragmentsAdapter.addFrag( new WPersonalLoanEmptyFragment());
            fragmentPager.setAdapter(fragmentsAdapter);
            fragmentPager.setCurrentItem(getIntent().getIntExtra("position",0));
        }

    }

    private void dynamicToolbarColor(String colorString) {
        collapsingToolbarLayout.setContentScrimColor(Color.parseColor(colorString));

    }

    public void changeViewPagerAndActionBarBackground(int position) {
        switch (position) {
            case 0:
                toolbarTextView.setText("STORE CARD");
                collapsingToolbarLayout.setBackgroundResource(R.drawable.accounts_storecard_background);
                dynamicToolbarColor("#4f5051");
                break;
            case 1:
                toolbarTextView.setText("CREDIT CARD");
                collapsingToolbarLayout.setBackgroundResource(R.drawable.accounts_blackcreditcard_background);
                dynamicToolbarColor("#2e353b");
                break;
            case 2:
                toolbarTextView.setText("PERSONAL LOAN");
                collapsingToolbarLayout.setBackgroundResource(R.drawable.accounts_personalloancard_background);
                dynamicToolbarColor("#312439");
                break;
        }
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

    private void handleAccountsResponse(AccountsResponse accountsResponse) {
        switch (accountsResponse.httpCode) {
            case 200:

                ((WoolworthsApplication) getApplication()).getUserManager().setAccounts(accountsResponse);
                ArrayList<BaseAccountFragment> baseAccountFragments = new ArrayList<BaseAccountFragment>();
                List<Account> accountList = accountsResponse.accountList;
                boolean containsStoreCard = false, containsCreditCard = false, containsPersonalLoan = false;
                if (accountList != null) {
                    for (Account p : accountList) {
                        if ("SC".equals(p.productGroupCode)) {
                            containsStoreCard = true;
                        } else if ("CC".equals(p.productGroupCode)) {
                            containsCreditCard = true;
                        } else if ("PL".equals(p.productGroupCode)) {
                            containsPersonalLoan = true;
                        }
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putString("accounts", Utils.objectToJson(accountsResponse));
                fragmentsAdapter = new CardsFragmentPagerAdapter(getSupportFragmentManager());
                if (containsStoreCard) {
                    WStoreCardFragment fragment = new WStoreCardFragment();
                    fragment.setArguments(bundle);
                    fragmentsAdapter.addFrag(fragment);
                } else {
                    fragmentsAdapter.addFrag(new WStoreCardEmptyFragment());
                }
                if (containsCreditCard) {
                    WCreditCardFragment fragment = new WCreditCardFragment();
                    fragment.setArguments(bundle);
                    fragmentsAdapter.addFrag(fragment);
                } else {
                    fragmentsAdapter.addFrag(new WCreditCardEmptyFragment());
                }
                if (containsPersonalLoan) {
                    WPersonalLoanFragment fragment = new WPersonalLoanFragment();
                    fragment.setArguments(bundle);
                    fragmentsAdapter.addFrag(fragment);
                } else {
                    fragmentsAdapter.addFrag(new WPersonalLoanEmptyFragment());
                }
                fragmentPager.setAdapter(fragmentsAdapter);
                fragmentPager.setCurrentItem(getIntent().getIntExtra("position",0));

                break;
            case 400:
                if ("0619".equals(accountsResponse.response.code) || "0618".equals(accountsResponse.response.code)) {

                    break;

                }
            case 502:
                Log.i("Handling 502","Handled a 502 error from the server");
                break;
            default:
                ((TextView) findViewById(R.id.no_internet_message)).setText(accountsResponse.response.desc);
                findViewById(R.id.no_internet).setVisibility(View.VISIBLE);
        }
    }

    public void setStatusBarColor(int position){
        switch (position){
            case 0:
                Utils.updateStatusBarBackground(MyAccountCardsActivity.this,R.color.cli_store_card);
                break;
            case 1:
                Utils.updateStatusBarBackground(MyAccountCardsActivity.this,R.color.cli_credit_card);
                break;
            case 2:
                Utils.updateStatusBarBackground(MyAccountCardsActivity.this,R.color.cli_personal_loan);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == SSOActivity.SSOActivityResult.EXPIRED.rawValue()){
            setResult(resultCode);
            finish();
        }
    }

    public static class MyAccountCardsFragment extends Fragment{

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            getActivity().setResult(resultCode);
        }
    }
}
