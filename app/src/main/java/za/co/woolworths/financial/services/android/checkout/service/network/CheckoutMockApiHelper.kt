package za.co.woolworths.financial.services.android.checkout.service.network

import retrofit2.Response
import za.co.woolworths.financial.services.android.models.network.OneAppService


/**
 * Created by Kunal Uttarwar on 10/06/21.
 */
class CheckoutMockApiHelper : MockRetrofitConfig() {

    fun updateAddress(
        addAddressRequestBody: AddAddressRequestBody,
        addressId: String
    ): Response<AddAddressResponse> =
        OneAppService.updateAddress(addAddressRequestBody, addressId).execute()

    fun changeAddress(nickName: String): Response<ChangeAddressResponse> =
        OneAppService.changeAddress(nickName).execute()

    fun getAvailableDeliverySlots(): Response<AvailableDeliverySlotsResponse> =
        OneAppService.getAvailableDeliverySlots().execute()
}