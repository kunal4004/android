package za.co.woolworths.financial.services.android.data.repository // ktlint-disable filename

import retrofit2.Response
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.OrderToListReponse
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.presentation.addtolist.request.CopyItemDetail
import za.co.woolworths.financial.services.android.presentation.addtolist.request.CopyItemToListRequest
import za.co.woolworths.financial.services.android.presentation.addtolist.response.CopyListResponse
import javax.inject.Inject

class MyListRepositoryImpl @Inject constructor() : MyListRepository {

    override suspend fun getMyList(): Response<ShoppingListsResponse> =
        OneAppService().getShoppingList()

    override suspend fun addProductsToListById(
        copyItemToListRequest: CopyItemToListRequest,
    ): Response<CopyListResponse> = OneAppService().addProductsToListV2(copyItemToListRequest)

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
