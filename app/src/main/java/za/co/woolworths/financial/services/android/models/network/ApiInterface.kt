package za.co.woolworths.financial.services.android.models.network

import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.*
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.chat.*
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementResponse
import za.co.woolworths.financial.services.android.models.dto.statement.StatementResponse
import java.util.*

interface ApiInterface {

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:28800")
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
            @Header("sessionToken") sessionToken: String): Call<AccountsResponse>


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
            @Path("productOfferingId") productOfferingId: String): Call<TransactionHistoryResponse>

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
            @Header("sessionToken") sessionToken: String): Call<VoucherResponse>

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
            @Body authoriseLoanRequest: AuthoriseLoanRequest): Call<AuthoriseLoanResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600")
    @GET("mobileconfigs")
    fun getConfig(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
            @Header("appVersion") appVersion: String
    ): Call<ConfigResponse>

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
            @Query("lat") lat: String,
            @Query("lon") lon: String,
            @Query("searchString") searchString: String,
            @Query("radius") radius: String
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
            @Header("sessionToken") sessionToken: String): Call<CLIEmailResponse>

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
            @Header("sessionToken") sessionToken: String): Call<PromotionsResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
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
            @Header("sessionToken") sessionToken: String): Call<RootCategories>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
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
            @Path("cat") category: String): Call<SubCategories>


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
            @Header("sessionToken") sessionToken: String): Call<FAQ>


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
            @Header("sessionToken") sessionToken: String): Call<CardDetailsResponse>

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
            @Path("docId") docId: String,
            @Query("productOfferingId") productOfferingId: String,
            @Query("docDesc") docDesc: String): Call<retrofit2.Response<ResponseBody>>

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
            @Header("sessionToken") sessionToken: String
    ): Call<ProvincesResponse>

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
            @Body suburbRequest: SetDeliveryLocationSuburbRequest): Call<SetDeliveryLocationSuburbResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("cart")
    fun getShoppingCart(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String
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
            @Body addItemToCart: MutableList<AddItemToCart>): Call<AddItemToCartResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @DELETE("cart/item")
    fun removeItemFromCart(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String,
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
            @Header("sessionToken") sessionToken: String): Call<CartSummaryResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @DELETE("cart/item")
    fun removeAllCartItems(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("sessionToken") sessionToken: String): Call<ShoppingCartResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @PUT("cart/item/{commerceId}")
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
            @Path("commerceId") commerceId: String,
            @Body quantity: ChangeQuantity): Call<ShoppingCartResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("products/{productId}")
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
            @Path("productId") productId: String,
            @Query("sku") sku: String): Call<ProductDetailResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("products/{productId}")
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
            @Path("productId") productId: String,
            @Query("sku") sku: String): Call<ProductDetailResponse>

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
            @Header("sessionToken") sessionToken: String): Call<ShoppingListsResponse>

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
            @Path("store_id") store_id: String,
            @Path("multipleSku") multipleSku: String): Call<SkusInventoryForStoreResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("searchSortAndFilter")
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
            @Header("longitude") longitude: Double,
            @Header("latitude") latitude: Double,
            @Header("sessionToken") sessionToken: String,
            @Query("searchTerm") searchTerm: String,
            @Query("searchType") searchType: String,
            @Query("responseType") responseType: String,
            @Query("pageOffset") pageOffset: Int,
            @Query("pageSize") pageSize: Int,
            @Query("sortOption") sortOption: String,
            @Query("refinement") refinement: String): Call<ProductView>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:3600", "Accept-Encoding: gzip")
    @GET("searchSortAndFilter")
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
            @Query("searchTerm") searchTerm: String,
            @Query("searchType") searchType: String,
            @Query("responseType") responseType: String,
            @Query("pageOffset") pageOffset: Int,
            @Query("pageSize") pageSize: Int,
            @Query("sortOption") sortOption: String,
            @Query("refinement") refinement: String): Call<ProductView>

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
            @Header("sessionToken") sessionToken: String): Call<OrdersResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json", "cacheTime:120")
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
            @Header("sessionToken") sessionToken: String): Call<CreditCardTokenResponse>


    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("accounts/npc/blockStoreCard/{productOfferingId}")
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
            @Path("productOfferingId") productOfferingId: String,
            @Body blockCardRequestBody: BlockCardRequestBody): Call<BlockMyCardResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("chat/pollAgentsAvailable")
    fun pollAgentsAvailable(
            @Header("apiId") apiId: String,
            @Header("sha1Password") sha1Password: String,
            @Header("deviceVersion") deviceVersion: String,
            @Header("deviceModel") deviceModel: String,
            @Header("network") network: String,
            @Header("os") os: String,
            @Header("osVersion") osVersion: String,
            @Header("userAgent") userAgent: String,
            @Header("userAgentVersion") userAgentVersion: String,
            @Header("sessionToken") sessionToken: String): Observable<AgentsAvailableResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("chat/createChatSession")
    fun createChatSession(
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
            @Body createChatSession: CreateChatSession): Call<CreateChatSessionResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @GET("chat/pollChatSessionState/{chatId}")
    fun pollChatSessionState(
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
            @Path("chatId") chatId: String): Observable<PollChatSessionStateResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("chat/sendChatMessage/{chatId}")
    fun sendChatMessage(
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
            @Path("chatId") chatId: String,
            @Body sendChatMessage: SendChatMessage): Call<SendChatMessageResponse>

    @Headers("Content-Type: application/json", "Accept: application/json", "Media-Type: application/json")
    @POST("chat/userTyping/{chatId}")
    fun userTyping(
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
            @Path("chatId") chatId: String,
            @Body emptyBody: Objects): Call<UserTypingResponse>

}