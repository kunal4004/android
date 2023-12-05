package za.co.woolworths.financial.services.android.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.convertToResource
import za.co.woolworths.financial.services.android.presentation.addtolist.request.CopyItemToListRequest
import za.co.woolworths.financial.services.android.presentation.addtolist.response.CopyListResponse
import javax.inject.Inject

class AddToListUC @Inject constructor(
    private val myListRepository: MyListRepository
) {
    operator fun invoke(
        copyItemToListRequest: CopyItemToListRequest
    ): Flow<Resource<CopyListResponse>> = flow {
        convertToResource {
            myListRepository.addProductsToListById(copyItemToListRequest)
        }.collect {
            emit(it)
        }
    }
}