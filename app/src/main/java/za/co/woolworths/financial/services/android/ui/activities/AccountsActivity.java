package za.co.woolworths.financial.services.android.ui.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.fragments.BaseAccountFragment;
import za.co.woolworths.financial.services.android.ui.fragments.CreditCardEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.CreditCardFragment;
import za.co.woolworths.financial.services.android.ui.fragments.PersonalLoanEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.PersonalLoanFragment;
import za.co.woolworths.financial.services.android.ui.fragments.StoreCardEmptyFragment;
import za.co.woolworths.financial.services.android.ui.fragments.StoreCardFragment;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WErrorDialog;


public class AccountsActivity extends BaseDrawerActivity {
    private static final String TAG = "AccountsActivity";
    public static final String LANDING_SCREEN = "LANDING_SCREEN";
    private ArrayList<BaseAccountFragment> mAccountFragments = new ArrayList<>();
    private ProgressDialog mGetAccountsProgressDialog;
    private ProgressDialog mGetWRewardsProgressDialog;
    private FragmentPagerAdapter mFragmentAdapter;
    private AlertDialog mLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accounts_activity);
        setWoolworthsTitle(R.string.my_accounts);
        findViewById(R.id.no_internet_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAccounts();
                loadWRewards();
            }
        });
        mLogin = WErrorDialog.getLoginErrorDialog(this);
        ViewPager viewPager = (ViewPager) findViewById(R.id.account_pager);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((PagerTabStrip) findViewById(R.id.account_pager_title_strip)).setTabIndicatorColor(getResources().getColor(mAccountFragments.get(position).getTabColor()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mFragmentAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {


            @Override
            public Fragment getItem(int i) {
                return mAccountFragments.get(i);
            }

            @Override
            public int getCount() {
                return mAccountFragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                String title = getString(mAccountFragments.get(position).getTitle());
                SpannableString spannableString = new SpannableString(title);
                spannableString.setSpan(new CalligraphyTypefaceSpan(Typeface.createFromAsset(getAssets(), "fonts/WFutura-Medium.ttf")), 0, title.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return spannableString;
            }
        };
        ((ViewPager) findViewById(R.id.account_pager)).setAdapter(mFragmentAdapter);
        loadAccounts();
        loadWRewards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAccountFragments.size() > 0) {
            ((PagerTabStrip) findViewById(R.id.account_pager_title_strip)).setTabIndicatorColor(getResources().getColor(mAccountFragments.get(((ViewPager) findViewById(R.id.account_pager)).getCurrentItem()).getTabColor()));
        }
    }

    private void loadAccounts() {
        mGetAccountsProgressDialog = new ProgressDialog(this);
        mGetAccountsProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.getting_accounts), 1, this));
        mGetAccountsProgressDialog.setCancelable(false);

        String productInfo = ((WoolworthsApplication) getApplication()).getUserManager().getAccounts();
        if (!productInfo.isEmpty()) {
            handleAccountsResponse(new Gson().fromJson(productInfo, AccountsResponse.class));
        } else {
            new HttpAsyncTask<String, String, AccountsResponse>() {

                @Override
                protected void onPreExecute() {
                    mGetAccountsProgressDialog.show();
                    findViewById(R.id.no_internet).setVisibility(View.GONE);
                }

                @Override
                protected Class<AccountsResponse> httpDoInBackgroundReturnType() {
                    return AccountsResponse.class;
                }

                @Override
                protected AccountsResponse httpDoInBackground(String... params) {
                    return ((WoolworthsApplication) getApplication()).getApi().getAccounts();
                }

                @Override
                protected AccountsResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {

                    WiGroupLogger.e(AccountsActivity.this, TAG, errorMessage);
                    AccountsResponse accountResponse = new AccountsResponse();
                    accountResponse.httpCode = 408;
                    accountResponse.response = new Response();
                    accountResponse.response.desc = getString(R.string.err_002);
                    return accountResponse;
                }

                @Override
                protected void onPostExecute(AccountsResponse accountsResponse) {
                    handleAccountsResponse(accountsResponse);
                    mGetAccountsProgressDialog.dismiss();
                }
            }.execute();
        }
    }

    private void loadWRewards() {
        mGetWRewardsProgressDialog = new ProgressDialog(this);
        mGetWRewardsProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.vouchers_loading), 1, this));
        mGetWRewardsProgressDialog.setCancelable(false);

        String wRewards = getWoolworthsApplication().getUserManager().getWRewards();
        if (wRewards.isEmpty()) {
            new HttpAsyncTask<String, String, VoucherResponse>() {

                @Override
                protected Class<VoucherResponse> httpDoInBackgroundReturnType() {
                    return VoucherResponse.class;
                }

                @Override
                protected VoucherResponse httpDoInBackground(String... params) {
                    return getWoolworthsApplication().getApi().getVouchers();
                }

                @Override
                protected VoucherResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    WiGroupLogger.e(AccountsActivity.this, TAG, errorMessage);
                    VoucherResponse voucherResponse = new VoucherResponse();
                    voucherResponse.httpCode = 408;
                    voucherResponse.response = new Response();
                    voucherResponse.response.desc = getString(R.string.err_002);
                    return voucherResponse;
                }

                @Override
                protected void onPreExecute() {
                    mGetWRewardsProgressDialog.show();
                    findViewById(R.id.no_internet).setVisibility(View.GONE);
                }

                @Override
                protected void onPostExecute(VoucherResponse voucherResponse) {
                    handleVoucherResponse(voucherResponse);

                    mGetWRewardsProgressDialog.dismiss();
                }
            }.execute();
        } else {
            VoucherResponse voucherResponse = new Gson().fromJson(getWoolworthsApplication().getUserManager().getWRewards(), VoucherResponse.class);
            handleVoucherResponse(voucherResponse);

            mFragmentAdapter.notifyDataSetChanged();
        }
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
                baseAccountFragments.add(containsStoreCard ? new StoreCardFragment() : new StoreCardEmptyFragment());
                baseAccountFragments.add(containsCreditCard ? new CreditCardFragment() : new CreditCardEmptyFragment());
                baseAccountFragments.add(containsPersonalLoan ? new PersonalLoanFragment() : new PersonalLoanEmptyFragment());
                mAccountFragments = baseAccountFragments;
                mFragmentAdapter.notifyDataSetChanged();
                PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.account_pager_title_strip);
                pagerTabStrip.setVisibility(View.VISIBLE);
                int intExtra = getIntent().getIntExtra(AccountsActivity.LANDING_SCREEN, 0);
                ((ViewPager) findViewById(R.id.account_pager)).setCurrentItem(intExtra);
                pagerTabStrip.setTabIndicatorColor(getResources().getColor(mAccountFragments.get(intExtra).getTabColor()));
                break;
            case 400:
                if ("0619".equals(accountsResponse.response.code) || "0618".equals(accountsResponse.response.code)) {
                    if(!isFinishing()) {
                        mLogin.show();
                    }
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

    private void handleVoucherResponse(VoucherResponse voucherResponse) {
        switch (voucherResponse.httpCode) {
            case 200:
                getWoolworthsApplication().getUserManager().setWRewards(voucherResponse);
                WoolworthsApplication.setNumVouchers(voucherResponse.voucherCollection == null || voucherResponse.voucherCollection.vouchers == null ? 0 :voucherResponse.voucherCollection.vouchers.size());

                ListView listView = (ListView)this.findViewById(za.co.wigroup.menudrawerlib.R.id.left_drawer);
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

                mFragmentAdapter.notifyDataSetChanged();
                break;
            case 400:
                if ("0619".equals(voucherResponse.response.code) || "0618".equals(voucherResponse.response.code)) {
                    if (!isFinishing()) {
                        mLogin.show();
                    }
                    break;

                }
            case 502:
                Log.i("Handling 502","Handled a 502 error from the server");
                break;
            default:
                ((TextView) findViewById(R.id.no_internet_message)).setText(voucherResponse.response.desc);
                findViewById(R.id.no_internet).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void setDrawerTitleClosed() {
        setWoolworthsTitle(R.string.my_accounts);
    }

    @Override
    protected void onPause() {
        mLogin.dismiss();
        mGetAccountsProgressDialog.dismiss();
        mGetWRewardsProgressDialog.dismiss();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        try {
            ViewPager viewPager = (ViewPager) findViewById(R.id.account_pager);
            if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem(0);
                return;
            }
        } catch (IllegalStateException e) {
            // ducking the exception since the activity is finishing anyway
        }
        super.onBackPressed();

    }
    public void applyNow(View v){
        Intent i =new Intent(AccountsActivity.this, WebViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("title","Apply Now");
        bundle.putString("link", WoolworthsApplication.getApplyNowLink());
        i.putExtra("Bundle",bundle);
        startActivity(i);

    }

    private WoolworthsApplication getWoolworthsApplication() {
        return (WoolworthsApplication) getApplication();
    }
}
