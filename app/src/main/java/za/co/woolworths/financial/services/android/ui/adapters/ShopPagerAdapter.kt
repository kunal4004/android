package za.co.woolworths.financial.services.android.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import za.co.woolworths.financial.services.android.ui.fragments.shop.DepartmentsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.MyListsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.MyOrdersFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents

class ShopPagerAdapter(fm: FragmentManager, tabTitle: MutableList<String>?, var listener: OnChildFragmentEvents) : FragmentStatePagerAdapter(fm) {
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