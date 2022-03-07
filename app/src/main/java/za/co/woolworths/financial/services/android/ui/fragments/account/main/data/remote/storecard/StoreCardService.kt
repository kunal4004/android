package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse

interface StoreCardService {
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json"
    )
    @GET("wfs/app/v4/user/creditCardToken")
    suspend fun getCreditCardToken(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String
    ): Response<CreditCardTokenResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json"
    )
    @GET("wfs/app/v4/payments/payu/methods")
    suspend fun getPaymentPAYUMethod(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String
    ): Response<PaymentMethodsResponse>

}