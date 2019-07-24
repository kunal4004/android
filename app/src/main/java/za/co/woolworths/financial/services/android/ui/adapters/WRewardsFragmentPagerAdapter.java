package za.co.woolworths.financial.services.android.ui.adapters;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by W7099877 on 11/01/2017.
 */

public class WRewardsFragmentPagerAdapter extends FragmentPagerAdapter {


    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    Bundle bundle;
    public WRewardsFragmentPagerAdapter(FragmentManager fm, Bundle bundle) {
        super(fm);
        this.bundle=bundle;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment=mFragmentList.get(position);
        fragment.setArguments(this.bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }
    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}
