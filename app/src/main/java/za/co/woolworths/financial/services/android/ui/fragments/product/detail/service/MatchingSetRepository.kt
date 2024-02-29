package za.co.woolworths.financial.services.android.ui.fragments.product.detail.service

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.dto.ProductRequest
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource

interface MatchingSetRepository {
    suspend fun getMatchingItemDetail(productsDetailsRequest: ProductRequest): Flow<CoreDataSource.IOTaskResult<ProductDetailResponse>>
    suspend fun getInventoryForMatchingItems(storeId:String, multipleSku:String): Flow<CoreDataSource.IOTaskResult<SkusInventoryForStoreResponse>>
    suspend fun addToCartForMatchingItems(addToCart: MutableList<AddItemToCart>): Flow<CoreDataSource.IOTaskResult<AddItemToCartResponse>>

}