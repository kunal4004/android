package za.co.woolworths.financial.services.android.models.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.*
import za.co.absa.openbankingapi.woolworths.integration.dto.PayUResponse
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.dynamicyield.data.response.getResponse.DynamicYieldChooseVariationResponse
import za.co.woolworths.financial.services.android.endlessaisle.service.network.UserLocationResponse
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.AddSubstitutionRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.AddSubstitutionResponse
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.KiboProductRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.KiboProductResponse
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.ProductSubstitution
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.request.SaveAddressLocationRequest
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel.ValidateStoreResponse
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.account.*
import za.co.woolworths.financial.services.android.models.dto.bpi.BPIBody
import za.co.woolworths.financial.services.android.models.dto.bpi.InsuranceTypeOptInBody
import za.co.woolworths.financial.services.android.models.dto.cart.SubmittedOrderResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_activation.CreditCardActivationRequestBody
import za.co.woolworths.financial.services.android.models.dto.credit_card_activation.CreditCardActivationResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.AvailableTimeSlotsResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.PossibleAddressResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.ScheduleDeliveryRequest
import za.co.woolworths.financial.services.android.models.dto.dash.LastOrderDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkDeviceBody
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.dto.npc.*
import za.co.woolworths.financial.services.android.models.dto.order_again.OrderAgainRequestBody
import za.co.woolworths.financial.services.android.models.dto.order_again.OrderAgainResponse
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPRequest
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPResponse
import za.co.woolworths.financial.services.android.models.dto.pma.DeleteResponse
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.dto.size_guide.SizeGuideResponse
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementResponse
import za.co.woolworths.financial.services.android.models.dto.statement.StatementResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardResponse
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyOptOutBody
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyRepliesBody
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CouponClaimCode
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.SelectedVoucher
import za.co.woolworths.financial.services.android.onecartgetstream.model.OCAuthenticationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.HomePageRequestEvent
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingAndReviewData
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.ReviewFeedback
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.request.PrepareWriteAReviewFormRequestEvent
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.response.WriteAReviewFormResponse
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Response.DyChangeAttributeResponse

interface ApiInterface {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:7200",
    )
    @GET("wfs/app/v4/user/accounts")
    fun getAccounts(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<AccountsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/account/{productOfferingId}/history")
    fun getAccountTransactionHistory(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("productOfferingId") productOfferingId: String,
    ): Call<TransactionHistoryResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/accounts")
    fun getAccountsByProductOfferingId(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("productOfferingId") productOfferingId: String,
    ): Call<AccountsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:14400",
    )
    @GET("wfs/app/v4/user/vouchers")
    fun getVouchers(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<VoucherResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/vouchers/count")
    fun getVouchersCount(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<VoucherCount>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/session")
    fun login(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body loginRequest: LoginRequest,
    ): Call<LoginResponse>

    // TODO:: Delete this request method
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/loan/request")
    fun issueLoan(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body issueLoan: IssueLoan,
    ): Call<IssueLoanResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/loan/authorise")
    fun authoriseLoan(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body authoriseLoanRequest: AuthoriseLoanRequest,
    ): Call<AuthoriseLoanResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:3600",
    )
    @GET("wfs/app/v4/mobileconfigs")
    suspend fun getConfig(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Header("appVersion") appVersion: String,
    ): ConfigResponse

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/locations")
    fun queryServiceGetStore(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("searchString") searchString: String,
    ): Call<LocationResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/locations")
    fun getStoresLocation(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("searchString") searchString: String,
        @Query("radius") radius: String,
        @Query("includeDetails") includeDetails: Boolean,
    ): Call<LocationResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/locationItems/{sku}")
    fun getStoresLocationItem(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("latitude") latitude: String,
        @Header("longitude") longitude: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path(value = "sku", encoded = false) sku: String,
        @Query("startRadius") startRadius: String,
        @Query("endRadius") endRadius: String,
        @Query("getStatus") getStatus: Boolean,
    ): Call<LocationResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/locationItems/{sku}")
    suspend fun productStoreFinder(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("latitude") latitude: String,
        @Header("longitude") longitude: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path(value = "sku", encoded = false) sku: String,
        @Query("startRadius") startRadius: String?,
        @Query("endRadius") endRadius: String?,
        @Query("getStatus") getStatus: Boolean,
    ): retrofit2.Response<LocationResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/messages")
    fun getMessages(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("pageSize") pageSize: Int,
        @Query("pageNumber") pageNumber: Int,

    ): Call<MessageResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @DELETE("wfs/app/v4/user/messages/{id}")
    fun getDeleteresponse(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("id") id: String,
    ): Call<DeleteMessageResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @PUT("wfs/app/v4/user/messages")
    fun setReadMessages(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body readMessages: MessageReadRequest,
    ): Call<ReadMessagesResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/devices")
    fun createUpdateDevice(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body device: CreateUpdateDevice,
    ): Call<CreateUpdateDeviceResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/cli/DEABanks")
    fun getDeaBanks(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("pageSize") pageSize: Int,
        @Query("pageNumber") pageNumber: Int,
    ): Call<DeaBanks>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/cli/DEABankAccountTypes")
    fun getBankAccountTypes(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("pageSize") pageSize: Int,
        @Query("pageNumber") pageNumber: Int,
    ): Call<BankAccountTypes>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/cli/application")
    fun cliCreateApplication(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body createOfferRequest: CreateOfferRequest,
    ): Call<OfferActive>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/cli/offer/{cliId}")
    fun cliUpdateApplication(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("cliId") cliId: String,
        @Body createOfferRequest: CreateOfferRequest,
    ): Call<OfferActive>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/cli/offer/{cliId}/decision")
    fun createOfferDecision(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("cliId") cliId: String,
        @Body createOfferDecision: CLIOfferDecision,
    ): Call<OfferActive>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/cli/offer/{cliId}/POI")
    fun cliSubmitPOI(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body attachments: MultipartBody.Part,
        response: Callback<CLICreateOfferResponse>,
    ): Call<CLICreateOfferResponse>

    // WOP-650 Set cacheTime to zero to allow correct status of CLI getOfferActive
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:0",
    )
    @GET("wfs/app/v4/user/cli/offerActive")
    fun getActiveOfferRequest(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("productOfferingId") productOfferingId: String,
    ): Call<OfferActive>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/cli/offer/email")
    fun cliSendEmailRquest(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<CLIEmailResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/cli/offer/bankingDetails")
    fun cliUpdateBankRequest(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body updateBankDetail: UpdateBankDetail,
    ): Call<UpdateBankDetailResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:900",
    )
    @GET("wfs/app/v4/content/promotions")
    fun getPromotions(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<PromotionsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "Accept-Encoding: gzip",
    )
    @GET("wfs/app/v4/categories")
    fun getRootCategories(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        // Optional params
        @Header("latitude") lat: Double?,
        @Header("longitude") long: Double?,
        @Query("suburbId") suburbId: String?,
        @Query("storeId") storeId: String?,
        @Query("deliveryType") deliveryType: String?,
        @Query("fulFillmentStoreId01") fulFillmentStoreId01: String?,
    ): Call<RootCategories>

    // Same as fun getRootCategories()
    // This is suspend fun with coroutines
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "Accept-Encoding: gzip",
    )
    @GET("wfs/app/v4/categories")
    suspend fun getDashCategoriesNavigation(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        // Optional params
        @Header("latitude") lat: Double?,
        @Header("longitude") long: Double?,
        @Query("suburbId") suburbId: String?,
        @Query("storeId") storeId: String?,
        @Query("deliveryType") deliveryType: String?,
        @Query("fulFillmentStoreId01") fulFillmentStoreId01: String?,
    ): retrofit2.Response<DashRootCategories>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "Accept-Encoding: gzip",
    )
    @GET("wfs/app/v4/search/department/landingPage")
    suspend fun getDashLandingDetails(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): retrofit2.Response<DashCategories>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "Accept-Encoding: gzip",
    )
    @GET("wfs/app/v4/categories/{cat}")
    fun getSubCategory(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("cat") category: String,
        @Query("version") version: String,
        // Optional params
        @Query("suburbId") suburbId: String?,
        @Query("storeId") storeId: String?,
        @Query("fulFillmentStoreId01") fulFillmentStoreId01: String?,
    ): Call<SubCategories>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:30",
        "Accept-Encoding: gzip",
    )
    @GET("wfs/app/v4/content/faq")
    fun getFAQ(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<FAQ>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:3600",
        "Accept-Encoding: gzip",
    )
    @GET("wfs/app/v4/products/{productId}")
    fun getProductDetail(
        @Header("deviceModel") deviceModel: String,
        @Header("deviceVersion") deviceVersion: String,
        @Header("os") os: String,
        @Header("network") network: String,
        @Header("apiId") apiId: String,
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sha1Password") sha1Password: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("productId") productId: String,
        @Query("sku") sku: String,
    ): Call<WProduct>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:3600",
        "Accept-Encoding: gzip",
    )
    @GET("wfs/app/v4/products/{productId}")
    fun getProductDetail(
        @Header("deviceModel") deviceModel: String,
        @Header("deviceVersion") deviceVersion: String,
        @Header("os") os: String,
        @Header("network") network: String,
        @Header("apiId") apiId: String,
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sha1Password") sha1Password: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("productId") productId: String,
        @Query("sku") sku: String,
        callback: Callback<String>,
    )

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:3600",
        "Accept-Encoding: gzip",
    )
    @GET("wfs/app/v4/products/{productId}")
    fun getProductDetail(
        @Header("deviceModel") deviceModel: String,
        @Header("deviceVersion") deviceVersion: String,
        @Header("os") os: String,
        @Header("network") network: String,
        @Header("apiId") apiId: String,
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sha1Password") sha1Password: String,
        @Header("longitude") longitude: Double,
        @Header("latitude") latitude: Double,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("productId") productId: String,
        @Query("sku") sku: String,
        callback: Callback<String>,
    )

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:14400",
    )
    @GET("wfs/app/v4/reward/cardDetails")
    fun getCardDetails(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<CardDetailsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/statements")
    fun getUserStatement(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("productOfferingId") productOfferingId: String,
        @Query("accountNumber") accountNumber: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): Call<StatementResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/statements/{docId}")
    @Streaming
    fun getStatement(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("docId") docId: String,
        @Query("productOfferingId") productOfferingId: String,
        @Query("docDesc") docDesc: String,
    ): Call<ResponseBody>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/statements")
    fun sendUserStatement(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body sendUserStatementRequest: SendUserStatementRequest,
    ): Call<SendUserStatementResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/location")
    fun getProvinces(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<ProvincesResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/cart/checkout/savedAddresses")
    fun getSavedAddresses(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<SavedAddressResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/cart/checkout/savedAddresses")
    suspend fun getSavedAddress(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): retrofit2.Response<SavedAddressResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/cart/checkout/addAddress")
    fun addAddress(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body addAddressRequestBody: AddAddressRequestBody,
    ): Call<AddAddressResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @PUT("wfs/app/v4/cart/checkout/address/{addressId}")
    fun editAddress(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("addressId") addressId: String,
        @Body addAddressRequestBody: AddAddressRequestBody,
    ): Call<AddAddressResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @DELETE("wfs/app/v4/cart/checkout/address/{addressId}")
    fun deleteAddress(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("addressId") addressId: String,
    ): Call<DeleteAddressResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/cart/checkout/changeAddress/{nickName}")
    fun changeAddress(
        @Path("nickName", encoded = true) nickName: String,
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<ChangeAddressResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/cart/checkout/confirmDeliveryAddress")
    fun getConfirmDeliveryAddressDetails(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body confirmDeliveryAddressBody: ConfirmDeliveryAddressBody,
    ): Call<ConfirmDeliveryAddressResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/cart/checkout/shippingDetails")
    fun getShippingDetails(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body shippingDetailsBody: ShippingDetailsBody,
    ): Call<ShippingDetailsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/cart/storePickupInfo")
    fun getStorePickupInfo(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body body: StorePickupInfoBody,
    ): Call<ConfirmDeliveryAddressResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/location/confirmSelection")
    fun setConfirmSelection(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body confirmSelectionRequestBody: ConfirmSelectionRequestBody,
    ): Call<ConfirmSelectionResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/location/{locationId}")
    fun getSuburbs(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("locationId") locationId: String,
    ): Call<SuburbsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/cart/suburb")
    fun setDeliveryLocationSuburb(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body suburbRequest: SetDeliveryLocationSuburbRequest,
    ): Call<SetDeliveryLocationSuburbResponse>


    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/cartV2")
    suspend fun getShoppingCartV2(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): retrofit2.Response<ShoppingCartResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/cart/{deliveryType}/itemV2")
    fun addItemToCart(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("deliveryType") deliveryType: String,
        @Body addItemToCart: MutableList<AddItemToCart>,
    ): Call<AddItemToCartResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/cart/{deliveryType}/itemV2")
    suspend fun addItemsToCart(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("deliveryType") deliveryType: String,
        @Body addItemToCart: MutableList<AddItemToCart>,
    ): retrofit2.Response<AddItemToCartResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @DELETE("wfs/app/v4/cartV2/item")
    fun removeItemFromCart(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("commerceId") commerceId: String,
    ): Call<ShoppingCartResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @DELETE("wfs/app/v4/cartV2/item")
    suspend fun removeCartItem(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("commerceId") commerceId: String,
    ): retrofit2.Response<ShoppingCartResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/cart/summary")
    fun getCartSummary(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<CartSummaryResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/cart/summary")
    suspend fun getCartsSummary(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): retrofit2.Response<CartSummaryResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @DELETE("wfs/app/v4/cartV2/item")
    suspend fun removeAllCartItems(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): retrofit2.Response<ShoppingCartResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @PUT("wfs/app/v4/cartV2/item/{commerceId}")
    suspend fun changeProductQuantityRequest(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("commerceId") commerceId: String?,
        @Body quantity: ChangeQuantity?,
    ): retrofit2.Response<ShoppingCartResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:3600",
        "Accept-Encoding: gzip",
    )
    @GET("wfs/app/v4/productsV2/{productId}")
    fun productDetail(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("productId") productId: String,
        @Query("sku") sku: String,
        @Query("suburbId") suburbId: String?,
        @Query("storeId") storeId: String?,
        @Query("deliveryType") deliveryType: String?,
        @Query("deliveryDetails") deliveryDetails: String?,
    ): Call<ProductDetailResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:3600",
        "Accept-Encoding: gzip",
    )
    @GET("wfs/app/v4/productsV2/{productId}")
    fun productDetail(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("longitude") longitude: Double,
        @Header("latitude") latitude: Double,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("productId") productId: String,
        @Query("sku") sku: String,
        @Query("suburbId") suburbId: String?,
        @Query("storeId") storeId: String?,
        @Query("deliveryType") deliveryType: String?,
        @Query("deliveryDetails") deliveryDetails: String?,
    ): Call<ProductDetailResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/list")
    fun getShoppingLists(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<ShoppingListsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/list")
    suspend fun getShoppingList(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): retrofit2.Response<ShoppingListsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("/wfs/app/recommendations/order-again")
    suspend fun getOrderAgainList(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body body: OrderAgainRequestBody
    ): retrofit2.Response<OrderAgainResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/list")
    suspend fun createNewList(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body name: CreateList,
    ): retrofit2.Response<ShoppingListsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/list")
    fun createList(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body name: CreateList,
    ): Call<ShoppingListsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/list/{id}")
    suspend fun getShoppingListItems(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("id") id: String,
    ): retrofit2.Response<ShoppingListItemsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/list/{productId}/item")
    fun addToList(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("productId") productId: String,
        @Body addToListRequest: MutableList<AddToListRequest>,
    ): Call<ShoppingListItemsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/list/{productId}/item")
    suspend fun addProductsToList(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("productId") productId: String,
        @Body addToListRequest: List<AddToListRequest>,
    ): retrofit2.Response<ShoppingListItemsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @DELETE("wfs/app/v4/list/{id}")
    fun deleteShoppingList(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("id") id: String,
    ): Call<ShoppingListsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @DELETE("wfs/app/v4/list/{listId}/item/{id}")
    fun deleteShoppingListItem(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("listId") listId: String,
        @Path("id") id: String,
        @Query("productId") productId: String,
        @Query("catalogRefId") catalogRefId: String,
    ): Call<ShoppingListItemsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/inventory/multiSku/{multipleSku}")
    fun getInventorySKU(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("multipleSku") multipleSku: String,
    ): Call<SkuInventoryResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/inventory/store/{store_id}/multiSku/{multipleSku}")
    suspend fun getInventorySKUForStore(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("store_id") store_id: String,
        @Path("multipleSku") multipleSku: String,
    ): retrofit2.Response<SkusInventoryForStoreResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/inventory/store/{store_id}/multiSku/{multipleSku}")
    fun getInventorySKUsForStore(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("store_id") store_id: String,
        @Path("multipleSku") multipleSku: String,
    ): Call<SkusInventoryForStoreResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/isninventory/multi/{store_id}/{multipleSku}")
    suspend fun fetchDashInventorySKUForStore(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("store_id") store_id: String,
        @Path("multipleSku") multipleSku: String,
    ): retrofit2.Response<SkusInventoryForStoreResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/isninventory/multi/{store_id}/{multipleSku}")
    fun fetchDashInventorySKUsForStore(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("store_id") store_id: String,
        @Path("multipleSku") multipleSku: String,
    ): Call<SkusInventoryForStoreResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/inventory/store/{store_id}/multiSku/{multipleSku}")
    suspend fun fetchInventorySKUForStore(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("store_id") store_id: String,
        @Path("multipleSku") multipleSku: String,
    ): retrofit2.Response<SkusInventoryForStoreResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:3600",
        "Accept-Encoding: gzip",
    )
    @GET("wfs/app/v4/searchSortAndFilterV2")
    fun getProducts(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("longitude") longitude: String,
        @Header("latitude") latitude: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("searchTerm") searchTerm: String,
        @Query("searchType") searchType: String,
        @Query("responseType") responseType: String,
        @Query("pageOffset") pageOffset: Int,
        @Query("pageSize") pageSize: Int,
        @Query("sortOption") sortOption: String,
        @Query("refinement") refinement: String,
        @Query("suburbId") suburbId: String?,
        @Query("storeId") storeId: String?,
        @Query("filterContent") filterContent: Boolean?,
        @Query("deliveryType") deliveryType: String?,
        @Query("deliveryDetails") deliveryDetails: String?,
    ): Call<ProductView>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:3600",
        "Accept-Encoding: gzip",
    )
    @GET("wfs/app/v4/searchSortAndFilterV2")
    fun getProductsWithoutLocation(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("searchTerm") searchTerm: String,
        @Query("searchType") searchType: String,
        @Query("responseType") responseType: String,
        @Query("pageOffset") pageOffset: Int,
        @Query("pageSize") pageSize: Int,
        @Query("sortOption") sortOption: String,
        @Query("refinement") refinement: String,
        @Header("longitude") longitude: String = "",
        @Header("latitude") latitude: String = "",
        @Query("suburbId") suburbId: String?,
        @Query("storeId") storeId: String?,
        @Query("filterContent") filterContent: Boolean?,
        @Query("deliveryType") deliveryType: String?,
        @Query("deliveryDetails") deliveryDetails: String?,
    ): Call<ProductView>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/cart/checkoutComplete")
    fun postCheckoutSuccess(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body checkoutSuccess: CheckoutSuccess,
    ): Call<Void>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/order")
    fun getOrders(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<OrdersResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/order/{id}")
    fun getOrderDetails(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("id") id: String,
    ): Call<OrderDetailsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/order/{id}")
    suspend fun addToListByOrderId(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("id") id: String,
        @Body requestBody: OrderToShoppingListRequestBody,
    ): retrofit2.Response<OrderToListReponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/order/taxInvoice/{taxNoteNumber}")
    fun getTaxInvoice(

        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("taxNoteNumber") taxNoteNumber: String,
    ): Call<OrderTaxInvoiceResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/creditCardToken")
    fun getCreditCardToken(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<CreditCardTokenResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/accounts/storecard/blockStoreCard/{productOfferingId}")
    fun blockStoreCard(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("productOfferingId") productOfferingId: String,
        @Body blockCardRequestBody: BlockCardRequestBody,
    ): Call<BlockMyCardResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/accounts/storecard/otp")
    fun getLinkNewCardOTP(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("otpMethod") otpMethod: String,
    ): Call<LinkNewCardOTP>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/vnd.appserver.api.v2+json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/accounts/storecard/linkStoreCard")
    fun linkStoreCard(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body linkStoreCard: LinkStoreCard,
    ): Call<LinkNewCardResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/accounts/storecard/cards")
    fun getStoreCards(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("lat") latitude: Double?,
        @Query("lon") longitude: Double?,
        @Body getStoreCardsRequestBody: StoreCardsRequestBody,
    ): Call<StoreCardsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/accounts/storecard/unblockStoreCard/{productOfferingId}")
    fun unblockStoreCard(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("productOfferingId") productOfferingId: String,
        @Body requestBody: UnblockStoreCardRequestBody,
    ): Call<UnblockStoreCardResponse>

    @GET("wfs/app/v4/user/locations/geofence")
    fun getStoresForNPC(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("searchString") searchString: String,
        @Query("npc") npc: Boolean?,
    ): Call<LocationResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/order/cancelOrder")
    fun queryServiceCancelOrder(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("orderId") orderId: String,
    ): Call<CancelOrderResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/absa/activateCardAccount")
    fun activateCreditCard(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body requestBody: CreditCardActivationRequestBody,
    ): Call<CreditCardActivationResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/accounts/otp/retrieve")
    fun retrieveOTP(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("otpMethod") otpMethod: String,
        @Query("productOfferingId") productOfferingId: String,
    ): Call<RetrieveOTPResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/accounts/otp/validate/{productOfferingId}")
    fun validateOTP(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body requestBody: ValidateOTPRequest,
        @Path("productOfferingId") productOfferingId: String,
    ): Call<ValidateOTPResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/event/{featureName}/{appScreen}")
    fun postEvent(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("featureName") featureName: String,
        @Path("appScreen") appScreen: String,
    ): Call<Response>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/payments/payu/methods")
    fun getPaymentPAYUMethod(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<PaymentMethodsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/payments/payu/pay")
    fun postPayUpPay(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body payUPay: PayUPay,
    ): Call<PayUResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/payments/payu/result")
    fun getPaymentPayUResult(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("customer") customer: String,
        @Query("payment_id") payment_id: String,
        @Query("charge_id") charge_id: String,
        @Query("status") status: String,
        @Query("productOfferingId") productOfferingID: String,
    ): Call<PayUPayResultResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/location/validateSelectedSuburb/{suburbId}")
    fun validateSelectedSuburb(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("suburbId") suburbId: String,
        @Query("isStore") isStore: Boolean,
    ): Call<ValidateSelectedSuburbResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/locationItems/validateLocation")
    fun validateLocation(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("placeId") placeId: String,
        @Query("inventoryCheck") inventoryCheck: Boolean,
    ): Call<ValidateLocationResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/locationItems/validateStoreInventory")
   suspend fun callValidateStoreInventory(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("placeId") placeId: String,
        @Query("storeId") storeId: String,
    ): retrofit2.Response<ValidateStoreResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/locationItems/validateLocation")
    suspend fun validatePlace(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("placeId") placeId: String,
        @Query("inventoryCheck") inventoryCheck: Boolean,
    ): retrofit2.Response<ValidateLocationResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @DELETE("wfs/app/v4/payments/payu/methods/{paymenToken}")
    fun payURemovePaymentMethod(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("paymenToken") paymenToken: String,
    ): Call<DeleteResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/cartV2/applyVouchers")
    fun applyVouchers(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body vouchersList: List<SelectedVoucher>,
    ): Call<ShoppingCartResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/accounts/cardDelivery/status")
    fun cardDeliveryStatus(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("envelopeReference") envelopeReference: String,
        @Query("productOfferingId") productOfferingId: String,
    ): Call<CreditCardDeliveryStatusResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/accounts/cardDelivery/possibleAddress")
    fun possibleAddress(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("searchPhrase") searchPhrase: String,
        @Query("envelopeNumber") envelopeNumber: String,
        @Query("productOfferingId") productOfferingId: String,
    ): Call<PossibleAddressResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/accounts/cardDelivery/timeslots")
    fun availableTimeSlots(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("envelopeNumber") envelopeNumber: String,
        @Query("productOfferingId") productOfferingId: String,
        @Query("x") x: String,
        @Query("y") y: String,
        @Query("shipByDate") shipByDate: String,
    ): Call<AvailableTimeSlotsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @PUT("wfs/app/v4/accounts/cardDelivery/scheduleDelivery")
    fun scheduleDelivery(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("productOfferingId") productOfferingId: String,
        @Query("envelopeNumber") envelopeNumber: String,
        @Query("schedule") schedule: Boolean,
        @Query("bookingReference") bookingReference: String,
        @Body requestBody: ScheduleDeliveryRequest,
    ): Call<CreditCardDeliveryStatusResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/cartV2/applyPromoCode")
    fun applyPromoCode(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body couponClaimCode: CouponClaimCode,
    ): Call<ShoppingCartResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/cartV2/removePromoCode")
    suspend fun removePromoCode(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body couponClaimCode: CouponClaimCode,
    ): retrofit2.Response<ShoppingCartResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/productsV2/content/{contentId}")
    fun getSizeGuideContent(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("contentId") contentId: String,
    ): Call<SizeGuideResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/otp/retrieve")
    fun getLinkDeviceOTP(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Header("sessionToken") sessionToken: String,
        @Query("otpMethod") otpMethod: String,
    ): Call<RetrieveOTPResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @PUT("wfs/app/v4/user/device/{deviceIdentityId}")
    fun changePrimaryDeviceApi(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("deviceIdentityId") deviceIdentityId: String,
        @Query("otp") otp: String?,
        @Query("otpMethod") otpMethod: String?,
    ): Call<ViewAllLinkedDeviceResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/device")
    fun linkDeviceApi(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceName") deviceName: String,
        @Body linkDeviceValidateBody: LinkDeviceBody,
        @Query("otp") otp: String?,
        @Query("otpMethod") otpMethod: String?,
    ): Call<LinkedDeviceResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:14400",
    )
    @GET("wfs/app/v4/user/device")
    fun getAllLinkedDevices(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Header("sessionToken") sessionToken: String,
    ): Call<ViewAllLinkedDeviceResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @DELETE("wfs/app/v4/user/device/{deviceIdentityId}")
    fun deleteDevice(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("deviceIdentityId") deviceIdentityId: String,
        @Query("newPrimaryDeviceIdentityId") newPrimaryDeviceIdentityId: String?,
        @Query("otp") otp: String?,
        @Query("otpMethod") otpMethod: String?,
    ): Call<ViewAllLinkedDeviceResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/accounts/storecard/email")
    fun confirmStoreCardEmail(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body storeCardEmailConfirmBody: StoreCardEmailConfirmBody,
    ): Call<GenericResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/survey")
    fun getVocSurvey(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Query("triggerEvent") triggerEvent: String,
    ): Call<SurveyDetailsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/survey/{surveyId}/replies")
    fun submitVocSurveyReplies(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Path("surveyId") surveyId: Long,
        @Body surveyReplies: SurveyRepliesBody,
    ): Call<Void>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/survey/optout")
    fun optOutVocSurvey(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Body optOutBody: SurveyOptOutBody,
    ): Call<Void>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/cart/checkout/submittedOrder")
    fun getSubmittedOrder(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<SubmittedOrderResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/accounts/termsAndConditions/BPI")
    fun getBPITermsAndConditionsInfo(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("productGroupCode") productGroupCode: String,
    ): Call<BPITermsConditionsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/accounts/termsAndConditions/BPI")
    fun emailBPITermsAndConditions(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body bpiBody: BPIBody,
    ): Call<GenericResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/{insuranceType}/optin")
    fun postInsuranceLeadGenOptIn(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("insuranceType") insuranceType: String,
        @Body insuranceTypeOptInBody: InsuranceTypeOptInBody,
    ): Call<GenericResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/accounts/collections/checkEligibility")
    suspend fun fetchCollectionCheckEligibility(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Query("productGroupCode") productGroupCode: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): EligibilityPlanResponse

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/email/{emailId}")
    suspend fun queryServiceNotifyCardNotYetReceived(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Path("emailId") emailId: String,
        @Body body: Any,
    ): Response


    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/cartV2/confirmLocation")
    fun confirmLocation(

        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body confirmLocationRequest: ConfirmLocationRequest,
    ): Call<ConfirmDeliveryAddressResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )

    @POST("wfs/app/v4/locationItems/saveLocation")
    fun saveLocation(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body saveAddressLocationRequest: SaveAddressLocationRequest,
    ): Call<GenericResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @DELETE("wfs/app/v4/user/deleteProfile")
    fun deleteAccount(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
    ): Call<DeleteAccountResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:3600",
        "Accept-Encoding: gzip",
    )
    @GET("/retail-ratings-reviews/app/v1")
    fun getRatingNReview(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("productId") productId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Call<RatingAndReviewData>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:3600",
        "Accept-Encoding: gzip",
    )
    @GET("/retail-ratings-reviews/app/v1")
    suspend fun getMoreReviews(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("productId") productId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("sort") sort: String?,
        @Query("refinement") refinement: String?,

    ): RatingAndReviewData

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
        "cacheTime:3600",
        "Accept-Encoding: gzip",
    )
    @POST("/retail-ratings-reviews/app/v1/submitFeedback")
    suspend fun submitFeedback(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body reviewFeedback: ReviewFeedback,

    ): GenericResponse

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/dash/chat/authenticate")
    fun authenticateOneCart(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<OCAuthenticationResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/dash/chat/authenticate")
    suspend fun getOCAuth(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): retrofit2.Response<OCAuthenticationResponse>

    @GET("wfs/app/v4/order/previousorder")
    suspend fun getLastDashOrder(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): retrofit2.Response<LastOrderDetailsResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/recommendations")
    suspend fun recommendation(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body recommendationRequest: RecommendationRequest,
    ): retrofit2.Response<RecommendationResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/featureEnablement")
    fun getFeatureEnablement(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<FeatureEnablementModel>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @GET("wfs/app/v4/user/insurance/products")
    fun getPetInsurance(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<PetInsuranceModel>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Media-Type: application/json",
    )
    @POST("wfs/app/v4/user/appGuid")
    fun getAppGUID(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body appGUIDRequestModel: AppGUIDRequestModel,
    ): Call<AppGUIDModel>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("wfs/app/dynamicYield/chooseVariation")
    suspend fun dynamicYieldHomePage(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body dyHomePageRequestEvent: HomePageRequestEvent
    ) : retrofit2.Response<DynamicYieldChooseVariationResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("wfs/app/dynamicYield/reportEvent")
    suspend fun dynamicYieldChangeAttribute(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body dyPrepareChangeAttributeRequestEvent: PrepareChangeAttributeRequestEvent
    ) : retrofit2.Response<DyChangeAttributeResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("wfs/app/v4/cart/get-substitution")
    suspend fun getSubstitution(
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("productId") productId: String?,
    ): retrofit2.Response<ProductSubstitution>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("wfs/app/v4/searchSortAndFilterV2")
    suspend fun getSearchedProducts(

            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("longitude") longitude: String,
            @Header("latitude") latitude: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("searchTerm") searchTerm: String,
            @Query("searchType") searchType: String,
            @Query("responseType") responseType: String,
            @Query("pageOffset") pageOffset: Int,
            @Query("pageSize") pageSize: Int,
            @Query("sortOption") sortOption: String,
            @Query("refinement") refinement: String,
            @Query("suburbId") suburbId: String?,
            @Query("storeId") storeId: String?,
            @Query("filterContent") filterContent: Boolean?,
            @Query("deliveryType") deliveryType: String?,
            @Query("deliveryDetails") deliveryDetails: String?
    ): ProductView


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("wfs/app/v4/searchSortAndFilterV2")
    suspend fun getSearchedProductsWithoutLocation(

            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("searchTerm") searchTerm: String,
            @Query("searchType") searchType: String,
            @Query("responseType") responseType: String,
            @Query("pageOffset") pageOffset: Int,
            @Query("pageSize") pageSize: Int,
            @Query("sortOption") sortOption: String,
            @Query("refinement") refinement: String,
            @Header("longitude") longitude: String = "",
            @Header("latitude") latitude: String = "",
            @Query("suburbId") suburbId: String?,
            @Query("storeId") storeId: String?,
            @Query("filterContent") filterContent: Boolean?,
            @Query("deliveryType") deliveryType: String?,
            @Query("deliveryDetails") deliveryDetails: String?
    ): ProductView

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("wfs/app/v4/cart/add-substitution")
    suspend fun addSubstitution(
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body addSubstitutionRequest: AddSubstitutionRequest
    ): retrofit2.Response<AddSubstitutionResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("wfs/app/recommendations/kibo-substitution-list")
    suspend fun getKiboProductsFromResponse(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Body addSubstitutionRequest: KiboProductRequest
    ): retrofit2.Response<KiboProductResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("wfs/app/v4/isninventory/multi/{store_id}/{multipleSku}")
    suspend fun fetchKiboInventorySKUForStore(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("store_id") store_id: String,
        @Path("multipleSku") multipleSku: String,
        @Query("substitution") substitution: Boolean): retrofit2.Response<SkusInventoryForStoreResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("wfs/app/reviews/submitReview")
    suspend fun writeAReviewForm(
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("productId") productId: String?,
        @Body prepareWriteAReviewFormRequestEvent: PrepareWriteAReviewFormRequestEvent
    ) : retrofit2.Response<WriteAReviewFormResponse>

    @GET("wfs/app/v4/user/locations/payinstore")
    fun verifyUserIsInStore(
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): Call<UserLocationResponse>

}
