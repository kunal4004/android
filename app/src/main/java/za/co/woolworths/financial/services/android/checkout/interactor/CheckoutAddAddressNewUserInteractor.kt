package za.co.woolworths.financial.services.android.checkout.interactor

import androidx.lifecycle.LiveData
import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressRequestBody
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.ShippingDetailsBody
import za.co.woolworths.financial.services.android.models.network.ConfirmDeliveryAddressBody

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserInteractor(
    private val checkoutAddAddressNewUserApiHelper: CheckoutAddAddressNewUserApiHelper
) {

    fun getSuburbs(provinceId: String) = checkoutAddAddressNewUserApiHelper.getSuburbs(provinceId)
    fun validateSelectedSuburb(suburbId: String, isStore: Boolean) =
        checkoutAddAddressNewUserApiHelper.validateSelectedSuburb(suburbId, isStore)

    fun addAddress(addAddressRequestBody: AddAddressRequestBody): LiveData<Any> {
        return checkoutAddAddressNewUserApiHelper.addAddress(addAddressRequestBody)
    }

    fun editAddress(addAddressRequestBody: AddAddressRequestBody, addressId: String) =
        checkoutAddAddressNewUserApiHelper.editAddress(addAddressRequestBody, addressId)

    fun deleteAddress(addressId: String) =
        checkoutAddAddressNewUserApiHelper.deleteAddress(addressId)

    fun changeAddress(nickName: String) = checkoutAddAddressNewUserApiHelper.changeAddress(nickName)

    fun getConfirmDeliveryAddressDetails(body: ConfirmDeliveryAddressBody) =
        checkoutAddAddressNewUserApiHelper.getConfirmDeliveryAddressDetails(body = body)

    fun getShippingDetails(body: ShippingDetailsBody) =
        checkoutAddAddressNewUserApiHelper.getShippingDetails(body = body)
}