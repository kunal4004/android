package za.co.woolworths.financial.services.android.data.repository // ktlint-disable filename

import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.OrderToListReponse
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.network.ApiInterface
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import javax.inject.Inject

class MyListRepositoryImpl @Inject constructor(private val apiInterface: ApiInterface) :
    MyListRepository, CoreDataSource(), ApiInterface by apiInterface {


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

    override suspend fun getCartSummary(): Flow<IOTaskResult<CartSummaryResponse>> =
        executeSafeNetworkApiCall {
            getShoppingCartSummary(
                "",
                "",
                super.getSessionToken(),
                super.getDeviceIdentityToken(),
            )
        }

    override suspend fun callValidatePlaceDetails(placeId: String): Flow<IOTaskResult<ValidateLocationResponse>> =
        executeSafeNetworkApiCall {
            validatePlace(
                "",
                "",
                super.getSessionToken(),
                super.getDeviceIdentityToken(),
                placeId,
                false
            )
        }

    override suspend fun deleteShoppingList(id: String): Response<ShoppingListsResponse> =
        OneAppService().deleteShoppingList(listId = id)

}
