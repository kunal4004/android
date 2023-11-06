package za.co.woolworths.financial.services.android.recommendations.analytics

import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.recommendations.analytics.usecase.RecClickUseCase
import za.co.woolworths.financial.services.android.recommendations.analytics.usecase.RecImpressionUseCase

interface RecommendationEvents {
    fun submitRecClicks(products: List<ProductList>)
    fun submitRecImpressions(recTokens: List<String>)
}

interface RecommendationUseCases {
    fun recClickUseCase(): RecClickUseCase

    fun recImpressionUseCase(): RecImpressionUseCase
}
