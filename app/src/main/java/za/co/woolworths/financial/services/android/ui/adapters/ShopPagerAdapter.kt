package za.co.woolworths.financial.services.android.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.ui.views.shop.dash.ChangeFullfilmentCollectionStoreFragment
import za.co.woolworths.financial.services.android.ui.views.shop.dash.DashDeliveryAddressFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.StandardDeliveryFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents

class ShopPagerAdapter(fm: FragmentManager, tabTitle: MutableList<String>?, var listener: OnChildFragmentEvents) : FragmentStatePagerAdapter(fm) {
    private val mTabTitle: MutableList<String>? = tabTitle
    private var validatePlace: ValidatePlace? = null

    fun setValidateResponse(validatePlace: ValidatePlace?) {
       this.validatePlace = validatePlace
    }
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> StandardDeliveryFragment()
            1 -> ChangeFullfilmentCollectionStoreFragment(validatePlace)
            else -> DashDeliveryAddressFragment()
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