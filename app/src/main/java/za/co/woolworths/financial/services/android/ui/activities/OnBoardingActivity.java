package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.adapters.OnBoardingViewPagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

public class OnBoardingActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {
	public static final String TAG = "OnBoardingActivity";
	private ViewPager pager;
	private OnBoardingViewPagerAdapter adapter;
	private LinearLayout pager_indicator;
	private int dotsCount;
	private ImageView[] dots;
	private RelativeLayout footerLayout;
	private WButton getStarted;
	private WTextView skip;
	private WTextView next;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(OnBoardingActivity.this, R.color.drawer_color,true);
		setContentView(R.layout.on_boarding_activity);
		pager = (ViewPager) findViewById(R.id.onBoardingPager);
		pager_indicator = (LinearLayout) findViewById(R.id.viewPagerCountDots);
		footerLayout = (RelativeLayout) findViewById(R.id.footerLayout);
		getStarted = (WButton) findViewById(R.id.getStarted);
		skip = (WTextView) findViewById(R.id.skip);
		next = (WTextView) findViewById(R.id.next);
		getStarted.setOnClickListener(this);
		skip.setOnClickListener(this);
		next.setOnClickListener(this);
		adapter = new OnBoardingViewPagerAdapter(OnBoardingActivity.this);
		pager.setAdapter(adapter);
		pager.addOnPageChangeListener(this);
		setUiPageViewController();
		setupForOneTimeVideoOnSplashScreen();
		saveApplicationVersion();
	}

	private void setUiPageViewController() {
		try {
			pager_indicator.removeAllViews();
			dotsCount = adapter.getCount();
			dots = new ImageView[dotsCount];
			for (int i = 0; i < dotsCount; i++) {
				dots[i] = new ImageView(OnBoardingActivity.this);
				dots[i].setImageDrawable(ContextCompat.getDrawable(OnBoardingActivity.this, R.drawable.page_control_inactive));
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
				);
				params.setMargins(10, 0, 10, 0);
				pager_indicator.addView(dots[i], params);
			}
			dots[0].setImageDrawable(ContextCompat.getDrawable(OnBoardingActivity.this, R.drawable.page_control_active));
		} catch (Exception ignored) {
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		for (int i = 0; i < dotsCount; i++) {
			dots[i].setImageDrawable(ContextCompat.getDrawable(OnBoardingActivity.this, R.drawable.page_control_inactive));
		}
		dots[position].setImageDrawable(ContextCompat.getDrawable(OnBoardingActivity.this, R.drawable.page_control_active));
		footerLayout.setVisibility(position == 3 ? View.GONE : View.VISIBLE);
		getStarted.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
	}


	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.skip:
				navigateToMain();
				break;
			case R.id.next:
				pager.setCurrentItem(getItem(+1), true);
				break;
			case R.id.getStarted:
				navigateToMain();
				break;
			default:
				break;
		}
	}

	private int getItem(int i) {
		return pager.getCurrentItem() + i;
	}

	private void navigateToMain() {
		try {
			Utils.sessionDaoSave(OnBoardingActivity.this, SessionDao.KEY.ON_BOARDING_SCREEN, "1");
			startActivityForResult(new Intent(OnBoardingActivity.this, BottomNavigationActivity.class)
					, 0);
			finish();
			overridePendingTransition(R.anim.stay,R.anim.fade_out);
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
		}

	}

	private void setupForOneTimeVideoOnSplashScreen() {
		try {
			Utils.sessionDaoSave(OnBoardingActivity.this, SessionDao.KEY.SPLASH_VIDEO, "1");
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
		}
	}

	private void saveApplicationVersion() {
		try {
			String appLatestVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			Utils.sessionDaoSave(OnBoardingActivity.this, SessionDao.KEY.APP_VERSION, appLatestVersion);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}
