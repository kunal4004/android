package za.co.woolworths.financial.services.android.domain.repository

import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.OrderToListReponse
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource

interface MyListRepository {

    suspend fun getMyList(): Response<ShoppingListsResponse>

    suspend fun addProductsToListById(
        listId: String,
        products: List<AddToListRequest>,
    ): Response<ShoppingListItemsResponse>

    suspend fun addProductsToListByOrderId(
        orderId: String,
        body: OrderToShoppingListRequestBody,
    ): Response<OrderToListReponse>

    suspend fun createNewList(createList: CreateList): Response<ShoppingListsResponse>

    suspend fun getCartSummary(): Flow<CoreDataSource.IOTaskResult<CartSummaryResponse>>
    suspend fun callValidatePlaceDetails(placeId: String): Flow<CoreDataSource.IOTaskResult<ValidateLocationResponse>>
}
