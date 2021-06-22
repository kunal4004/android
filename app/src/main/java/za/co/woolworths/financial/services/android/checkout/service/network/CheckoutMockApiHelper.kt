package za.co.woolworths.financial.services.android.checkout.service.network

import za.co.woolworths.financial.services.android.models.network.OneAppService

/**
 * Created by Kunal Uttarwar on 10/06/21.
 */
class CheckoutMockApiHelper: MockRetrofitConfig() {
    fun getSavedAddresses() = OneAppService.getSavedAddresses().execute()
    fun addAddress(addAddressRequestBody: AddAddressRequestBody) = OneAppService.addAddress(addAddressRequestBody).execute()
    fun updateAddress(addAddressRequestBody: AddAddressRequestBody, addressId: String) = OneAppService.updateAddress(addAddressRequestBody, addressId).execute()
    fun changeAddress(nickName: String) = OneAppService.changeAddress(nickName).execute()
}