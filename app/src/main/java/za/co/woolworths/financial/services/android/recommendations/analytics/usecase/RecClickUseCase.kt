package za.co.woolworths.financial.services.android.recommendations.analytics.usecase

import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepository
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

class RecClickUseCase @Inject constructor(
    private val recommendationsRepository: RecommendationsRepository
) {

    suspend operator fun invoke(products: List<Product>): Resource<RecommendationResponse> {
        val event = prepareEvent(products)
        return recommendationsRepository.getRecommendationResponse(event)
    }

    private fun prepareEvent(products: List<Product>): RecommendationRequest? {
        val monetateId = Utils.getMonetateId()
        val eventType = Constants.EVENT_TYPE_REC_CLICKS
        val recClicks = products.mapNotNull { it.recToken }.filter { it.isNotEmpty() }
        return if (recClicks.isEmpty() || monetateId.isNullOrEmpty()) {
            null
        } else {
            RecommendationRequest(
                events = listOf(
                    Event(
                        eventType = eventType,
                        recClicks = recClicks,
                        cartLines = null,
                        categories = null,
                        pageType = null,
                        products = null,
                        url = null
                    )
                ), monetateId = monetateId
            )
        }
    }
}