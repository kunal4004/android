package za.co.woolworths.financial.services.android.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import za.co.woolworths.financial.services.android.domain.repository.OrderAgainRepository
import za.co.woolworths.financial.services.android.models.dto.order_again.OrderAgainRequestBody
import za.co.woolworths.financial.services.android.models.dto.order_again.OrderAgainResponse
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.convertToResource
import javax.inject.Inject

class OrderAgainUC @Inject constructor(
    private val orderAgainRepository: OrderAgainRepository
) {

    operator fun invoke(
        plist: String
    ): Flow<Resource<OrderAgainResponse>> = flow {
        convertToResource {
            orderAgainRepository.getOrderAgainList(
                OrderAgainRequestBody(
                    priceListId = plist
                )
            )
        }.collect {
            emit(it)
        }
    }
}