package za.co.woolworths.financial.services.android.domain.repository

import retrofit2.Response

interface CheckoutRepository {
    suspend fun checkoutComplete(suburbId: String): Response<Unit>

}