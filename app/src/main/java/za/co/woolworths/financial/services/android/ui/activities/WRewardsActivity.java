package za.co.woolworths.financial.services.android.ui.activities;


import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.fragments.TierInfoFragment;
import za.co.woolworths.financial.services.android.ui.fragments.VouchersListFragment;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WErrorDialog;

public class WRewardsActivity extends BaseDrawerActivity {

    private static final String TAG = "WRewardsActivity";
    private FragmentPagerAdapter mFragmentAdapter;
    private List<Fragment> mAccountFragments = new ArrayList<>();
    private AlertDialog mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWRewardsTitle();
        setContentView(R.layout.w_rewards_activity);
        mLogin = WErrorDialog.getLoginErrorDialog(this);
        mAccountFragments.add(new TierInfoFragment());
        mAccountFragments.add(new VouchersListFragment());
        ViewPager viewPager = (ViewPager) findViewById(R.id.w_rewards_pager);
        ((PagerTabStrip) findViewById(R.id.w_rewards_pager_title_strip)).setTabIndicatorColor(getResources().getColor(R.color.rewards));
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
                SpannableString spannableString;
                switch (position) {
                    case 0:
                        spannableString = FontHyperTextParser.getSpannable(getString(R.string.tier_info), 1, WRewardsActivity.this);
                        break;
                    default:
                        String wRewards = getWoolworthsApplication().getUserManager().getWRewards();
                        String s = "";
                        if (!wRewards.isEmpty()) {
                            List<Voucher> vouchers = new Gson().fromJson(wRewards, VoucherResponse.class).voucherCollection.vouchers;
                            int size = vouchers == null ? 0 : vouchers.size();
                            if (size > 0) {
                                s = String.format("[%s]", size);
                            }
                        }
                        String activeVouchers = getString(R.string.active_vouchers, s);
                        spannableString = new SpannableString(activeVouchers);
                        spannableString.setSpan(new CalligraphyTypefaceSpan(Typeface.createFromAsset(getAssets(), "fonts/WFutura-Medium.ttf")), 0, activeVouchers.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString.setSpan(new CalligraphyTypefaceSpan(Typeface.createFromAsset(getAssets(), "fonts/WFutura-SemiBold.ttf")), 15, activeVouchers.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.rewards)), 15, activeVouchers.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                }

                return spannableString;
            }
        };
        viewPager.setAdapter(mFragmentAdapter);
        findViewById(R.id.no_internet_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadWRewards();
            }
        });
    }

    private void loadWRewards() {
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

                    WiGroupLogger.e(WRewardsActivity.this, TAG, errorMessage);
                    VoucherResponse voucherResponse = new VoucherResponse();
                    voucherResponse.httpCode = 408;
                    voucherResponse.response = new Response();
                    voucherResponse.response.desc = getString(R.string.err_002);
                    return voucherResponse;
                }

                @Override
                protected void onPreExecute() {
                    findViewById(R.id.w_rewards_loading).setVisibility(View.VISIBLE);
                }

                @Override
                protected void onPostExecute(VoucherResponse voucherResponse) {
                    findViewById(R.id.w_rewards_loading).setVisibility(View.GONE);
                    switch (voucherResponse.httpCode) {
                        case 200:
                            getWoolworthsApplication().getUserManager().setWRewards(voucherResponse);
                            WoolworthsApplication.setNumVouchers(voucherResponse.voucherCollection.vouchers.size());
                            ((TierInfoFragment) mAccountFragments.get(0)).update(voucherResponse);
                            ((VouchersListFragment) mAccountFragments.get(1)).update(voucherResponse);
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
            }.execute();
        } else {
            VoucherResponse voucherResponse = new Gson().fromJson(getWoolworthsApplication().getUserManager().getWRewards(), VoucherResponse.class);
            ((TierInfoFragment) mAccountFragments.get(0)).update(voucherResponse);
            ((VouchersListFragment) mAccountFragments.get(1)).update(voucherResponse);
            mFragmentAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        loadWRewards();
    }

    private WoolworthsApplication getWoolworthsApplication() {
        return (WoolworthsApplication) getApplication();
    }

    @Override
    protected void setDrawerTitleClosed() {
        setWRewardsTitle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.w_rewards_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.w_rewards_menu) {
            Intent i =new Intent(WRewardsActivity.this, WebViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("title","HOW IT WORKS");
            bundle.putString("link", WoolworthsApplication.getWrewardsLink());
            i.putExtra("Bundle",bundle);
            startActivity(i);
            //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.how_it_works_url)));
            //startActivity(browserIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setWRewardsTitle() {
        getActionBar().setTitle(FontHyperTextParser.getSpannable(getString(R.string.drawer_rewards), 1, this));
    }

    @Override
    protected void onPause() {
        mLogin.dismiss();
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ViewPager viewPager = (ViewPager) findViewById(R.id.w_rewards_pager);

            if(viewPager.getCurrentItem() == 1)
                viewPager.setCurrentItem(0, true);
            else
                return super.onKeyDown(keyCode, event);

            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
