package za.co.woolworths.financial.services.android.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import za.co.woolworths.financial.services.android.domain.repository.OrderAgainRepository
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.convertToResource
import javax.inject.Inject


class AddToCartUC @Inject constructor(
    private val orderAgainRepository: OrderAgainRepository
) {

    operator fun invoke(
        items: List<AddItemToCart>
    ): Flow<Resource<AddItemToCartResponse>> = flow {
        convertToResource {
            orderAgainRepository.addItemsToCart(
                items
            )
        }.collect {
            emit(it)
        }
    }
}