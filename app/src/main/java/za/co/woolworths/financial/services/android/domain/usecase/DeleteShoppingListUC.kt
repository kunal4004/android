package za.co.woolworths.financial.services.android.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.convertToResource
import javax.inject.Inject

class DeleteShoppingListUC @Inject constructor(
    private val myListRepository: MyListRepository
) {

    operator fun invoke(
        id: String
    ): Flow<Resource<ShoppingListsResponse>> = flow {
        convertToResource {
            myListRepository.deleteShoppingList(
                id = id
            )
        }.collect {
            emit(it)
        }
    }.flowOn(Dispatchers.IO)
}