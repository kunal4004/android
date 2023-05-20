package za.co.woolworths.financial.services.android.dynamicyield.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.dynamicyield.data.response.getResponse.DynamicYieldChooseVariationResponse
import za.co.woolworths.financial.services.android.dynamicyield.data.response.request.DynamicVariantRequestEvent
import za.co.woolworths.financial.services.android.models.network.OneAppService

class DynamicYieldViewModel: ViewModel() {
    lateinit var createDyLiveData: MutableLiveData<DynamicYieldChooseVariationResponse?>
    init {
        createDyLiveData = MutableLiveData()
    }

    fun getDyLiveData(): MutableLiveData<DynamicYieldChooseVariationResponse?> {
        return createDyLiveData
    }

    fun createDyRequest(dynamicYieldRequestEvent: DynamicVariantRequestEvent) {
       // val retroService = ApiInterface::class
        val call = OneAppService().dynamicYieldChooseVariation(dynamicYieldRequestEvent)
        call.enqueue(object: Callback<DynamicYieldChooseVariationResponse> {
            override fun onResponse(
                call: Call<DynamicYieldChooseVariationResponse>,
                response: Response<DynamicYieldChooseVariationResponse>
            ) {
                if (response.isSuccessful) {
                    createDyLiveData.postValue(response.body())
                } else {
                    createDyLiveData.postValue(null)
                }
            }

            override fun onFailure(call: Call<DynamicYieldChooseVariationResponse>, t: Throwable) {
                createDyLiveData.postValue(null)
            }

        })
    }
}