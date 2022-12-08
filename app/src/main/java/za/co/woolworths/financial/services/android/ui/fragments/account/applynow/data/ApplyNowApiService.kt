package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.data

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ApplyNowModel

interface ApplyNowApiService {
    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:120")
    @GET("/wfs/app/v4/mobileconfigs/content")
    suspend fun applyNowService(
        @Query("contentId") contentId: String
    ): Response<ApplyNowModel>
}