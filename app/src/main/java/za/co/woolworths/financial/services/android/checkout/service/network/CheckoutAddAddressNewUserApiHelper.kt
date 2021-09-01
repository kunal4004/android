package za.co.woolworths.financial.services.android.checkout.service.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Response
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserApiHelper: RetrofitConfig() {

    fun getSuburbs(provinceId: String): Response<SuburbsResponse> = OneAppService.getSuburbs(provinceId).execute()
    fun validateSelectedSuburb(suburbId: String, isStore: Boolean): Response<ValidateSelectedSuburbResponse> = OneAppService.validateSelectedSuburb(suburbId, isStore).execute()
    fun addAddress(addAddressRequestBody: AddAddressRequestBody): LiveData<Any> {
        val addAddressData = MutableLiveData<Any>()
        OneAppService.addAddress(addAddressRequestBody)?.enqueue(CompletionHandler(object :
            IResponseListener<AddAddressResponse> {
            override fun onSuccess(addAddressResponse: AddAddressResponse?) {
                addAddressData.value = addAddressResponse ?: null
            }

            override fun onFailure(error: Throwable?) {
                if (error != null) {
                    addAddressData.value = error!!
                }
            }

        }, AddAddressResponse::class.java))
        return addAddressData
    }
}