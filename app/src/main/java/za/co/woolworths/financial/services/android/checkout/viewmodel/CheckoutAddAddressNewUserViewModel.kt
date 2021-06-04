package za.co.woolworths.financial.services.android.checkout.viewmodel

import androidx.lifecycle.ViewModel
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.util.DeliveryType

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserViewModel(checkoutAddAddressNewUserInteractor: CheckoutAddAddressNewUserInteractor, checkoutAddAddressNewUserApiHelper: CheckoutAddAddressNewUserApiHelper): ViewModel() {

    fun initGetSuburbs(provinceId: String, deliveryType: DeliveryType) = null
}