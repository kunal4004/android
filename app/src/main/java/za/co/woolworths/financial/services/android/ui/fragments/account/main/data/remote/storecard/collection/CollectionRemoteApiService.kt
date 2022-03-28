package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.collection

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
        @Query("productGroupCode") productGroupCode: String
    ): Response<EligibilityPlanResponse>
}