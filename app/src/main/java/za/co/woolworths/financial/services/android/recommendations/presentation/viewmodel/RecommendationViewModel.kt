package za.co.woolworths.financial.services.android.recommendations.presentation.viewmodel

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

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val recommendationsRepository: RecommendationsRepository
) : ViewModel() {

    companion object {
        const val MIN_VISIBLE_RECOMMENDATION_ITEMS = 2
    }

    private var currentSelectedTab = 0
    private var recommendationTitle: String? = null
    private var submittedRecImpressions: ArrayList<String> = arrayListOf()

    private val _recommendationResponseData = MutableLiveData<List<Action>?>()
    val recommendationResponseData: LiveData<List<Action>?> = _recommendationResponseData

    private val _visibleRecommendationItemRequest = MutableLiveData<Boolean?>()
    val visibleRecommendationItemRequest: LiveData<Boolean?> = _visibleRecommendationItemRequest

    fun clearRecommendations() {
        _recommendationResponseData.value = null
    }

    fun setCurrentSelectedTab(tabPosition: Int) {
        currentSelectedTab = tabPosition
        recImpressionOnTabChanged(currentSelectedTab)
    }

    private fun recImpressionOnTabChanged(tabPosition: Int) {
        val productCount = recommendationResponseData.value?.size ?: 0

        if (tabPosition < 0 || tabPosition >= productCount) {
            return
        }

        val eligibleItemCountForRecImpression =
            if (productCount > MIN_VISIBLE_RECOMMENDATION_ITEMS) {
                MIN_VISIBLE_RECOMMENDATION_ITEMS
            } else {
                productCount
            }
        val recTokens = arrayListOf<String>()
        for (i in 0 until eligibleItemCountForRecImpression) {
            if (i < (recommendationResponseData.value?.get(tabPosition)?.products?.size ?: 0) &&
                !recommendationResponseData.value?.get(tabPosition)?.products?.get(i)?.recToken.isNullOrEmpty()
            ) {
                recTokens.add(recommendationResponseData.value?.get(tabPosition)?.products?.get(i)?.recToken!!)
            }
        }

        filterAndSubmitRecImpression(
            tokensVisited = recTokens, submittedTokens = submittedRecImpressions
        )
    }

    fun clearSubmittedRecImpressions() {
        submittedRecImpressions.clear()
        currentSelectedTab = 0
    }

    fun getRecommendationResponse(recommendationRequest: RecommendationRequest) {
        viewModelScope.launch {
            val response =
                recommendationsRepository.getRecommendationResponse(recommendationRequest)
            if (response.status == Status.SUCCESS) {
                recommendationTitle = response.data?.title
                _recommendationResponseData.value = response.data?.actions?.filterNot { it.products.isNullOrEmpty() }
                if (!response.data?.monetateId.isNullOrEmpty()) {
                    Utils.saveMonetateId(response.data?.monetateId)
                }
            }
        }
    }

    fun recommendationTitle() = recommendationTitle

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

        filterAndSubmitRecImpression(
            tokensVisited = tokensVisited, submittedTokens = submittedRecImpressions
        )
    }

    private fun filterAndSubmitRecImpression(
        tokensVisited: List<String>, submittedTokens: List<String>?
    ) {
        val eligibleTokensToSubmit = filterRecTokens(
            tokensVisited = tokensVisited, submittedTokens = submittedTokens
        )
        submitRecImpressionEvent(eligibleTokensToSubmit)
    }

    private fun tokensFromPosition(tabPosition: Int, itemPosition: List<Int>): List<String> {
        val recTokens = arrayListOf<String>()
        if (tabPosition >= 0 && !recommendationResponseData.value.isNullOrEmpty() && tabPosition < (recommendationResponseData.value?.size
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
        if (recTokens.isEmpty()) {
            return
        }

        if (isConnectedToNetwork() == true) {
            WoolworthsApplication.getInstance().recommendationAnalytics.submitRecImpressions(
                recTokens
            )
        }
        submittedRecImpressions.addAll(recTokens)
    }

}