package za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.RetroInstance
import za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.RetroServiceInterface
import za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.getresponse.DyKeywordSearchResponse
import za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.request.DyKeywordSearchRequestEvent

class DyKeywordSearchViewModel : ViewModel() {
    val createKeywordSearchObserver: MutableLiveData<DyKeywordSearchResponse?>

    init {
        createKeywordSearchObserver = MutableLiveData()
    }

    fun createKeywordSearch(dyKeywordSearchRequestEvent: DyKeywordSearchRequestEvent?) {
        val retroServiceInterface = RetroInstance.getRetroInstance().create(
            RetroServiceInterface::class.java
        )
        val call = retroServiceInterface.dynamicYieldKeywordSearch(dyKeywordSearchRequestEvent)
        call.enqueue(object : Callback<DyKeywordSearchResponse?> {
            override fun onResponse(
                call: Call<DyKeywordSearchResponse?>,
                response: Response<DyKeywordSearchResponse?>
            ) {
                if (response.isSuccessful) {
                    createKeywordSearchObserver.postValue(response.body())
                } else {
                    createKeywordSearchObserver.postValue(null)
                }
            }

            override fun onFailure(call: Call<DyKeywordSearchResponse?>, t: Throwable) {
                createKeywordSearchObserver.postValue(null)
            }
        })
    }
}