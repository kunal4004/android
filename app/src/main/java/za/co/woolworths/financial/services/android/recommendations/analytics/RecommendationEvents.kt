package za.co.woolworths.financial.services.android.recommendations.analytics

import za.co.woolworths.financial.services.android.recommendations.analytics.usecase.RecClickUseCase
import za.co.woolworths.financial.services.android.recommendations.analytics.usecase.RecImpressionUseCase
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product

interface RecommendationEvents {
    fun submitRecClicks(products: List<Product>)
    fun submitRecImpressions(recTokens: List<String>)
}

interface RecommendationUseCases {
    fun recClickUseCase(): RecClickUseCase

    fun recImpressionUseCase(): RecImpressionUseCase
}
