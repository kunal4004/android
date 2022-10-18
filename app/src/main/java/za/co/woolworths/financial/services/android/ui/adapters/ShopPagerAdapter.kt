package za.co.woolworths.financial.services.android.ui.adapters

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.shop.dash.ChangeFullfilmentCollectionStoreFragment
import za.co.woolworths.financial.services.android.ui.views.shop.dash.DashDeliveryAddressFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.StandardDeliveryFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.OnChildFragmentEvents
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.ARG_SEND_DELIVERY_DETAILS
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.ARG_VALIDATE_PLACE

class ShopPagerAdapter(
    fm: FragmentManager,
    tabTitle: MutableList<String>?,
    var listener: OnChildFragmentEvents
) : FragmentStatePagerAdapter(fm) {
    private val mTabTitle: MutableList<String>? = tabTitle
    private var validatePlace: ValidatePlace? = null

    fun setValidateResponse(validatePlace: ValidatePlace?) {
        this.validatePlace = validatePlace
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                val standardDeliveryFragment = StandardDeliveryFragment()
                standardDeliveryFragment.arguments = bundleOf(
                    ARG_SEND_DELIVERY_DETAILS to listener.isSendDeliveryDetails()
                )
                standardDeliveryFragment
            }
            1 -> {
                val collectionFragment = ChangeFullfilmentCollectionStoreFragment()
                collectionFragment.arguments = bundleOf(
                    ARG_VALIDATE_PLACE to validatePlace,
                    ARG_SEND_DELIVERY_DETAILS to listener.isSendDeliveryDetails()
                )
                collectionFragment
            }
            else -> {
                val dashFragment = DashDeliveryAddressFragment()
                dashFragment.arguments = bundleOf(
                    ARG_SEND_DELIVERY_DETAILS to listener.isSendDeliveryDetails()
                )
                dashFragment
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