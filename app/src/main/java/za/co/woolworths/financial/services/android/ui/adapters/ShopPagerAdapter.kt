package za.co.woolworths.financial.services.android.ui.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import za.co.woolworths.financial.services.android.ui.fragments.shop.DepartmentsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.MyListsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.MyOrdersFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents

class ShopPagerAdapter(fm: FragmentManager, tabTitle: MutableList<String>?, var listener: OnChildFragmentEvents) : FragmentPagerAdapter(fm) {
    private val mTabTitle: MutableList<String>? = tabTitle
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> DepartmentsFragment()
            1 -> MyListsFragment()
            else -> MyOrdersFragment.getInstance(listener)
        }
    }

    override fun getCount(): Int {
        return mTabTitle?.size ?: 0
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> mTabTitle?.get(0) ?: ""
            1 -> mTabTitle?.get(1) ?: ""
            else -> mTabTitle?.get(2) ?: ""

        }
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}