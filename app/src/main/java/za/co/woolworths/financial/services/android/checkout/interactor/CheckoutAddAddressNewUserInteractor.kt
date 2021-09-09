package za.co.woolworths.financial.services.android.checkout.interactor

import androidx.lifecycle.LiveData
import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressRequestBody
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutMockApiHelper

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserInteractor(
    private val checkoutAddAddressNewUserApiHelper: CheckoutAddAddressNewUserApiHelper,
    private val checkoutMockApiHelper: CheckoutMockApiHelper
) {

    fun getSuburbs(provinceId: String) = checkoutAddAddressNewUserApiHelper.getSuburbs(provinceId)
    fun validateSelectedSuburb(suburbId: String, isStore: Boolean) =
        checkoutAddAddressNewUserApiHelper.validateSelectedSuburb(suburbId, isStore)

    fun addAddress(addAddressRequestBody: AddAddressRequestBody): LiveData<Any> {
        return checkoutAddAddressNewUserApiHelper.addAddress(addAddressRequestBody)
    }

    fun updateAddress(addAddressRequestBody: AddAddressRequestBody, addressId: String) =
        checkoutMockApiHelper.updateAddress(addAddressRequestBody, addressId)

    fun deleteAddress(addressId: String) =
        checkoutAddAddressNewUserApiHelper.deleteAddress(addressId)

    fun changeAddress(nickName: String) = checkoutAddAddressNewUserApiHelper.changeAddress(nickName)
    fun getAvailableDeliverySlots() = checkoutMockApiHelper.getAvailableDeliverySlots()
    fun getConfirmDeliveryAddressDetails() =
        checkoutMockApiHelper.getConfirmDeliveryAddressDetails()
}