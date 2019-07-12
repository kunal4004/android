package za.co.woolworths.financial.services.android.ui.activities.cli;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.ui.adapters.CLIIncreaseLimitInfoPagerAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.controller.SelectedItemCallback;

public class FindOutMoreActivity extends AppCompatActivity implements SelectedItemCallback, ViewPager.OnPageChangeListener, View.OnClickListener {

	private LinearLayout pager_indicator;
	private int dotsCount;
	private ImageView[] dots;
	private CLIIncreaseLimitInfoPagerAdapter cliInfoAdapter;
	public WButton btnIncreaseMyLimit;
	private String mOfferActivePayload;
	private boolean mOfferActive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cli_find_out_more_carousel_activity);
		Utils.updateStatusBarBackground(this);
		Bundle mBundle = getIntent().getExtras();
		if (mBundle != null) {
			mOfferActivePayload = mBundle.getString("OFFER_ACTIVE_PAYLOAD");
			mOfferActive = mBundle.getBoolean("OFFER_IS_ACTIVE");
		}

		Toolbar mToolbar = (Toolbar) findViewById(R.id.mToolbar);
		setSupportActionBar(mToolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(null);
		}
		ViewPager pager = (ViewPager) findViewById(R.id.cliInfoPager);
		btnIncreaseMyLimit = (WButton) findViewById(R.id.btnIncreaseMyLimit);
		pager_indicator = (LinearLayout) findViewById(R.id.viewPagerCountDots);
		cliInfoAdapter = new CLIIncreaseLimitInfoPagerAdapter(FindOutMoreActivity.this, this);
		pager.setAdapter(cliInfoAdapter);
		pager.addOnPageChangeListener(this);
		btnIncreaseMyLimit.setOnClickListener(this);
		setUiPageViewController();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.CLI_WALKTHROUGH);
	}

	private void setUiPageViewController() {
		try {
			pager_indicator.removeAllViews();
			dotsCount = cliInfoAdapter.getCount();
			dots = new ImageView[dotsCount];
			for (int i = 0; i < dotsCount; i++) {
				dots[i] = new ImageView(FindOutMoreActivity.this);
				dots[i].setImageDrawable(ContextCompat.getDrawable(FindOutMoreActivity.this, R.drawable.unselected_dot));
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
				);
				params.setMargins(10, 0, 10, 0);
				pager_indicator.addView(dots[i], params);
			}
			dots[0].setImageDrawable(ContextCompat.getDrawable(FindOutMoreActivity.this, R.drawable.selected_dot));
		} catch (Exception ignored) {
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_item, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		for (int i = 0; i < dotsCount; i++) {
			dots[i].setImageDrawable(ContextCompat.getDrawable(FindOutMoreActivity.this, R.drawable.unselected_dot));
		}
		dots[position].setImageDrawable(ContextCompat.getDrawable(FindOutMoreActivity.this, R.drawable.selected_dot));
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onBackPressed() {
		goBack();
	}

	private void goBack() {
		this.finish();
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_search:
				goBack();
				break;
		}
		return false;
	}

	@Override
	public void onItemClick(View v, int position) {
		/*Intent openYoutubeVideo = new Intent(this, YoutubePlayerActivity.class);
		startActivity(openYoutubeVideo);
		overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);*/
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnIncreaseMyLimit:
				Intent openFindOutMore = new Intent(FindOutMoreActivity.this, CLIPhase2Activity.class);
				openFindOutMore.putExtra("OFFER_ACTIVE_PAYLOAD", mOfferActivePayload);
				openFindOutMore.putExtra("OFFER_IS_ACTIVE", mOfferActive);
				startActivity(openFindOutMore);
				overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				FindOutMoreActivity.this.finish();
				break;
			default:
				break;
		}
	}
}
