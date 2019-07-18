package za.co.woolworths.financial.services.android.ui.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by W7099877 on 22/11/2016.
 */

public class CardsFragmentPagerAdapter extends FragmentPagerAdapter {

	private final List<Fragment> mFragmentList = new ArrayList<>();

	public CardsFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return mFragmentList.get(position);
	}

	@Override
	public int getCount() {
		return mFragmentList.size();
	}

	public void addFrag(Fragment fragment) {
		mFragmentList.add(fragment);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		return super.instantiateItem(container, position);
	}
}
