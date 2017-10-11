package za.co.woolworths.financial.services.android.ui.activities;

import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.adapters.CLIIncreaseLimitInfoPagerAdapter;
import za.co.woolworths.financial.services.android.util.Utils;


public class CLIIncreaseLimitInfoActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

	private Toolbar mToolbar;
	private LinearLayout pager_indicator;
	private int dotsCount;
	private ImageView[] dots;
	private ViewPager pager;
	private CLIIncreaseLimitInfoPagerAdapter cliInfoAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cli_increse_limit_info_activity);
		Utils.updateStatusBarBackground(this);
		mToolbar = (Toolbar) findViewById(R.id.mToolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(null);
		pager = (ViewPager) findViewById(R.id.cliInfoPager);
		pager_indicator = (LinearLayout) findViewById(R.id.viewPagerCountDots);
		cliInfoAdapter = new CLIIncreaseLimitInfoPagerAdapter(CLIIncreaseLimitInfoActivity.this);
		pager.setAdapter(cliInfoAdapter);
		pager.addOnPageChangeListener(this);
		setUiPageViewController();
	}

	private void setUiPageViewController() {
		try {
			pager_indicator.removeAllViews();
			dotsCount = cliInfoAdapter.getCount();
			dots = new ImageView[dotsCount];
			for (int i = 0; i < dotsCount; i++) {
				dots[i] = new ImageView(CLIIncreaseLimitInfoActivity.this);
				dots[i].setImageDrawable(ContextCompat.getDrawable(CLIIncreaseLimitInfoActivity.this, R.drawable.page_control_inactive));
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT
				);
				params.setMargins(10, 0, 10, 0);
				pager_indicator.addView(dots[i], params);
			}
			dots[0].setImageDrawable(ContextCompat.getDrawable(CLIIncreaseLimitInfoActivity.this, R.drawable.page_control_active));
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
			dots[i].setImageDrawable(ContextCompat.getDrawable(CLIIncreaseLimitInfoActivity.this, R.drawable.page_control_inactive));
		}
		dots[position].setImageDrawable(ContextCompat.getDrawable(CLIIncreaseLimitInfoActivity.this, R.drawable.page_control_active));
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
}
