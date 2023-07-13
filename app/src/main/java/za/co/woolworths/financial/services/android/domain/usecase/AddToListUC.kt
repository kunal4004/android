package za.co.woolworths.financial.services.android.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.convertToResource
import javax.inject.Inject

class AddToListUC @Inject constructor(
    private val myListRepository: MyListRepository
) {
    operator fun invoke(
        listId: String,
        products: List<AddToListRequest>
    ): Flow<Resource<ShoppingListItemsResponse>> = flow {
        convertToResource {
            myListRepository.addProductsToListById(listId, products)
        }.collect {
            emit(it)
        }
    }
}