package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.JWTDecodedModel;
import za.co.woolworths.financial.services.android.models.WOnboardingOnFragmentInteractionListener;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.ui.fragments.WOnboardingFourFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WOnboardingOneFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WOnboardingThreeFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WOnboardingTwoFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.JWTHelper;
import za.co.woolworths.financial.services.android.util.ScreenManager;

public class WOnboardingActivity extends FragmentActivity implements WOnboardingOnFragmentInteractionListener {

    private static final String TAG = "WOnboardingActivity";

    private ViewPager mPager;
    private Button btnLogin;
    private Button btnRegister;
    private WTextView txtSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wonboarding);

        //Typeface.createFromAsset(getAssets(), "fonts/WFutura-Medium.ttf")

        this.mPager = (ViewPager) findViewById(R.id.activity_wonboarding_viewpager);
        mPager.setAdapter(this.mPagerFragmentAdapter);
        mPager.setOnPageChangeListener(this.mPageChangeListener);

        this.txtSkip = (WTextView) findViewById(R.id.txtSkip);
        this.btnLogin = (Button) findViewById(R.id.btnOnboardingLogin);
        this.btnRegister = (Button) findViewById(R.id.btnOnboardingRegister);

        this.txtSkip.setOnClickListener(this.txtSkip_onClick);
        this.btnLogin.setOnClickListener(this.btnSignin_onClick);
        this.btnRegister.setOnClickListener(this.btnRegister_onClick);

        Typeface buttonTypeface = Typeface.createFromAsset(getAssets(), "fonts/WFutura-SemiBold.ttf");
        this.btnLogin.setTypeface(buttonTypeface, 12);
        this.btnRegister.setTypeface(buttonTypeface, 12);
    }

    private View.OnClickListener txtSkip_onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            WOnboardingActivity.this.navigateToMain();
        }
    };

    private View.OnClickListener btnSignin_onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ScreenManager.presentSSOSignin(WOnboardingActivity.this);
        }
    };

    private View.OnClickListener btnRegister_onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ScreenManager.presentSSORegister(WOnboardingActivity.this);
        }
    };

    private FragmentPagerAdapter mPagerFragmentAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            switch (position) {
                case 0:
                    fragment = new WOnboardingOneFragment();
                    break;
                case 1:
                    fragment = new WOnboardingTwoFragment();
                    break;
                case 2:
                    fragment = new WOnboardingThreeFragment();
                    break;
                case 3:
                    fragment = new WOnboardingFourFragment();
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

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            final Drawable pageControlActive = getResources().getDrawable(R.drawable.page_indicator_selected);
            final Drawable pageControlInactive = getResources().getDrawable(R.drawable.page_indicator_default);

            switch (position) {
                case 0:
                    ((ImageView)findViewById(R.id.dotimg0)).setImageDrawable(pageControlActive);
                    ((ImageView)findViewById(R.id.dotimg1)).setImageDrawable(pageControlInactive);
                    ((ImageView)findViewById(R.id.dotimg2)).setImageDrawable(pageControlInactive);
                    ((ImageView)findViewById(R.id.dotimg3)).setImageDrawable(pageControlInactive);
                    break;
                case 1:
                    ((ImageView)findViewById(R.id.dotimg0)).setImageDrawable(pageControlInactive);
                    ((ImageView)findViewById(R.id.dotimg1)).setImageDrawable(pageControlActive);
                    ((ImageView)findViewById(R.id.dotimg2)).setImageDrawable(pageControlInactive);
                    ((ImageView)findViewById(R.id.dotimg3)).setImageDrawable(pageControlInactive);
                    break;
                case 2:
                    ((ImageView)findViewById(R.id.dotimg0)).setImageDrawable(pageControlInactive);
                    ((ImageView)findViewById(R.id.dotimg1)).setImageDrawable(pageControlInactive);
                    ((ImageView)findViewById(R.id.dotimg2)).setImageDrawable(pageControlActive);
                    ((ImageView)findViewById(R.id.dotimg3)).setImageDrawable(pageControlInactive);
                    break;
                case 3:
                    ((ImageView)findViewById(R.id.dotimg0)).setImageDrawable(pageControlInactive);
                    ((ImageView)findViewById(R.id.dotimg1)).setImageDrawable(pageControlInactive);
                    ((ImageView)findViewById(R.id.dotimg2)).setImageDrawable(pageControlInactive);
                    ((ImageView)findViewById(R.id.dotimg3)).setImageDrawable(pageControlActive);
                    break;
                default:break;
            }
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()){
            //Save JWT
            SessionDao sessionDao = new SessionDao(WOnboardingActivity.this);
            sessionDao.key = SessionDao.KEY.USER_TOKEN;
            sessionDao.value = data.getStringExtra(SSOActivity.TAG_JWT);
            try {
                sessionDao.save();
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }

            this.navigateToMain();
        }
    }

    private void navigateToMain(){
        startActivity(new Intent(WOnboardingActivity.this, WOneAppBaseActivity.class));
        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d(WOnboardingActivity.TAG, "onFragmentInteraction");
    }
}
