package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.data.repository

import retrofit2.Response
import za.co.woolworths.financial.services.android.models.network.ApiInterface
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.domain.repository.NotifyMeRepository
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.models.NotifyMeRequest
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.models.NotifyMeResponse
import javax.inject.Inject

class NotifyMeRepositoryImpl @Inject constructor(
    private val apiInterface: ApiInterface
) : NotifyMeRepository, CoreDataSource(), ApiInterface by apiInterface {

    override suspend fun notifyMe(notifyMeRequest: NotifyMeRequest): Response<NotifyMeResponse> =
        OneAppService().notifyMe(notifyMeRequest)

}