package za.co.woolworths.financial.services.android.models.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.*
import za.co.absa.openbankingapi.woolworths.integration.dto.PayUResponse
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.credit_card_activation.CreditCardActivationRequestBody
import za.co.woolworths.financial.services.android.models.dto.credit_card_activation.CreditCardActivationResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.*
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkDeviceBody
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkDeviceValidateBody
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.dto.npc.*
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPRequest
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPResponse
import za.co.woolworths.financial.services.android.models.dto.pma.DeleteResponse
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.models.dto.size_guide.SizeGuideResponse
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementResponse
import za.co.woolworths.financial.services.android.models.dto.statement.StatementResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardResponse
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyRepliesBody
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyOptOutBody
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CouponClaimCode
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.SelectedVoucher

interface ApiInterface {

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:7200")
    @GET("user/accounts")
    fun getAccounts(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<AccountsResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("user/account/{productOfferingId}/history")
    fun getAccountTransactionHistory(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("productOfferingId") productOfferingId: String): Call<TransactionHistoryResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("user/accounts")
    fun getAccountsByProductOfferingId(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("productOfferingId") productOfferingId: String): Call<AccountsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:14400")
    @GET("user/vouchers")
    fun getVouchers(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<VoucherResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("user/vouchers/count")
    fun getVouchersCount(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<VoucherCount>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("user/session")
    fun login(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body loginRequest: LoginRequest): Call<LoginResponse>


    //TODO:: Delete this request method
    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("user/loan/request")
    fun issueLoan(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body issueLoan: IssueLoan): Call<IssueLoanResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("user/loan/authorise")
    fun authoriseLoan(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body authoriseLoanRequest: AuthoriseLoanRequest): Call<AuthoriseLoanResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600")
    @GET("mobileconfigs")
    suspend fun getConfig(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Header("appVersion") appVersion: String
    ):ConfigResponse

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("user/locations")
    fun queryServiceGetStore(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("lat") lat: String,
            @Query("lon") lon: String,
            @Query("searchString") searchString: String
    ): Call<LocationResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("user/locations")
    fun getStoresLocation(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("lat") lat: String,
            @Query("lon") lon: String,
            @Query("searchString") searchString: String,
            @Query("radius") radius: String,
            @Query("includeDetails") includeDetails: Boolean
    ): Call<LocationResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("locationItems/{sku}")
    fun getStoresLocationItem(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("latitude") latitude: String,
            @Header("longitude") longitude: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path(value = "sku", encoded = false) sku: String,
            @Query("startRadius") startRadius: String,
            @Query("endRadius") endRadius: String,
            @Query("getStatus") getStatus: Boolean): Call<LocationResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("user/messages")
    fun getMessages(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("pageSize") pageSize: Int,
            @Query("pageNumber") pageNumber: Int

    ): Call<MessageResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @DELETE("user/messages/{id}")
    fun getDeleteresponse(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("id") id: String
    ): Call<DeleteMessageResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @PUT("user/messages")
    fun setReadMessages(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body readMessages: MessageReadRequest
    ): Call<ReadMessagesResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("user/devices")
    fun createUpdateDevice(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body device: CreateUpdateDevice
    ): Call<CreateUpdateDeviceResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("user/cli/DEABanks")
    fun getDeaBanks(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("pageSize") pageSize: Int,
            @Query("pageNumber") pageNumber: Int
    ): Call<DeaBanks>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("user/cli/DEABankAccountTypes")
    fun getBankAccountTypes(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("pageSize") pageSize: Int,
            @Query("pageNumber") pageNumber: Int
    ): Call<BankAccountTypes>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("user/cli/application")
    fun cliCreateApplication(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body createOfferRequest: CreateOfferRequest): Call<OfferActive>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("user/cli/offer/{cliId}")
    fun cliUpdateApplication(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("cliId") cliId: String,
            @Body createOfferRequest: CreateOfferRequest): Call<OfferActive>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("user/cli/offer/{cliId}/decision")
    fun createOfferDecision(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("cliId") cliId: String,
            @Body createOfferDecision: CLIOfferDecision): Call<OfferActive>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("user/cli/offer/{cliId}/POI")
    fun cliSubmitPOI(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body attachments: MultipartBody.Part, response: Callback<CLICreateOfferResponse>): Call<CLICreateOfferResponse>

    //WOP-650 Set cacheTime to zero to allow correct status of CLI getOfferActive
    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:0")
    @GET("user/cli/offerActive")
    fun getActiveOfferRequest(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("productOfferingId") productOfferingId: String): Call<OfferActive>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("user/cli/offer/email")
    fun cliSendEmailRquest(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<CLIEmailResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("user/cli/offer/bankingDetails")
    fun cliUpdateBankRequest(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body updateBankDetail: UpdateBankDetail): Call<UpdateBankDetailResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:900")
    @GET("content/promotions")
    fun getPromotions(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<PromotionsResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "Accept-Encoding: gzip")
    @GET("categories")
    fun getRootCategories(
            @Header("osVersion") osVersion: String,
            @Header("apiId") apiId: String,
            @Header("os") os: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("apiKey") userAgent: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            //Optional params
            @Header("latitude") lat: Double?,
            @Header("longitude") long: Double?,
            @Query("suburbId") suburbId: String?,
            @Query("storeId") storeId: String?,
            @Query("fulFillmentStoreId01") fulFillmentStoreId01: String?
    ): Call<RootCategories>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "Accept-Encoding: gzip")
    @GET("categories/{cat}")
    fun getSubCategory(
            @Header("osVersion") osVersion: String,
            @Header("apiId") apiId: String,
            @Header("os") os: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("apiKey") apiKey: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("cat") category: String,
            @Query("version") version: String,
            //Optional params
            @Query("suburbId") suburbId: String?,
            @Query("storeId") storeId: String?,
            @Query("fulFillmentStoreId01") fulFillmentStoreId01: String?
    ): Call<SubCategories>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:30", "Accept-Encoding: gzip")
    @GET("content/faq")
    fun getFAQ(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<FAQ>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("products/{productId}")
    fun getProductDetail(
            @Header("osVersion") osVersion: String,
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
            @Query("sku") sku: String): Call<WProduct>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("products/{productId}")
    fun getProductDetail(
            @Header("osVersion") osVersion: String,
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
            callback: Callback<String>)

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("products/{productId}")
    fun getProductDetail(
            @Header("osVersion") osVersion: String,
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
            callback: Callback<String>)

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:14400")
    @GET("reward/cardDetails")
    fun getCardDetails(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<CardDetailsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("user/statements")
    fun getUserStatement(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("productOfferingId") productOfferingId: String,
            @Query("accountNumber") accountNumber: String,
            @Query("startDate") startDate: String,
            @Query("endDate") endDate: String): Call<StatementResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("user/statements/{docId}")
    @Streaming
    fun getStatement(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("docId") docId: String,
            @Query("productOfferingId") productOfferingId: String,
            @Query("docDesc") docDesc: String): Call<ResponseBody>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("user/statements")
    fun sendUserStatement(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body sendUserStatementRequest: SendUserStatementRequest): Call<SendUserStatementResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("location")
    fun getProvinces(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<ProvincesResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("savedAddresses")
    fun getSavedAddresses(): Call<SavedAddressResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("addAddress")
    fun addAddress(@Body addAddressRequestBody: AddAddressRequestBody): Call<AddAddressResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @PATCH("address/{id}")
    fun updateAddress(@Body addAddressRequestBody: AddAddressRequestBody, @Path("id") id: String): Call<AddAddressResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @DELETE("address/{addressId}")
    fun deleteAddress(@Path("addressId") addressId: String): Call<DeleteAddressResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("changeAddress/{nickName}")
    fun changeAddress(@Path("nickName") nickName: String): Call<ChangeAddressResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("availableDeliverySlots")
    fun getAvailableDeliverySlots(): Call<AvailableDeliverySlotsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("location/{locationId}")
    fun getSuburbs(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("locationId") locationId: String
    ): Call<SuburbsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("cart/suburb")
    fun setDeliveryLocationSuburb(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body suburbRequest: SetDeliveryLocationSuburbRequest): Call<SetDeliveryLocationSuburbResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("cartV2")
    fun getShoppingCart(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
    ): Call<ShoppingCartResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("cart/item")
    fun addItemToCart(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body addItemToCart: MutableList<AddItemToCart>): Call<AddItemToCartResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @DELETE("cartV2/item")
    fun removeItemFromCart(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("commerceId") commerceId: String): Call<ShoppingCartResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("cart/summary")
    fun getCartSummary(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<CartSummaryResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @DELETE("cartV2/item")
    fun removeAllCartItems(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<ShoppingCartResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @PUT("cartV2/item/{commerceId}")
    fun changeQuantityRequest(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("commerceId") commerceId: String,
            @Body quantity: ChangeQuantity): Call<ShoppingCartResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("productsV2/{productId}")
    fun productDetail(
            @Header("osVersion") osVersion: String,
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
            @Query("suburbId") suburbId: String?,
            @Query("storeId") storeId: String?
    ): Call<ProductDetailResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("productsV2/{productId}")
    fun productDetail(
            @Header("osVersion") osVersion: String,
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
            @Query("suburbId") suburbId: String?,
            @Query("storeId") storeId: String?
    ): Call<ProductDetailResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("list")
    fun getShoppingLists(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<ShoppingListsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("list")
    fun createList(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body name: CreateList): Call<ShoppingListsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("list/{id}")
    fun getShoppingListItems(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("id") id: String): Call<ShoppingListItemsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("list/{productId}/item")
    fun addToList(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("productId") productId: String,
            @Body addToListRequest: MutableList<AddToListRequest>): Call<ShoppingListItemsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @DELETE("list/{id}")
    fun deleteShoppingList(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("id") id: String): Call<ShoppingListsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @DELETE("list/{listId}/item/{id}")
    fun deleteShoppingListItem(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("listId") listId: String,
            @Path("id") id: String,
            @Query("productId") productId: String,
            @Query("catalogRefId") catalogRefId: String): Call<ShoppingListItemsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("inventory/multiSku/{multipleSku}")
    fun getInventorySKU(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("multipleSku") multipleSku: String): Call<SkuInventoryResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("inventory/store/{store_id}/multiSku/{multipleSku}")
    fun getInventorySKUForStore(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("store_id") store_id: String,
            @Path("multipleSku") multipleSku: String): Call<SkusInventoryForStoreResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("searchSortAndFilterV2")
    fun getProducts(
            @Header("osVersion") osVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("os") os: String,
            @Header("network") network: String,
            @Header("apiId") apiId: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sha1Password") sha1Password: String,
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
            @Query("storeId") storeId: String?): Call<ProductView>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("searchSortAndFilterV2")
    fun getProductsWithoutLocation(
            @Header("osVersion") osVersion: String,
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
            @Query("storeId") storeId: String?): Call<ProductView>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("cart/checkoutComplete")
    fun postCheckoutSuccess(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body checkoutSuccess: CheckoutSuccess): Call<Void>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("order")
    fun getOrders(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<OrdersResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("order/{id}")
    fun getOrderDetails(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("id") id: String): Call<OrderDetailsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("order/{id}")
    fun addOrderToList(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("id") id: String,
            @Body requestBody: OrderToShoppingListRequestBody): Call<OrderToListReponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("order/taxInvoice/{taxNoteNumber}")
    fun getTaxInvoice(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("taxNoteNumber") taxNoteNumber: String): Call<OrderTaxInvoiceResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("user/creditCardToken")
    fun getCreditCardToken(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<CreditCardTokenResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("accounts/storecard/blockStoreCard/{productOfferingId}")
    fun blockStoreCard(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("productOfferingId") productOfferingId: String,
            @Body blockCardRequestBody: BlockCardRequestBody): Call<BlockMyCardResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("accounts/storecard/otp")
    fun getLinkNewCardOTP(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("otpMethod") otpMethod: String): Call<LinkNewCardOTP>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("accounts/storecard/linkStoreCard")
    fun linkStoreCard(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body linkStoreCard: LinkStoreCard): Call<LinkNewCardResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("accounts/storecard/cards")
    fun getStoreCards(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("lat") latitude: Double?,
            @Query("lon") longitude: Double?,
            @Body getStoreCardsRequestBody: StoreCardsRequestBody): Call<StoreCardsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("accounts/storecard/unblockStoreCard/{productOfferingId}")
    fun unblockStoreCard(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("productOfferingId") productOfferingId: String,
            @Body requestBody: UnblockStoreCardRequestBody): Call<UnblockStoreCardResponse>

    @GET("user/locations/geofence")
    fun getStoresForNPC(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("lat") lat: String,
            @Query("lon") lon: String,
            @Query("searchString") searchString: String,
            @Query("npc") npc: Boolean?
    ): Call<LocationResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("order/cancelOrder")
    fun queryServiceCancelOrder(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("orderId") orderId: String): Call<CancelOrderResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("absa/activateCardAccount")
    fun activateCreditCard(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body requestBody: CreditCardActivationRequestBody): Call<CreditCardActivationResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("accounts/otp/retrieve")
    fun retrieveOTP(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("otpMethod") otpMethod: String,
            @Query("productOfferingId") productOfferingId: String): Call<RetrieveOTPResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("accounts/otp/validate/{productOfferingId}")
    fun validateOTP(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body requestBody: ValidateOTPRequest,
            @Path("productOfferingId") productOfferingId: String): Call<ValidateOTPResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("event/{featureName}/{appScreen}")
    fun postEvent(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("featureName") featureName: String,
            @Path("appScreen") appScreen: String): Call<Response>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("payments/payu/methods")
    fun getPaymentPAYUMethod(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String): Call<PaymentMethodsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("payments/payu/pay")
    fun postPayUpPay(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body payUPay: PayUPay): Call<PayUResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("payments/payu/result")
    fun getPaymentPayUResult(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("customer") customer: String,
            @Query("payment_id") payment_id: String,
            @Query("charge_id") charge_id: String,
            @Query("status") status: String,
            @Query("productOfferingId") productOfferingID: String): Call<PayUPayResultResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("location/validateSelectedSuburb/{suburbId}")
    fun validateSelectedSuburb(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("suburbId") suburbId: String,
            @Query("isStore") isStore: Boolean): Call<ValidateSelectedSuburbResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @DELETE("payments/payu/methods/{paymenToken}")
    fun payURemovePaymentMethod(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("paymenToken") paymenToken: String
    ): Call<DeleteResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("cartV2/applyVouchers")
    fun applyVouchers(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body vouchersList: List<SelectedVoucher>): Call<ShoppingCartResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("accounts/cardDelivery/status")
    fun cardDeliveryStatus(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("envelopeReference") envelopeReference: String,
            @Query("productOfferingId") productOfferingId: String): Call<CreditCardDeliveryStatusResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("accounts/cardDelivery/possibleAddress")
    fun possibleAddress(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("searchPhrase") searchPhrase: String,
            @Query("envelopeNumber") envelopeNumber: String,
            @Query("productOfferingId") productOfferingId: String): Call<PossibleAddressResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("accounts/cardDelivery/timeslots")
    fun availableTimeSlots(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("envelopeNumber") envelopeNumber: String,
            @Query("productOfferingId") productOfferingId: String,
            @Query("x") x: String,
            @Query("y") y: String,
            @Query("shipByDate") shipByDate: String): Call<AvailableTimeSlotsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @PUT("accounts/cardDelivery/scheduleDelivery")
    fun scheduleDelivery(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Query("productOfferingId") productOfferingId: String,
            @Query("envelopeNumber") envelopeNumber: String,
            @Query("schedule") schedule: Boolean,
            @Query("bookingReference") bookingReference: String,
            @Body requestBody: ScheduleDeliveryRequest): Call<CreditCardDeliveryStatusResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("cartV2/applyPromoCode")
    fun applyPromoCode(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body couponClaimCode: CouponClaimCode): Call<ShoppingCartResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("cartV2/removePromoCode")
    fun removePromoCode(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body couponClaimCode: CouponClaimCode): Call<ShoppingCartResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("productsV2/content/{contentId}")
    fun getSizeGuideContent(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("contentId") contentId: String
    ): Call<SizeGuideResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("user/otp/retrieve")
    fun getLinkDeviceOTP(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Header("sessionToken") sessionToken: String,
            @Query("otpMethod") otpMethod: String
    ): Call<RetrieveOTPResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @PUT("user/device/{deviceIdentityId}")
    fun changePrimaryDeviceApi(
        @Header("apiId") apiId: String,
        @Header("sha1Password") sha1Password: String,
        @Header("deviceVersion") deviceVersion: String,
        @Header("deviceModel") deviceModel: String,
        @Header("network") network: String,
        @Header("os") os: String,
        @Header("osVersion") osVersion: String,
        @Header("userAgent") userAgent: String,
        @Header("userAgentVersion") userAgentVersion: String,
        @Header("sessionToken") sessionToken: String,
        @Header("deviceIdentityToken") deviceIdentityToken: String,
        @Path("deviceIdentityId") deviceIdentityId: String,
        @Query("otp") otp: String?,
        @Query("otpMethod") otpMethod: String?
    ): Call<ViewAllLinkedDeviceResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("user/device")
    fun linkDeviceApi(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceName") deviceName: String,
            @Body linkDeviceValidateBody: LinkDeviceBody,
            @Query("otp") otp: String?,
            @Query("otpMethod") otpMethod: String?): Call<LinkedDeviceResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:14400")
    @GET("user/device")
    fun getAllLinkedDevices(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Header("sessionToken") sessionToken: String
    ): Call<ViewAllLinkedDeviceResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @DELETE("user/device/{deviceIdentityId}")
    fun deleteDevice(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Path("deviceIdentityId") deviceIdentityId: String,
            @Query("newPrimaryDeviceIdentityId") newPrimaryDeviceIdentityId: String?,
            @Query("otp") otp: String?,
            @Query("otpMethod") otpMethod: String?
    ): Call<ViewAllLinkedDeviceResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("accounts/storecard/email")
    fun confirmStoreCardEmail(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("deviceIdentityToken") deviceIdentityToken: String,
            @Body storeCardEmailConfirmBody: StoreCardEmailConfirmBody): Call<GenericResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("survey")
    fun getVocSurvey(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Query("triggerEvent") triggerEvent: String
    ): Call<SurveyDetailsResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("survey/{surveyId}/replies")
    fun submitVocSurveyReplies(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Path("surveyId") surveyId: Long,
            @Body surveyReplies: SurveyRepliesBody
    ): Call<Void>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("survey/optout")
    fun optOutVocSurvey(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Body optOutBody: SurveyOptOutBody
    ): Call<Void>
}
