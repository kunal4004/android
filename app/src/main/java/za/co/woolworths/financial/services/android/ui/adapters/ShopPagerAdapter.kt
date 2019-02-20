package za.co.woolworths.financial.services.android.ui.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import za.co.woolworths.financial.services.android.ui.fragments.shop.DepartmentsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.MyListsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.MyOrdersFragment

class ShopPagerAdapter(fm: FragmentManager, tabTitle: MutableList<String>?) : FragmentPagerAdapter(fm) {
    private val mTabTitle = tabTitle
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> DepartmentsFragment()
            1 -> MyListsFragment()
            else -> {
                return MyOrdersFragment()
            }
        }
    }

    override fun getCount(): Int {
        return mTabTitle!!.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> mTabTitle!![0]
            1 -> mTabTitle!![1]
            else -> {
                return mTabTitle!![2]
            }
        }
    }
}