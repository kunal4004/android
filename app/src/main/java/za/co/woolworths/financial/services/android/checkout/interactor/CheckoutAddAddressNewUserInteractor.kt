package za.co.woolworths.financial.services.android.checkout.interactor

import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.util.DeliveryType

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserInteractor(private val checkoutAddAddressNewUserApiHelper: CheckoutAddAddressNewUserApiHelper) {

    fun getSuburbs(provinceId: String) = checkoutAddAddressNewUserApiHelper.getSuburbs(provinceId)
}