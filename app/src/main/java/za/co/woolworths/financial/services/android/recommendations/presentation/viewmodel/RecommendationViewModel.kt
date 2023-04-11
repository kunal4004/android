package za.co.woolworths.financial.services.android.recommendations.presentation.viewmodel

import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepository
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Action
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.ui.extension.isConnectedToNetwork
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val recommendationsRepository: RecommendationsRepository
) : ViewModel() {

    private var currentSelectedTab = 0
    private var submittedRecImpressions: ArrayList<String> = arrayListOf()

    private val _recommendationResponseData = MutableLiveData<List<Action>?>()
    val recommendationResponseData: LiveData<List<Action>?> = _recommendationResponseData

    private val _visibleRecommendationItemRequest = MutableLiveData<Boolean?>()
    val visibleRecommendationItemRequest: LiveData<Boolean?> = _visibleRecommendationItemRequest
    private var handler: Handler? = null

    fun setCurrentSelectedTab(tabPosition: Int) {
        currentSelectedTab = tabPosition
        parentPageScrolledToRecommendation()
//        if(handler == null) {
//            handler = Handler(Looper.getMainLooper())
//            handler?.postDelayed(runnable, 1000)
//        }
    }

    private val runnable = Runnable {
        parentPageScrolledToRecommendation()
    }

    fun getRecommendationResponse(recommendationRequest: RecommendationRequest) {
        viewModelScope.launch {
            val response =
                recommendationsRepository.getRecommendationResponse(recommendationRequest)
            if (response.status == Status.SUCCESS) {
                _recommendationResponseData.value = response.data?.actions
                if (!response.data?.monetateId.isNullOrEmpty()) {
                    Utils.saveMonetateId(response.data?.monetateId)
                }
            }
        }
    }

    fun parentPageScrolledToRecommendation() {
        requestVisibleRecommendationProducts()
    }

    private fun requestVisibleRecommendationProducts() {
        _visibleRecommendationItemRequest.value = true
    }

    fun visibleRecommendationProducts(visibleProductPositions: List<Int>) {
        val validPositions = visibleProductPositions.filter { it > -1 }
        if (validPositions.isEmpty()) {
            return
        }
        val tokensVisited = tokensFromPosition(currentSelectedTab, validPositions)

        val eligibleTokensToSubmit = if (submittedRecImpressions.isNotEmpty()) {
            filterRecTokens(
                tokensVisited = tokensVisited, submittedTokens = submittedRecImpressions
            )
        } else {
            tokensVisited
        }

        submitRecImpressionEvent(eligibleTokensToSubmit)
    }

    private fun tokensFromPosition(tabPosition: Int, itemPosition: List<Int>): List<String> {
        val recTokens = arrayListOf<String>()
        if (!recommendationResponseData.value.isNullOrEmpty() && tabPosition < (recommendationResponseData.value?.size
                ?: 0)
        ) {
            itemPosition.forEach { position ->
                if (position < (recommendationResponseData.value?.get(tabPosition)?.products?.size
                        ?: 0) && !recommendationResponseData.value?.get(tabPosition)?.products?.get(
                        position
                    )?.recToken.isNullOrEmpty()
                ) recTokens.add(
                    recommendationResponseData.value?.get(tabPosition)?.products?.get(
                        position
                    )?.recToken!!
                )
            }
        }
        return recTokens
    }

    private fun filterRecTokens(
        tokensVisited: List<String>, submittedTokens: List<String>?
    ): List<String> {
        return if (submittedTokens.isNullOrEmpty()) {
            tokensVisited
        } else {
            tokensVisited.filter { !submittedTokens.contains(it) }
        }
    }

    private fun submitRecImpressionEvent(recTokens: List<String>) {
        Log.e("TAG", "submittedRecImpressions Size : ${submittedRecImpressions.size}")
        Log.e("TAG", "submittedRecImpressions : $submittedRecImpressions")
        Log.e("TAG", "New Eligible recTokens : $recTokens")
        if (isConnectedToNetwork() == true) {
            WoolworthsApplication.getInstance().recommendationAnalytics.submitRecImpressions(
                recTokens
            )
        }
        submittedRecImpressions.addAll(recTokens)
    }

}