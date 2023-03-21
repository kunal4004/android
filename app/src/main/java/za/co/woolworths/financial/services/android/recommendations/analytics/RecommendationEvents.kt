package za.co.woolworths.financial.services.android.recommendations.analytics

import za.co.woolworths.financial.services.android.recommendations.analytics.usecase.RecClickUseCase
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product

interface RecommendationEvents {
    fun submitRecClicks(products: List<Product>)
}

interface RecommendationUseCases {
    fun recClickUseCase(): RecClickUseCase
}
