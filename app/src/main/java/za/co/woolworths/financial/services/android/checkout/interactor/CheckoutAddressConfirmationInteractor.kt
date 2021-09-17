package za.co.woolworths.financial.services.android.checkout.interactor

import androidx.lifecycle.LiveData
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddressConfirmationApiHelper

/**
 * Created by Kunal Uttarwar on 12/08/21.
 */
class CheckoutAddressConfirmationInteractor(private val checkoutAddressConfirmationApiHelper: CheckoutAddressConfirmationApiHelper) {
    fun setSuburb(suburbId: String): LiveData<Any> {
        return checkoutAddressConfirmationApiHelper.setSuburb(suburbId)
    }
}