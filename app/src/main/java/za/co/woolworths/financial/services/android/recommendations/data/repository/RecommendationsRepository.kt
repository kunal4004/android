package za.co.woolworths.financial.services.android.recommendations.data.repository

import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest

interface RecommendationsRepository {

    suspend fun getRecommendationResponse(recommendationRequest: RecommendationRequest?, requestData: Boolean, fulfillmentStoreId: String? = null): Resource<RecommendationResponse>

}
