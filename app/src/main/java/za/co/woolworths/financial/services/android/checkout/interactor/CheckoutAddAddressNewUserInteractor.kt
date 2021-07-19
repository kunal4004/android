package za.co.woolworths.financial.services.android.checkout.interactor

import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressRequestBody
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutMockApiHelper

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserInteractor(private val checkoutAddAddressNewUserApiHelper: CheckoutAddAddressNewUserApiHelper, private val checkoutMockApiHelper: CheckoutMockApiHelper) {

    fun getSuburbs(provinceId: String) = checkoutAddAddressNewUserApiHelper.getSuburbs(provinceId)
    fun getProvince() = checkoutAddAddressNewUserApiHelper.getProvince()
    fun validateSelectedSuburb(suburbId: String, isStore: Boolean) = checkoutAddAddressNewUserApiHelper.validateSelectedSuburb(suburbId, isStore)
    fun getSavedAddresses() = checkoutMockApiHelper.getSavedAddresses()
    fun addAddress(addAddressRequestBody: AddAddressRequestBody) = checkoutMockApiHelper.addAddress(addAddressRequestBody)
    fun updateAddress(addAddressRequestBody: AddAddressRequestBody, addressId: String) = checkoutMockApiHelper.updateAddress(addAddressRequestBody, addressId)
    fun deleteAddress(addressId: String) = checkoutMockApiHelper.deleteAddress(addressId)
    fun changeAddress(nickName:String) = checkoutMockApiHelper.changeAddress(nickName)
}