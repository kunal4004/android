package za.co.woolworths.financial.services.android.checkout.service.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.SetDeliveryLocationSuburbResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig

/**
 * Created by Kunal Uttarwar on 12/08/21.
 */
class CheckoutAddressConfirmationApiHelper : RetrofitConfig() {
    fun setSuburb(suburbId: String): LiveData<Any> {
        val setSuburbData = MutableLiveData<Any>()
        OneAppService.setSuburb(suburbId).enqueue(CompletionHandler(object :
            IResponseListener<SetDeliveryLocationSuburbResponse> {
            override fun onSuccess(response: SetDeliveryLocationSuburbResponse?) {
                setSuburbData.value = response ?: null
            }

            override fun onFailure(error: Throwable?) {
                if (error != null) {
                    setSuburbData.value = error!!
                }
            }

        }, SetDeliveryLocationSuburbResponse::class.java))
        return setSuburbData

    }
}