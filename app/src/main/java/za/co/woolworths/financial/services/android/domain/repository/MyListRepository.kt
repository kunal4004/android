package za.co.woolworths.financial.services.android.domain.repository

import retrofit2.Response
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.OrderToListReponse
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyItemToListRequest
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyListResponse

interface MyListRepository {

    suspend fun getMyList(): Response<ShoppingListsResponse>

    suspend fun addProductsToListById(
        listId: String,
        products: List<AddToListRequest>
    ): Response<ShoppingListItemsResponse>

    suspend fun copyToList(
        request: CopyItemToListRequest
    ): Response<CopyListResponse>

    suspend fun addProductsToListByOrderId(
        orderId: String,
        body: OrderToShoppingListRequestBody
    ): Response<OrderToListReponse>

    suspend fun createNewList(createList: CreateList): Response<ShoppingListsResponse>
}
