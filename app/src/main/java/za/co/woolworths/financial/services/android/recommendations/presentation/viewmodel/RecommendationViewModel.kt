package za.co.woolworths.financial.services.android.recommendations.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepository
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val recommendationsRepository: RecommendationsRepository
) : ViewModel() {


    private val _recommendationResponseData =
        MutableLiveData<Event<Resource<RecommendationResponse>>>()
    val recommendationResponseData: LiveData<Event<Resource<RecommendationResponse>>> =
        _recommendationResponseData

    fun getRecommendationResponse(recommendationRequest: RecommendationRequest) {
        viewModelScope.launch {
            val response =
                recommendationsRepository.getRecommendationResponse(recommendationRequest)
            _recommendationResponseData.value = Event(response)
        }
    }
}