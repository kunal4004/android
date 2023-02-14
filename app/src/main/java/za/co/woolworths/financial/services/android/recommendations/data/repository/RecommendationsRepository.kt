package za.co.woolworths.financial.services.android.recommendations.data.repository

import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest

interface RecommendationsRepository {

    suspend fun getRecommendationResponse(recommendationRequest: RecommendationRequest?): Resource<RecommendationResponse>
    suspend fun addItemsToCart(mAddItemsToCart: MutableList<AddItemToCart>): Resource<AddItemToCartResponse>
    suspend fun fetchInventorySkuForStore(mStoreId: String, referenceId: String): Resource<SkusInventoryForStoreResponse>
    suspend fun callStoreFinder(sku: String, startRadius: String?, endRadius: String?): Resource<LocationResponse>
}
