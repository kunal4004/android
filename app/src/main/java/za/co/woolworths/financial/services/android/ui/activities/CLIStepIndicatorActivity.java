package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WOnboardingOnFragmentInteractionListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.fragments.CLIFirstStepFragment;
import za.co.woolworths.financial.services.android.ui.fragments.CLIFourthStepFragment;
import za.co.woolworths.financial.services.android.ui.fragments.CLISecondStepFragment;
import za.co.woolworths.financial.services.android.ui.fragments.CLIThirdStepFragment;
import za.co.woolworths.financial.services.android.ui.views.StepIndicator;
import za.co.woolworths.financial.services.android.ui.views.WFragmentViewPager;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity.mToolbar;

/**
 * Created by dimitrij on 2016/12/20.
 */

public class CLIStepIndicatorActivity extends AppCompatActivity implements WOnboardingOnFragmentInteractionListener,
        CLIFirstStepFragment.StepNavigatorCallback{

    private OnFragmentRefresh onFragmentRefresh;
    private WoolworthsApplication mWoolworthApplication;

    public interface OnFragmentRefresh{
        void refreshFragment();
    }

    private StepIndicator mStepIndicator;
    private WFragmentViewPager mViewPStepIndicator;
    public static AppBarLayout mAppBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cli_step);
        Utils.updateStatusBarBackground(CLIStepIndicatorActivity.this);
        mWoolworthApplication = (WoolworthsApplication)getApplication();
        initViews();
        setActionBar();
        setCLIContent();
    }


    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mStepIndicator = (StepIndicator) findViewById(R.id.step_indicator);
        mStepIndicator.setDefaultView(1);
        mViewPStepIndicator = (WFragmentViewPager) findViewById(R.id.mViewPStepIndicator);
        mAppBarLayout=(AppBarLayout) findViewById(R.id.appbar);
    }

    private void setActionBar(){
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayUseLogoEnabled(false);
        mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeAsUpIndicator(R.drawable.back24);
    }

    private void setCLIContent(){
        // Create the adapter that will return a fragment for each of the three
        mViewPStepIndicator.setPagingEnabled(false);
        mViewPStepIndicator.setAdapter(mPagerFragmentAdapter);
        mStepIndicator.setupWithViewPager(mViewPStepIndicator);
        mViewPStepIndicator.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position==2){
                    refresh();
                }
            }

            @Override
            public void onPageSelected(int position) {
                if(position==3){
                    showCloseIconToolbar(true);
                }else {
                    showCloseIconToolbar(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {}

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
                default:break;
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
                return  true;
        }
        return false;
    }

    public void showCloseIconToolbar(boolean showToolbarIcon) {
        if(showToolbarIcon)
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

    public void moveToPage(int position){
        mStepIndicator.setCurrentStepPosition(position);
        mViewPStepIndicator.setCurrentItem(position);
    }

    public void refresh(){
        onFragmentRefresh.refreshFragment();
    }

    @Override
    public void onBackPressed() {
       backActionPressed();
    }

    private void backActionPressed(){
        int currentPosition = mViewPStepIndicator.getCurrentItem();
        switch (currentPosition){
            case 0:
                Intent openCLIStepIndicatorActivity = new Intent(CLIStepIndicatorActivity.this,CLISupplyInfoActivity.class);
                startActivity(openCLIStepIndicatorActivity);
                finish();
                overridePendingTransition(0,0);
                break;
            case 1:
                moveToPage(0);
                break;
            case 2:
                if (mWoolworthApplication.isOther()){
                    mViewPStepIndicator.setCurrentItem(0);
                    mStepIndicator.setOtherState();
                }else {
                    moveToPage(1);
                }
                break;
            case 3:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
    }
}
