package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.ui.fragments.WCreditCardFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WPersonalLoanFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardFragment;
import za.co.woolworths.financial.services.android.ui.views.WCustomPager;

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
}
