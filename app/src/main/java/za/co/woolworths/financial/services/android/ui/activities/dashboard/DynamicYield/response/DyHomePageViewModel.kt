package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.response

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.dynamicyield.data.response.getResponse.DynamicYieldChooseVariationResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.HomePageRequestEvent

class DyHomePageViewModel: ViewModel() {
    lateinit var createDyHomePageLiveData: MutableLiveData<DynamicYieldChooseVariationResponse?>
    init {
        createDyHomePageLiveData = MutableLiveData()
    }

    fun getDyLiveData(): MutableLiveData<DynamicYieldChooseVariationResponse?> {
        return createDyHomePageLiveData
    }

    fun createDyRequest(dynamicYieldRequestEvent: HomePageRequestEvent) {
        // val retroService = ApiInterface::class
        val call = OneAppService().dynamicYieldHomePage(dynamicYieldRequestEvent)
        call.enqueue(object: Callback<DynamicYieldChooseVariationResponse> {
            override fun onResponse(
                call: Call<DynamicYieldChooseVariationResponse>,
                response: Response<DynamicYieldChooseVariationResponse>
            ) {
                if (response.isSuccessful) {
                    createDyHomePageLiveData.postValue(response.body())
                } else {
                    createDyHomePageLiveData.postValue(null)
                }
            }

            override fun onFailure(call: Call<DynamicYieldChooseVariationResponse>, t: Throwable) {
                createDyHomePageLiveData.postValue(null)
            }

        })
    }
}