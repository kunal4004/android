package za.co.woolworths.financial.services.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WOnboardingOnFragmentInteractionListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.fragments.CLIFirstStepFragment;
import za.co.woolworths.financial.services.android.ui.fragments.CLIFourthStepFragment;
import za.co.woolworths.financial.services.android.ui.fragments.CLISecondStepFragment;
import za.co.woolworths.financial.services.android.ui.fragments.CLIThirdStepFragment;
import za.co.woolworths.financial.services.android.ui.views.WFragmentViewPager;
import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.Utils;


public class CLIStepIndicatorActivity extends BaseActivity implements WOnboardingOnFragmentInteractionListener,
        CLIFirstStepFragment.StepNavigatorCallback {

    private OnFragmentRefresh onFragmentRefresh;
    private WoolworthsApplication mWoolworthApplication;
    private ImageView mImgStepIcon;
    private Toolbar mToolbar;

    public interface OnFragmentRefresh {
        void refreshFragment();
    }

    private WFragmentViewPager mViewPStepIndicator;
    public AppBarLayout mAppBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cli_step);
        Utils.updateStatusBarBackground(CLIStepIndicatorActivity.this);
        mWoolworthApplication = (WoolworthsApplication) getApplication();
        initViews();
        setActionBar();
        setCLIContent();
        registerReceiver(moveToPageBroadcastReceiver, new IntentFilter("moveToPageBroadcastReceiver"));
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mViewPStepIndicator = (WFragmentViewPager) findViewById(R.id.mViewPStepIndicator);
        mImgStepIcon = (ImageView) findViewById(R.id.imgStepIcon);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
    }

    private void setActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayUseLogoEnabled(false);
        mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeAsUpIndicator(R.drawable.back24);
    }

    private void setCLIContent() {
        // Create the adapter that will return a fragment for each of the three
        mViewPStepIndicator.setPagingEnabled(false);
        mViewPStepIndicator.setAdapter(mPagerFragmentAdapter);


        int limit = mPagerFragmentAdapter.getCount();
        // Set the number of pages that should be retained to either
        // side of the current page in the view hierarchy in an idle state.
        mViewPStepIndicator.setOffscreenPageLimit(limit);


        //mStepIndicator.setupWithViewPager(mViewPStepIndicator);
        mViewPStepIndicator.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position) {
                    case 2:
                        refresh();
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 3) {
                    showCloseIconToolbar(true);
                } else {
                    showCloseIconToolbar(false);
                }

                switch (position) {
                    case 0:
                        mImgStepIcon.setImageResource(R.drawable.clinumbersprogress_1);
                        break;

                    case 1:
                        mImgStepIcon.setImageResource(R.drawable.clinumbersprogress_2);
                        break;

                    case 2:
                        mImgStepIcon.setImageResource(R.drawable.clinumbersprogress_3);
                        refresh();
                        break;

                    case 3:
                        mImgStepIcon.setImageResource(R.drawable.clinumbersprogress_4);
                        break;

                    default:
                        mImgStepIcon.setImageResource(R.drawable.clinumbersprogress_1);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    public FragmentPagerAdapter mPagerFragmentAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new CLIFirstStepFragment();
                    break;
                case 1:
                    fragment = new CLISecondStepFragment();
                    break;
                case 2:
                    fragment = new CLIThirdStepFragment();
                    break;
                case 3:
                    fragment = new CLIFourthStepFragment();
                    break;
                default:
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backActionPressed();
                return true;
        }
        return false;
    }

    public void showCloseIconToolbar(boolean showToolbarIcon) {
        if (showToolbarIcon)
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.close_24);
        else
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.back24);
    }

    @Override
    public void openNextFragment(int index) {
        moveToPage(index);
    }


    public void setOnFragmentRefresh(OnFragmentRefresh onFragmentRefresh) {
        this.onFragmentRefresh = onFragmentRefresh;
    }

    public void moveToPage(int position) {
        mViewPStepIndicator.setCurrentItem(position);
    }

    public void refresh() {
        onFragmentRefresh.refreshFragment();
    }

    @Override
    public void onBackPressed() {
        backActionPressed();
    }

    private void backActionPressed() {
        int currentPosition = mViewPStepIndicator.getCurrentItem();
        switch (currentPosition) {
            case 0:
                Intent openCLIStepIndicatorActivity = new Intent(CLIStepIndicatorActivity.this, CLISupplyInfoActivity.class);
                startActivity(openCLIStepIndicatorActivity);
                finish();
                overridePendingTransition(0, 0);
                break;
            case 1:
                moveToPage(0);
                break;
            case 2:
                if (mWoolworthApplication.isOther()) {
                    mViewPStepIndicator.setCurrentItem(0);
                } else {
                    moveToPage(1);
                }
                break;
            case 3:
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(moveToPageBroadcastReceiver);
    }

    BroadcastReceiver moveToPageBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            moveToPage(3);
        }
    };
}
