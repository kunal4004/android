package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard

import retrofit2.Response
import retrofit2.http.*
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse

interface WfsApiService {
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


    @Headers(
        "Content-Type: application/json",
        "Accept: application/vnd.appserver.api.v2+json",
        "Media-Type: application/json"
    )
    @POST("wfs/app/v4/accounts/storecard/cards")
    suspend fun queryServiceStoreCards(
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("lat") latitude: Double?,
        @Query("lon") longitude: Double?,
        @Body getStoreCardsRequestBody: StoreCardsRequestBody
    ): Response<StoreCardsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json"
    )
    @GET("wfs/app/v4/accounts/collections/checkEligibility")
    suspend fun queryServiceCheckCustomerEligibilityPlan(
        @Query("productGroupCode") productGroupCode: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String
    ): Response<EligibilityPlanResponse>


    /**
     * Credit Limit Increase
     */
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json"
    )
    @GET("wfs/app/v4/user/cli/offerActive")
    suspend fun queryServiceCliOfferActive(
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("productOfferingId") productOfferingId: String
    ): Response<OfferActive>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json"
    )
    @POST("wfs/app/v4/accounts/storecard/blockStoreCard/{productOfferingId}")
    suspend fun queryServiceBlockStoreCard(
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("productOfferingId") productOfferingId: String,
        @Body blockCardRequestBody: BlockCardRequestBody
    ): Response<BlockMyCardResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json"
    )
    @POST("wfs/app/v4/accounts/storecard/unblockStoreCard/{productOfferingId}")
    suspend fun queryServiceUnBlockStoreCard(
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("productOfferingId") productOfferingId: String,
        @Body blockCardRequestBody: BlockCardRequestBody
    ): Response<BlockMyCardResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("wfs/app/v4/user/email/{emailId}")
    suspend fun queryServiceNotifyCardNotYetReceived(
        @Path("emailId") emailId: String,
        @Body body: Any
    ): Response<BlockMyCardResponse>

}