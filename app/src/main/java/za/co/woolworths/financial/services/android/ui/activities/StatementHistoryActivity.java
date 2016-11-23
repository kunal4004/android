package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import za.co.wigroup.androidutils.views.TabIndicator;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.fragments.MonthFragment;
import za.co.woolworths.financial.services.android.ui.fragments.YearStatement;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;


public class StatementHistoryActivity extends FragmentActivity
{

	public static final String DATA = "data";
	private Fragment[]           mFragments;
	private FragmentPagerAdapter mFragmentPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statement_history_view);
		setTitle(FontHyperTextParser.getSpannable(getString(R.string.statement), 1, this));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		findViewById(R.id.statement_history_how_to_save).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent i =new Intent(StatementHistoryActivity.this, WebViewActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("title","HOW TO SAVE");
				bundle.putString("link", WoolworthsApplication.getHowToSaveLink());
				i.putExtra("Bundle",bundle);
				startActivity(i);

			}
		});
		Gson            gson            = new Gson();
		VoucherResponse voucherResponse = gson.fromJson(getIntent().getStringExtra(DATA), VoucherResponse.class);
		mFragments = new Fragment[voucherResponse.tierHistoryList.size() + 1];
		mFragments[0] = new YearStatement();
		Bundle args = new Bundle();
		args.putString(YearStatement.DATA, gson.toJson(voucherResponse.tierInfo));
		mFragments[0].setArguments(args);
		for (int i = 0; i < voucherResponse.tierHistoryList.size(); i++)
		{
			mFragments[i + 1] = new MonthFragment();
			Bundle args1 = new Bundle();
			args1.putString(MonthFragment.DATA, gson.toJson(voucherResponse.tierHistoryList.get(i)));
			mFragments[i + 1].setArguments(args1);
		}
		((TabIndicator) findViewById(R.id.statement_history_indicator)).setIndicatorCount(mFragments.length);
		mFragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
		{
			@Override
			public int getCount()
			{
				return mFragments.length;
			}

			@Override
			public Fragment getItem(int position)
			{
				return mFragments[position];
			}
		};
		ViewPager viewPager = (ViewPager) findViewById(R.id.statement_history_view_pager);
		viewPager.setAdapter(mFragmentPagerAdapter);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			public void onPageScrolled(int i, float v, int i2)
			{
				TabIndicator tabIndicator = (TabIndicator) findViewById(R.id.statement_history_indicator);
				tabIndicator.setCurrentIndicatorPosition(i);
				tabIndicator.setOffset(v);
			}

			public void onPageSelected(int i)
			{
			}

			public void onPageScrollStateChanged(int i)
			{
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.w_rewards_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				break;
			case R.id.w_rewards_menu:
				startActivity(new Intent(this, WRewardInfoActivity.class));
		}
		return super.onOptionsItemSelected(item);
	}
}
