package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.remote.collection

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse

interface CollectionRemoteApiService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json"
    )
    @GET("wfs/app/v4/accounts/collections/checkEligibility")
    suspend fun queryServiceCheckCustomerEligibilityPlan(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Query("productGroupCode") productGroupCode: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String
    ): Response<EligibilityPlanResponse>
}