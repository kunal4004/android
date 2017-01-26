package za.co.woolworths.financial.services.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
import za.co.woolworths.financial.services.android.ui.fragments.WCreditCardEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WCreditCardFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WPersonalLoanEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WPersonalLoanFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardFragment;
import za.co.woolworths.financial.services.android.ui.views.WCustomPager;
import za.co.woolworths.financial.services.android.ui.views.WObservableScrollView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WViewPager;
import za.co.woolworths.financial.services.android.util.ObservableScrollViewCallbacks;
import za.co.woolworths.financial.services.android.util.PersonalLoanAmount;
import za.co.woolworths.financial.services.android.util.ScrollState;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.Utils;

public class MyAccountCardsActivity extends AppCompatActivity implements View.OnClickListener, PersonalLoanAmount, ObservableScrollViewCallbacks {

    WViewPager pager;
    WCustomPager fragmentPager;
    public WTextView toolbarTextView;
    CardsFragmentPagerAdapter fragmentsAdapter;
    private WoolworthsApplication mWoolworthsApplication;
    private SharePreferenceHelper mSharePreferenceHelper;
    ArrayList<Integer> cards;
    private Toolbar mToolbar;
    private Button mBtnApplyNow;

    private boolean cardsHasAccount = false;
    private int wMinDrawnDownAmount;
    private WObservableScrollView mWObservableScrollView;
    private int mScrollY;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //scrollToolbarOnDelay();
        setContentView(R.layout.activity_my_accounts_offline_layout);
        setActionBar();
        init();
        mSharePreferenceHelper = SharePreferenceHelper.getInstance(MyAccountCardsActivity.this);
        getScreenResolution(this);
        mWObservableScrollView.setScrollViewCallbacks(this);
        fragmentPager = (WCustomPager) findViewById(R.id.fragmentpager);
        fragmentPager.setViewPagerIsScrollable(false);
        cards = new ArrayList<>();
        int position = getIntent().getIntExtra("position", 0);
        setStatusBarColor(position);
        changeViewPagerAndActionBarBackground(position);
        changeButtonColor(position);
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
                changeButtonColor(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        cardsHasAccount = getIntent().hasExtra("accounts");
        if (cardsHasAccount) {
            AccountsResponse accountsResponse = new Gson().fromJson(getIntent().getExtras().getString("accounts"), AccountsResponse.class);
            handleAccountsResponse(accountsResponse);
        } else {
            fragmentsAdapter = new CardsFragmentPagerAdapter(getSupportFragmentManager());
            fragmentsAdapter.addFrag(new WStoreCardEmptyFragment());
            fragmentsAdapter.addFrag(new WCreditCardEmptyFragment());
            fragmentsAdapter.addFrag(new WPersonalLoanEmptyFragment());
            fragmentPager.setAdapter(fragmentsAdapter);
            fragmentPager.setCurrentItem(getIntent().getIntExtra("position", 0));
            cards.add(R.drawable.w_store_card);
            cards.add(R.drawable.w_credi_card);
            cards.add(R.drawable.w_personal_loan_card);
            setUpAdapter(cards);
        }

        mSharePreferenceHelper.save("acc_card_activity", "acc_card_activity");
        this.registerReceiver(this.finishAlert, new IntentFilter(mSharePreferenceHelper.getValue("acc_card_activity")));
    }

    public void setActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(null);
        }
    }

    private void init() {
        mWoolworthsApplication = (WoolworthsApplication) getApplication();
        toolbarTextView = (WTextView) findViewById(R.id.toolbarText);
        pager = (WViewPager) findViewById(R.id.myAccountsCardPager);
        mBtnApplyNow = (Button) findViewById(R.id.btnApplyNow);
        mWObservableScrollView = (WObservableScrollView) findViewById(R.id.nest_scrollview);
        mBtnApplyNow.setOnClickListener(this);
    }

    BroadcastReceiver finishAlert = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            MyAccountCardsActivity.this.finish();
        }
    };

    private void dynamicToolbarColor(int color) {
        int mColor = ContextCompat.getColor(MyAccountCardsActivity.this, color);
        mToolbar.setBackgroundColor((mColor));
    }

    public void changeViewPagerAndActionBarBackground(int position) {
        switch (position) {
            case 0:
                toolbarTextView.setText("STORE CARD");
                pager.setBackgroundResource(R.drawable.accounts_storecard_background);
                dynamicToolbarColor(R.color.cli_store_card);
                break;
            case 1:
                toolbarTextView.setText("CREDIT CARD");
                pager.setBackgroundResource(R.drawable.accounts_blackcreditcard_background);
                dynamicToolbarColor(R.color.cli_credit_card);
                break;
            case 2:
                toolbarTextView.setText("PERSONAL LOAN");
                pager.setBackgroundResource(R.drawable.accounts_personalloancard_background);
                dynamicToolbarColor(R.color.cli_personal_loan);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharePreferenceHelper.removeValue("acc_card_activity");
        this.unregisterReceiver(finishAlert);
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
                List<Account> accountList = accountsResponse.accountList;
                boolean containsStoreCard = false, containsCreditCard = false, containsPersonalLoan = false;
                if (accountList != null) {
                    cards.clear();
                    cards.add(R.drawable.w_store_card);
                    for (Account p : accountList) {
                        if ("SC".equals(p.productGroupCode)) {
                            containsStoreCard = true;
                        } else if ("CC".equals(p.productGroupCode)) {
                            containsCreditCard = true;
                            if (p.accountNumberBin.equalsIgnoreCase(Utils.SILVER_CARD)) {
                                cards.add(R.drawable.w_silver_credit_card);
                            } else if (p.accountNumberBin.equalsIgnoreCase(Utils.GOLD_CARD)) {
                                cards.add(R.drawable.w_gold_credit_card);
                            } else if (p.accountNumberBin.equalsIgnoreCase(Utils.BLACK_CARD)) {
                                cards.add(R.drawable.w_credi_card);
                            }
                        } else if ("PL".equals(p.productGroupCode)) {
                            containsPersonalLoan = true;
                        }
                    }
                    if (!containsCreditCard) {
                        cards.add(R.drawable.w_credi_card);
                    }
                    cards.add(R.drawable.w_personal_loan_card);
                    setUpAdapter(cards);
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
                fragmentPager.setCurrentItem(getIntent().getIntExtra("position", 0));

                break;
            case 400:
                if ("0619".equals(accountsResponse.response.code) || "0618".equals(accountsResponse.response.code)) {

                    break;

                }
            case 502:
                Log.i("Handling 502", "Handled a 502 error from the server");
                break;
            default:
                ((TextView) findViewById(R.id.no_internet_message)).setText(accountsResponse.response.desc);
                findViewById(R.id.no_internet).setVisibility(View.VISIBLE);
        }
    }

    public void setStatusBarColor(int position) {
        switch (position) {
            case 0:
                Utils.updateStatusBarBackground(MyAccountCardsActivity.this, R.color.cli_store_card);
                break;
            case 1:
                Utils.updateStatusBarBackground(MyAccountCardsActivity.this, R.color.cli_credit_card);
                break;
            case 2:
                Utils.updateStatusBarBackground(MyAccountCardsActivity.this, R.color.cli_personal_loan);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == SSOActivity.SSOActivityResult.EXPIRED.rawValue()) {
            setResult(resultCode);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnApplyNow:

                if (!cardsHasAccount) { //not logged in

                    switch (pager.getCurrentItem()) {
                        case 0:
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WoolworthsApplication.getApplyNowLink())));
                            break;

                        case 1:
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WoolworthsApplication.getApplyNowLink())));
                            break;

                        case 2:
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WoolworthsApplication.getApplyNowLink())));
                            break;
                    }

                } else {
                    switch (pager.getCurrentItem()) { //logged in
                        case 2:
                            mSharePreferenceHelper.save("", "lw_amount_drawn_cent");
                            Intent openWithdrawCashNow = new Intent(MyAccountCardsActivity.this, LoanWithdrawalActivity.class);
                            openWithdrawCashNow.putExtra("minDrawnDownAmount", wMinDrawnDownAmount);
                            startActivity(openWithdrawCashNow);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            break;
                    }
                }
                break;
        }
    }

    @Override
    public void minDrawnAmount(int amount) {
        this.wMinDrawnDownAmount = amount;

    }

    public static class MyAccountCardsFragment extends Fragment {

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            getActivity().setResult(resultCode);
        }
    }

    public void setUpAdapter(ArrayList<Integer> cardsList) {
        pager.setAdapter(new MyAccountsCardsAdapter(MyAccountCardsActivity.this, cardsList));
        pager.setCurrentItem(getIntent().getIntExtra("position", 0));
    }

    private void getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        if (width < 1000 && height < 1500) {
            pager.setPadding(100, 0, 100, 0);
            pager.setClipToPadding(false);
            pager.setPageMargin(16);

        } else {
            pager.setPadding(200, 0, 200, 0);
            pager.setClipToPadding(false);
            pager.setPageMargin(16);
        }
    }

    public void changeButtonColor(int position) {
        // not logged in
        mBtnApplyNow.setVisibility(View.GONE);
        if (!cardsHasAccount) {
            switch (position) {
                case 0:
                    mBtnApplyNow.setBackgroundColor(ContextCompat.getColor(MyAccountCardsActivity.this, R.color.cli_store_card));
                    mBtnApplyNow.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    mBtnApplyNow.setBackgroundColor(ContextCompat.getColor(MyAccountCardsActivity.this, R.color.cli_credit_card));
                    mBtnApplyNow.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    mBtnApplyNow.setVisibility(View.VISIBLE);
                    mBtnApplyNow.setBackgroundColor(ContextCompat.getColor(MyAccountCardsActivity.this, R.color.purple));
                    break;
            }
        } else { // logged in
            switch (position) {
                case 0:
                    mBtnApplyNow.setVisibility(View.GONE);
                    break;
                case 1:
                    mBtnApplyNow.setVisibility(View.GONE);
                    break;
                case 2:
                    mBtnApplyNow.setText(getString(R.string.withdraw_cash_now));
                    mBtnApplyNow.setBackgroundColor(ContextCompat.getColor(MyAccountCardsActivity.this, R.color.purple));
                    mBtnApplyNow.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void hideViews() {
        actionBar.hide();
    }

    private void showViews() {
        actionBar.show();
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        this.mScrollY = scrollY;
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState.UP == scrollState) {
            hideViews();
        } else {
            if (scrollState == scrollState.DOWN) {
                if (mScrollY < 10) {
                    showViews();
                }
            }
        }
    }
}

