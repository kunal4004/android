package za.co.woolworths.financial.services.android.domain.repository

import retrofit2.Response
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.dto.order_again.OrderAgainRequestBody
import za.co.woolworths.financial.services.android.models.dto.order_again.OrderAgainResponse

interface OrderAgainRepository {

    suspend fun getOrderAgainList(body: OrderAgainRequestBody): Response<OrderAgainResponse>
    suspend fun getOrderAgainListInventory(
        storeId: String,
        multiSku: String,
        isUserBrowsing: Boolean = false
    ): Response<SkusInventoryForStoreResponse>
}