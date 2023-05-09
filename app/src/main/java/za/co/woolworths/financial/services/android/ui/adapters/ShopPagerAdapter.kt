package za.co.woolworths.financial.services.android.ui.adapters

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import za.co.woolworths.financial.services.android.ui.fragments.shop.StandardDeliveryFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents
import za.co.woolworths.financial.services.android.ui.views.shop.dash.ChangeFulfillmentCollectionStoreFragment
import za.co.woolworths.financial.services.android.ui.views.shop.dash.DashDeliveryAddressFragment
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.ARG_SEND_DELIVERY_DETAILS
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.EXTRA_SEND_DELIVERY_DETAILS_PARAMS

class ShopPagerAdapter(
    fm: FragmentManager,
    tabTitle: MutableList<String>?,
    var listener: OnChildFragmentEvents
) : FragmentStatePagerAdapter(fm) {
    private val mTabTitle: MutableList<String>? = tabTitle
    private val dashFragment = DashDeliveryAddressFragment().apply {
        arguments = bundleOf(
            ARG_SEND_DELIVERY_DETAILS to listener.isSendDeliveryDetails()
        )
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {

            1 -> {
                val collectionFragment = ChangeFulfillmentCollectionStoreFragment()
                collectionFragment.arguments = bundleOf(
                    EXTRA_SEND_DELIVERY_DETAILS_PARAMS to listener.isSendDeliveryDetails()
                )
                collectionFragment
            }

            2 -> dashFragment

            else -> {
                val standardDeliveryFragment = StandardDeliveryFragment()
                standardDeliveryFragment.arguments = bundleOf(
                    EXTRA_SEND_DELIVERY_DETAILS_PARAMS to listener.isSendDeliveryDetails()
                )
                standardDeliveryFragment
            }
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