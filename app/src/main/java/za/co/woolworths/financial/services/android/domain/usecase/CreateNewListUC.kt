package za.co.woolworths.financial.services.android.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.convertToResource
import javax.inject.Inject

class CreateNewListUC @Inject constructor(
    private val myListRepository: MyListRepository
) {

    operator fun invoke(
        name: String
    ): Flow<Resource<ShoppingListsResponse>> = flow {
        convertToResource {
            myListRepository.createNewList(
                CreateList(name = name)
            )
        }.collect {
            emit(it)
        }
    }
}