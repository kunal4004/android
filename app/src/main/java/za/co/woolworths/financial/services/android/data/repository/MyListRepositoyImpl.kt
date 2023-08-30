package za.co.woolworths.financial.services.android.data.repository // ktlint-disable filename

import retrofit2.Response
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.OrderToListReponse
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import javax.inject.Inject

class MyListRepositoryImpl @Inject constructor() : MyListRepository {

    override suspend fun getMyList(): Response<ShoppingListsResponse> =
        OneAppService().getShoppingList()

    override suspend fun addProductsToListById(
        listId: String,
        products: List<AddToListRequest>,
    ): Response<ShoppingListItemsResponse> = OneAppService().addProductsToList(products, listId)

    override suspend fun addProductsToListByOrderId(
        orderId: String,
        body: OrderToShoppingListRequestBody,
    ): Response<OrderToListReponse> = OneAppService().addToListByOrderId(
        orderId,
        body,
    )

    override suspend fun createNewList(createList: CreateList): Response<ShoppingListsResponse> =
        OneAppService().createNewList(createList)
}
