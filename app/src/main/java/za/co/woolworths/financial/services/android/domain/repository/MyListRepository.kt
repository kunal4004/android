package za.co.woolworths.financial.services.android.domain.repository

import retrofit2.Response
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.OrderToListReponse
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.presentation.addtolist.request.CopyItemDetail
import za.co.woolworths.financial.services.android.presentation.addtolist.request.CopyItemToListRequest
import za.co.woolworths.financial.services.android.presentation.addtolist.response.CopyListResponse

interface MyListRepository {

    suspend fun getMyList(): Response<ShoppingListsResponse>

    suspend fun addProductsToListById(
        copyItemToListRequest: CopyItemToListRequest
    ): Response<CopyListResponse>

    suspend fun addProductsToListByOrderId(
        orderId: String,
        body: OrderToShoppingListRequestBody
    ): Response<OrderToListReponse>

    suspend fun createNewList(createList: CreateList): Response<ShoppingListsResponse>
}
