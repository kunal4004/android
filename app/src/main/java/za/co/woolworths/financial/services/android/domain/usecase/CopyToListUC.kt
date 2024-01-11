package za.co.woolworths.financial.services.android.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.convertToResource
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyItemToListRequest
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyListResponse
import javax.inject.Inject

class CopyToListUC @Inject constructor(
    private val myListRepository: MyListRepository
) {
    operator fun invoke(
        request: CopyItemToListRequest
    ): Flow<Resource<CopyListResponse>> = flow {
        convertToResource {
            myListRepository.copyToList(request)
        }.collect {
            emit(it)
        }
    }
}