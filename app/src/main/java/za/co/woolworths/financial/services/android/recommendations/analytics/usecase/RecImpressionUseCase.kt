package za.co.woolworths.financial.services.android.recommendations.analytics.usecase

import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepository
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.CommonRecommendationEvent
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

class RecImpressionUseCase @Inject constructor(
    private val recommendationsRepository: RecommendationsRepository
) {

    suspend operator fun invoke(recTokens: List<String>): Resource<RecommendationResponse> {
        val event = prepareEvent(recTokens)
        return recommendationsRepository.getRecommendationResponse(event, false, null)
    }

    private fun prepareEvent(recTokens: List<String>): RecommendationRequest? {
        val monetateId = Utils.getMonetateId()
        val eventType = Constants.EVENT_TYPE_REC_IMPRESSIONS
        return if (recTokens.isEmpty() || monetateId.isNullOrEmpty()) {
            null
        } else {
            RecommendationRequest(
                events = listOf(
                    Event(
                        eventType = eventType,
                        recImpressions = recTokens,
                        cartLines = null,
                        categories = null,
                        pageType = null,
                        products = null,
                        url = null
                    )
                ).plus(CommonRecommendationEvent.commonRecommendationEvents()),
                monetateId = monetateId
            )
        }
    }
}