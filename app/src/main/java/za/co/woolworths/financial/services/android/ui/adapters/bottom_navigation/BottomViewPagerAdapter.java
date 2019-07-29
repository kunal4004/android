package za.co.woolworths.financial.services.android.ui.adapters.bottom_navigation;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.List;

public class BottomViewPagerAdapter extends FragmentPagerAdapter {
	private List<Fragment> data;
	SparseArray<Fragment> registeredFragments = new SparseArray<>();

	public BottomViewPagerAdapter(FragmentManager fm, List<Fragment> data) {
		super(fm);
		this.data = data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Fragment getItem(int position) {
		return data.get(position);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = (Fragment) super.instantiateItem(container, position);
		registeredFragments.put(position, fragment);
		return fragment;
	}

	/**
	 * Remove the saved reference from our Map on the Fragment destroy
	 *
	 * @param container
	 * @param position
	 * @param object
	 */
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		registeredFragments.remove(position);
		super.destroyItem(container, position, object);
	}

	/**
	 * Get the Fragment by position
	 *
	 * @param position tab position of the fragment
	 * @return
	 */
	public Fragment getRegisteredFragment(int position) {
		return registeredFragments.get(position);
	}
}