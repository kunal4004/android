package za.co.woolworths.financial.services.android.data.repository

import retrofit2.Response
import za.co.woolworths.financial.services.android.domain.repository.OrderAgainRepository
import za.co.woolworths.financial.services.android.models.dto.order_again.OrderAgainRequestBody
import za.co.woolworths.financial.services.android.models.dto.order_again.OrderAgainResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import javax.inject.Inject

class OrderAgainRepositoryImpl @Inject constructor(

): OrderAgainRepository {

    override suspend fun getOrderAgainList(body: OrderAgainRequestBody): Response<OrderAgainResponse> =
        OneAppService().getOrderAgainList(body)

}