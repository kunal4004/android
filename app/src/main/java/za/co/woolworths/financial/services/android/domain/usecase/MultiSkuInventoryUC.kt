package za.co.woolworths.financial.services.android.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import za.co.woolworths.financial.services.android.domain.repository.OrderAgainRepository
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.convertToResource
import javax.inject.Inject

class MultiSkuInventoryUC @Inject constructor(
    private val orderAgainRepository: OrderAgainRepository
) {

    operator fun invoke(
        storeId: String,
        multiSku: String
    ): Flow<Resource<SkusInventoryForStoreResponse>> = flow {
        convertToResource {
            orderAgainRepository.getOrderAgainListInventory(
                storeId = storeId,
                multiSku = multiSku,
                isUserBrowsing = false
            )
        }.collect {
            emit(it)
        }
    }
}