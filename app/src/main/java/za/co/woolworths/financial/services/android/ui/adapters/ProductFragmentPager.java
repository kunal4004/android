package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class ProductFragmentPager extends FragmentPagerAdapter {

    /**
     * Contains all the fragments.
     */
    private List<Fragment> fragments = new ArrayList<>();

    /**
     * Contains all the tab titles.
     */
    private List<String> tabTitles = new ArrayList<>();

    /**
     * Creates a new PagerAdapter instance.
     *
     * @param fragmentManager The FragmentManager.
     */
    public ProductFragmentPager(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }

    /**
     * Adds the fragment to the list, also adds the fragment's tab title.
     *
     * @param fragment New instance of the Fragment to be associated with this tab.
     * @param tabTitle A String containing the tab title for this Fragment.
     */
    public void addFragment(Fragment fragment, String tabTitle) {
        fragments.add(fragment);
        tabTitles.add(tabTitle);
    }

}
