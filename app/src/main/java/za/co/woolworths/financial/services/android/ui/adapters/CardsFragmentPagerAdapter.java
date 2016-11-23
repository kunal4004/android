package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import za.co.woolworths.financial.services.android.ui.fragments.WCreditCardFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WPersonalLoanFragment;
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardFragment;

/**
 * Created by W7099877 on 22/11/2016.
 */

public class CardsFragmentPagerAdapter extends FragmentPagerAdapter {

    public CardsFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                WCreditCardFragment fragment1 = new WCreditCardFragment();
                return fragment1;
            case 1:
                WStoreCardFragment fragment2 = new WStoreCardFragment();
                return fragment2;
            case 2:
                WPersonalLoanFragment fragment3 = new WPersonalLoanFragment();
                return fragment3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }


}
