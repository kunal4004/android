package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.domain.repository

import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.models.NotifyMeResponse
import retrofit2.Response
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.models.NotifyMeRequest

interface NotifyMeRepository {

suspend fun notifyMe(notifyMe: NotifyMeRequest) : Response<NotifyMeResponse>

}
