package za.co.woolworths.financial.services.android.ui.activities.write_a_review.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.cart.service.network.CartResponse
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Action
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.repository.WriteAReviewFormRepository
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.request.PrepareWriteAReviewFormRequestEvent
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.response.WriteAReviewFormResponse
import javax.inject.Inject

@HiltViewModel
class WriteAReviewFormViewModel @Inject constructor(
    private val writeAReviewFormRepository: WriteAReviewFormRepository

): ViewModel() {
    private val _writeAReviewFormResponseData = MutableLiveData<Event<Resource<WriteAReviewFormResponse>>>()
    val writeAReviewFormResponseData: LiveData<Event<Resource<WriteAReviewFormResponse>>> = _writeAReviewFormResponseData

   /* private val _removeCartItem =
        MutableLiveData<Event<Resource<CartResponse>>>()
    val removeCartItem: LiveData<Event<Resource<CartResponse>>> =
        _removeCartItem*/

    fun createWriteAReviewFormRequest(productId: String?, writeAReviewFormRequest: PrepareWriteAReviewFormRequestEvent) {
        _writeAReviewFormResponseData.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = writeAReviewFormRepository.getWriteAReviewFormResponse(productId, writeAReviewFormRequest)
            _writeAReviewFormResponseData.value = Event(response)
           /* if (response.status == Status.SUCCESS) {
                _writeAReviewFormResponseData.value = response
               // var value = response.data?.response?.desc
            }*/
        }
    }
}