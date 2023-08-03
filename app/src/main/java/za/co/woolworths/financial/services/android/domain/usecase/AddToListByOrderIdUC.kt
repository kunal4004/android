package za.co.woolworths.financial.services.android.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.models.dto.OrderToListReponse
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.convertToResource
import javax.inject.Inject

class AddToListByOrderIdUC @Inject constructor(
    private val myListRepository: MyListRepository
) {

    operator fun invoke(
        orderId: String,
        body: OrderToShoppingListRequestBody
    ): Flow<Resource<OrderToListReponse>> = flow {
        convertToResource {
            myListRepository.addProductsToListByOrderId(
                orderId, body
            )
        }.collect {
            emit(it)
        }
    }
}