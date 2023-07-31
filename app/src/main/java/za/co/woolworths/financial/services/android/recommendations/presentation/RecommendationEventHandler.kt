package za.co.woolworths.financial.services.android.recommendations.presentation

/**
 * To be used when required to track the event of item added to cart from the recommendations
 */
interface RecommendationEventHandler: RecommendationLoadingNotifier {
    fun onItemAddedToCart()
}

interface RecommendationLoadingNotifier {
    fun onRecommendationsLoadedSuccessfully()
}