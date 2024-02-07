package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.convertToResource
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.domain.repository.NotifyMeRepository
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.models.NotifyMeRequest
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.models.NotifyMeResponse
import javax.inject.Inject

class NotifyMeUC @Inject constructor (private val notifyMeRepository: NotifyMeRepository) {

    operator fun invoke(
        notifyMe: NotifyMeRequest
    ): Flow<Resource<NotifyMeResponse>> = flow {
        convertToResource {
            notifyMeRepository.notifyMe(notifyMe)
        }.collect {
            emit(it)
        }
    }
}
