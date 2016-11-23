package za.co.woolworths.financial.services.android.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Voucher;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.fragments.VoucherItemFragment;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;

public class WRewardDetailActivity extends FragmentActivity
{

	public static final String        VOUCHER_ID = "voucher_id";
	public static final String        DATA       = "data";
	private             List<Voucher> mVouchers  = new ArrayList<Voucher>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.w_reward_detail_activity);
		mVouchers = (new Gson().fromJson(getIntent().getStringExtra(DATA), VoucherResponse.class)).voucherCollection.vouchers;
		ViewPager viewPager = (ViewPager) findViewById(R.id.w_rewards_detail_activity_pager);
		viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager())
		{
			@Override
			public Fragment getItem(int i)
			{
				return new VoucherItemFragment(mVouchers.get(i));
			}

			@Override
			public int getCount()
			{
				return mVouchers.size();
			}


		});
		//viewPager.setCurrentItem(getIntent().getExtras().getInt(VOUCHER_ID));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setWRewardsTitle();

		// Disable left and right swiping. Not sure how permanent this change will be, so leaving the ViewPager in.
		// If this becomes permanent, the fragment should be removed. Layout and logic should be done on this activity instead.
		Voucher currentVoucher = mVouchers.get(getIntent().getExtras().getInt(VOUCHER_ID));
		mVouchers.clear();
		mVouchers.add(currentVoucher);
		viewPager.getAdapter().notifyDataSetChanged();

		viewPager.setOverScrollMode(ViewPager.OVER_SCROLL_IF_CONTENT_SCROLLS); // Hide the overscroll animation
	}

	private void setWRewardsTitle()
	{
		getActionBar().setTitle(FontHyperTextParser.getSpannable(getString(R.string.my_vouchers), 1, this));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
