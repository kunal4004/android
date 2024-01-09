package za.co.woolworths.financial.services.android.data.repository

import retrofit2.Response
import za.co.woolworths.financial.services.android.domain.repository.CheckoutRepository
import za.co.woolworths.financial.services.android.models.network.OneAppService
import javax.inject.Inject

class CheckoutRepositoryImpl @Inject constructor() : CheckoutRepository {

    override suspend fun checkoutComplete(suburbId: String): Response<Unit> =
        OneAppService().postCheckoutComplete(suburbId)
}