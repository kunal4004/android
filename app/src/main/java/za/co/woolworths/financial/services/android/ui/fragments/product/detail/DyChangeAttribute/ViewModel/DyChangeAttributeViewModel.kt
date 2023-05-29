package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Response.DyChangeAttributeResponse

class DyChangeAttributeViewModel: ViewModel() {
    lateinit var createDyChangeAttributeLiveData: MutableLiveData<DyChangeAttributeResponse?>
    init {
        createDyChangeAttributeLiveData = MutableLiveData()
    }

    fun getDyLiveData(): MutableLiveData<DyChangeAttributeResponse?> {
        return createDyChangeAttributeLiveData
    }

    fun createDyChangeAttributeRequest(prepareChangeAttributeRequestEvent: PrepareChangeAttributeRequestEvent) {
        // val retroService = ApiInterface::class
        val call = OneAppService().dynamicYieldChangeAttribute(prepareChangeAttributeRequestEvent)
       call.enqueue(object: Callback<DyChangeAttributeResponse> {
           override fun onResponse(
               call: Call<DyChangeAttributeResponse>,
               response: Response<DyChangeAttributeResponse>
           ) {
               if (response.isSuccessful)
                   createDyChangeAttributeLiveData.postValue(response.body())
               else
                   createDyChangeAttributeLiveData.postValue(null)
           }

           override fun onFailure(call: Call<DyChangeAttributeResponse>, t: Throwable) {
               createDyChangeAttributeLiveData.postValue(null)
           }

       })
    }
}