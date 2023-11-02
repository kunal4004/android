package za.co.woolworths.financial.services.android.models.network

import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import za.co.absa.openbankingapi.woolworths.integration.dto.PayUResponse
import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressRequestBody
import za.co.woolworths.financial.services.android.checkout.service.network.AddAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.ChangeAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.ConfirmDeliveryAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.ConfirmSelectionRequestBody
import za.co.woolworths.financial.services.android.checkout.service.network.ConfirmSelectionResponse
import za.co.woolworths.financial.services.android.checkout.service.network.DeleteAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.service.network.ShippingDetailsBody
import za.co.woolworths.financial.services.android.checkout.service.network.ShippingDetailsResponse
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanRequest
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanResponse
import za.co.woolworths.financial.services.android.models.dto.BPITermsConditionsResponse
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes
import za.co.woolworths.financial.services.android.models.dto.CLIEmailResponse
import za.co.woolworths.financial.services.android.models.dto.CLIOfferDecision
import za.co.woolworths.financial.services.android.models.dto.CancelOrderResponse
import za.co.woolworths.financial.services.android.models.dto.CardDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse
import za.co.woolworths.financial.services.android.models.dto.ChangeQuantity
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.DashRootCategories
import za.co.woolworths.financial.services.android.models.dto.DeaBanks
import za.co.woolworths.financial.services.android.models.dto.DeleteAccountResponse
import za.co.woolworths.financial.services.android.models.dto.DeleteMessageResponse
import za.co.woolworths.financial.services.android.models.dto.FAQ
import za.co.woolworths.financial.services.android.models.dto.IssueLoan
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.LoginRequest
import za.co.woolworths.financial.services.android.models.dto.LoginResponse
import za.co.woolworths.financial.services.android.models.dto.MessageReadRequest
import za.co.woolworths.financial.services.android.models.dto.MessageResponse
import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.OrderTaxInvoiceResponse
import za.co.woolworths.financial.services.android.models.dto.OrderToListReponse
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.models.dto.PayUPay
import za.co.woolworths.financial.services.android.models.dto.PayUPayResultRequest
import za.co.woolworths.financial.services.android.models.dto.PayUPayResultResponse
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.PromotionsResponse
import za.co.woolworths.financial.services.android.models.dto.ProvincesResponse
import za.co.woolworths.financial.services.android.models.dto.ReadMessagesResponse
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.dto.SubCategories
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetailResponse
import za.co.woolworths.financial.services.android.models.dto.VoucherCount
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse
import za.co.woolworths.financial.services.android.models.dto.account.AppGUIDModel
import za.co.woolworths.financial.services.android.models.dto.account.AppGUIDRequestType
import za.co.woolworths.financial.services.android.models.dto.account.FeatureEnablementModel
import za.co.woolworths.financial.services.android.models.dto.account.FicaModel
import za.co.woolworths.financial.services.android.models.dto.account.PetInsuranceModel
import za.co.woolworths.financial.services.android.models.dto.account.getRequestBody
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
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardResponse
import za.co.woolworths.financial.services.android.models.dto.npc.LinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPRequest
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPResponse
import za.co.woolworths.financial.services.android.models.dto.pma.DeleteResponse
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.dto.size_guide.SizeGuideResponse
import za.co.woolworths.financial.services.android.models.dto.statement.GetStatement
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementRequest
import za.co.woolworths.financial.services.android.models.dto.statement.SendUserStatementResponse
import za.co.woolworths.financial.services.android.models.dto.statement.StatementResponse
import za.co.woolworths.financial.services.android.models.dto.statement.UserStatement
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardResponse
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyOptOutBody
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyRepliesBody
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CouponClaimCode
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.SelectedVoucher
import za.co.woolworths.financial.services.android.onecartgetstream.model.OCAuthenticationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingAndReviewData
import za.co.woolworths.financial.services.android.dynamicyield.data.response.getResponse.DynamicYieldChooseVariationResponse
import za.co.woolworths.financial.services.android.endlessaisle.service.network.UserLocationRequestBody
import za.co.woolworths.financial.services.android.endlessaisle.service.network.UserLocationResponse
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.HomePageRequestEvent
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.request.PrepareWriteAReviewFormRequestEvent
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.response.WriteAReviewFormResponse
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Response.DyChangeAttributeResponse
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent
import java.net.URLEncoder

open class OneAppService(
    private val appContextProvider: AppContextProviderInterface = AppContextProviderImpl(),
    retrofitApiProvider: RetrofitApiProviderInterface = RetrofitApiProviderImpl()
) : RetrofitConfig(appContextProvider, retrofitApiProvider) {

    companion object {
        var forceNetworkUpdate: Boolean = false
    }

    fun login(loginRequest: LoginRequest): Call<LoginResponse> {
        return mApiInterface.login(
            "",
            "", getSessionToken(), getDeviceIdentityToken(), loginRequest
        )
    }

    fun getAccounts(): Call<AccountsResponse> {
        return mApiInterface.getAccounts(
            "", "",
            getSessionToken(), getDeviceIdentityToken()
        )
    }

    fun authoriseLoan(authoriseLoanRequest: AuthoriseLoanRequest): Call<AuthoriseLoanResponse> {
        return mApiInterface.authoriseLoan(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), authoriseLoanRequest
        )
    }

    fun getAccountTransactionHistory(productOfferingId: String): Call<TransactionHistoryResponse> {
        return mApiInterface.getAccountTransactionHistory(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), productOfferingId
        )
    }

    fun getVouchers(): Call<VoucherResponse> {
        return mApiInterface.getVouchers(
            "", "", getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    fun getVouchersCount(): Call<VoucherCount> {
        return mApiInterface.getVouchersCount(
            "", "", getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    fun queryServiceGetStore(
        latitude: Double? = 0.0,
        longitude: Double? = 0.0,
        searchTextField: String
    ): Call<LocationResponse> {
        return mApiInterface.queryServiceGetStore(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            latitude.toString(),
            longitude.toString(),
            searchTextField
        )
    }

    fun getStoresForNPC(
        latitude: Double? = 0.0,
        longitude: Double? = 0.0,
        searchTextField: String,
        npc: Boolean?
    ): Call<LocationResponse> {
        return mApiInterface.getStoresForNPC(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            latitude.toString(),
            longitude.toString(),
            searchTextField,
            npc
        )
    }

    fun getLocationsItem(
        sku: String,
        startRadius: String,
        endRadius: String
    ): Call<LocationResponse> {
        val loc = getMyLocation()
        return if (startRadius != null && startRadius == "") {
            //This should never happen for now
            mApiInterface.getStoresLocationItem(
                "",
                "",
                loc.latitude.toString(),
                loc.longitude.toString(),
                getSessionToken(),
                getDeviceIdentityToken(),
                sku,
                startRadius,
                endRadius,
                true
            )
        } else {
            mApiInterface.getStoresLocationItem(
                "",
                "",
                loc.latitude.toString(),
                loc.longitude.toString(),
                getSessionToken(),
                getDeviceIdentityToken(),
                sku,
                startRadius,
                endRadius,
                true
            )
        }
    }

    suspend fun productStoreFinder(
        sku: String,
        startRadius: String?,
        endRadius: String?
    ): retrofit2.Response<LocationResponse> {
        val loc = getMyLocation()
        return withContext(Dispatchers.IO) {
            if ("" == startRadius) {
                //This should never happen for now
                mApiInterface.productStoreFinder(
                    "", "", loc.latitude.toString(),
                    loc.longitude.toString(), getSessionToken(), getDeviceIdentityToken(), sku,
                    startRadius, endRadius, true
                )
            } else {
                mApiInterface.productStoreFinder(
                    "", "", loc.latitude.toString(),
                    loc.longitude.toString(), getSessionToken(), getDeviceIdentityToken(), sku,
                    startRadius, endRadius, true
                )
            }
        }
    }

    fun getMessagesResponse(pageSize: Int, pageNumber: Int): Call<MessageResponse> {
        return mApiInterface.getMessages(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), pageSize, pageNumber
        )
    }

    fun cliCreateApplication(offerRequest: CreateOfferRequest): Call<OfferActive> {
        return mApiInterface.cliCreateApplication(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), offerRequest
        )
    }

    fun cliUpdateApplication(offerRequest: CreateOfferRequest, cliId: String): Call<OfferActive> {
        return mApiInterface.cliUpdateApplication(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), cliId, offerRequest
        )
    }

    fun createOfferDecision(
        createOfferDecision: CLIOfferDecision,
        cliId: String
    ): Call<OfferActive> {
        return mApiInterface.createOfferDecision(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), cliId, createOfferDecision
        )
    }

    fun getDeaBanks(): Call<DeaBanks> {
        return mApiInterface.getDeaBanks(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), 0, 0
        )
    }

    fun getBankAccountTypes(): Call<BankAccountTypes> {
        return mApiInterface.getBankAccountTypes(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), 0, 0
        )
    }

    fun getActiveOfferRequest(productOfferingId: String): Call<OfferActive> {
        return mApiInterface.getActiveOfferRequest(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), productOfferingId
        )
    }

    fun getDeleteMessagesResponse(id: String): Call<DeleteMessageResponse> {
        return mApiInterface.getDeleteresponse(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), id
        )
    }

    fun getReadMessagesResponse(readMessages: MessageReadRequest): Call<ReadMessagesResponse> {
        return mApiInterface.setReadMessages(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), readMessages
        )
    }

    fun cliEmailResponse(): Call<CLIEmailResponse> {
        return mApiInterface.cliSendEmailRquest(getSessionToken(), getDeviceIdentityToken())
    }

    fun cliUpdateBankDetail(updateBankDetail: UpdateBankDetail): Call<UpdateBankDetailResponse> {
        return mApiInterface.cliUpdateBankRequest(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), updateBankDetail
        )
    }

    fun getResponseOnCreateUpdateDevice(device: CreateUpdateDevice): Call<CreateUpdateDeviceResponse> {
        return mApiInterface.createUpdateDevice(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), device
        )
    }

    fun issueLoan(issueLoan: IssueLoan): Call<IssueLoanResponse> {
        return mApiInterface.issueLoan(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), issueLoan
        )
    }

    fun addToList(
        addToListRequest: MutableList<AddToListRequest>,
        listId: String
    ): Call<ShoppingListItemsResponse> {
        return mApiInterface.addToList(
            getSessionToken(), getDeviceIdentityToken(), listId,
            addToListRequest
        )
    }

    suspend fun addProductsToList(
        addToListRequest: List<AddToListRequest>,
        listId: String
    ): retrofit2.Response<ShoppingListItemsResponse> = mApiInterface.addProductsToList(
            getSessionToken(), getDeviceIdentityToken(), listId,
            addToListRequest
        )

    fun getPromotions(): Call<PromotionsResponse> {
        return mApiInterface.getPromotions(
            "", "", getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    fun getRootCategory(
        locationEnabled: Boolean,
        location: Location?,
        deliveryType: String?
    ): Call<RootCategories> {
        val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()
        // Pass storeId value of 01 fulfillment type
        val fulfillmentStoreId01 = Utils.retrieveStoreId("01")
        var locationCord = location
        if (!locationEnabled) {
            locationCord = null
        }

        return mApiInterface.getRootCategories(
            getSessionToken(),
            getDeviceIdentityToken(),
            locationCord?.latitude,
            locationCord?.longitude,
            suburbId,
            storeId,
            deliveryType,
            fulfillmentStoreId01
        )
    }

    suspend fun getDashCategoryNavigation(location: Location?): retrofit2.Response<DashRootCategories> {
        return withContext(Dispatchers.IO) {
            val storeId =  WoolworthsApplication.getDashBrowsingValidatePlaceDetails()?.onDemand?.storeId
                ?: WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.storeId ?: ""

            mApiInterface.getDashCategoriesNavigation(
                getSessionToken(),
                getDeviceIdentityToken(),
                location?.latitude,
                location?.longitude,
                null,
                storeId,
                Delivery.DASH.type,
                storeId
            )
        }
    }

    suspend fun getDashLandingDetails(): retrofit2.Response<DashCategories> {
        return withContext(Dispatchers.IO) {
            mApiInterface.getDashLandingDetails(getSessionToken(), getDeviceIdentityToken())
        }
    }

    fun getSubCategory(category_id: String, version: String): Call<SubCategories> {
        val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()
        val fulfillmentStoreId01 = Utils.retrieveStoreId("01")
        return mApiInterface.getSubCategory(
            getSessionToken(), getDeviceIdentityToken(),
            category_id, version, suburbId, storeId, fulfillmentStoreId01
        )
    }

    fun getProvinces(): Call<ProvincesResponse> {
        return mApiInterface.getProvinces(
            "", "", getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    suspend fun getSavedAddress() : retrofit2.Response<SavedAddressResponse>{
        return withContext(Dispatchers.IO){
            mApiInterface.getSavedAddress("","",getSessionToken(), getDeviceIdentityToken())
        }
    }

    fun addAddress(addAddressRequestBody: AddAddressRequestBody): Call<AddAddressResponse> {
        return mApiInterface.addAddress(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            addAddressRequestBody,
        )
    }

    fun editAddress(
        addAddressRequestBody: AddAddressRequestBody,
        addressId: String
    ): Call<AddAddressResponse> {
        return mApiInterface.editAddress(
            "",
            "",
            getSessionToken(), getDeviceIdentityToken(), addressId, addAddressRequestBody
        )
    }

    fun deleteAddress(addressId: String): Call<DeleteAddressResponse> {
        return mApiInterface.deleteAddress(getSessionToken(), getDeviceIdentityToken(), addressId)
    }

    fun changeAddress(nickName: String): Call<ChangeAddressResponse> {
        return mApiInterface.changeAddress(
            nickName, "", "", getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    fun getShippingDetails(body: ShippingDetailsBody): Call<ShippingDetailsResponse> {
        return mApiInterface.getShippingDetails(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), body
        )
    }

    fun getStorePickupInfo(body: StorePickupInfoBody): Call<ConfirmDeliveryAddressResponse> {
        return mApiInterface.getStorePickupInfo(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), body
        )
    }

    fun setConfirmSelection(confirmSelectionRequestBody: ConfirmSelectionRequestBody): Call<ConfirmSelectionResponse> {
        return mApiInterface.setConfirmSelection(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), confirmSelectionRequestBody
        )
    }

    fun getCartSummary(): Call<CartSummaryResponse> {
        return mApiInterface.getCartSummary(
            "", "", getSessionToken(),
            getDeviceIdentityToken()
        )
    }


    fun getProducts(requestParams: ProductsRequestParams): Call<ProductView> {
        val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()

        val (deliveryType, deliveryDetails) = when {
            !requestParams.sendDeliveryDetailsParams -> {
                Pair(null, null)
            }
            else ->
                Pair(
                    KotlinUtils.browsingDeliveryType?.type,
                    KotlinUtils.getDeliveryDetails(requestParams.isUserBrowsing)
                )
        }

        val pageSize = AppConfigSingleton.searchApiSettings?.let { it.pageSize } ?: Utils.PAGE_SIZE

        return if (Utils.isLocationEnabled(appContextProvider.appContext())) {
            mApiInterface.getProducts(
                "",
                "",
                "",
                "",
                getSessionToken(),
                getDeviceIdentityToken(),
                requestParams.searchTerm,
                requestParams.searchType.value,
                requestParams.responseType.value,
                requestParams.pageOffset,
                pageSize,
                requestParams.sortOption,
                requestParams.refinement,
                suburbId = suburbId,
                storeId = storeId,
                filterContent = requestParams.filterContent,
                deliveryType = deliveryType,
                deliveryDetails = deliveryDetails
            )
        } else {
            mApiInterface.getProductsWithoutLocation(
                "",
                "",
                getSessionToken(),
                getDeviceIdentityToken(),
                requestParams.searchTerm,
                requestParams.searchType.value,
                requestParams.responseType.value,
                requestParams.pageOffset,
                pageSize,
                requestParams.sortOption,
                requestParams.refinement,
                suburbId = suburbId,
                storeId = storeId,
                filterContent = requestParams.filterContent,
                deliveryType = deliveryType,
                deliveryDetails = deliveryDetails
            )
        }
    }

     fun getSuburbOrStoreId(): Pair<String?, String?> {
        val suburbId: String? = null
        val storeId: String? = null
        return Pair(suburbId, storeId)
    }

    fun getFAQ(): Call<FAQ> {
        return mApiInterface.getFAQ(
            "", "", getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    fun getCardDetails(): Call<CardDetailsResponse> {
        return mApiInterface.getCardDetails(getSessionToken(), getDeviceIdentityToken())
    }

    fun getStatementResponse(statement: UserStatement): Call<StatementResponse> {
        return mApiInterface.getUserStatement(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), statement.productOfferingId, statement.accountNumber
                ?: "", statement.startDate, statement.endDate
        )
    }

    fun sendStatementRequest(statement: SendUserStatementRequest): Call<SendUserStatementResponse> {
        return mApiInterface.sendUserStatement(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), statement
        )
    }

    fun addItemToCart(addToCart: MutableList<AddItemToCart>): Call<AddItemToCartResponse> {

        val deliveryType = KotlinUtils.getPreferredDeliveryType()?.type ?: ""

        return mApiInterface.addItemToCart(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), deliveryType, addToCart
        )
    }


    suspend fun addItemsToCart(addToCart: MutableList<AddItemToCart>): retrofit2.Response<AddItemToCartResponse> {
        return withContext(Dispatchers.IO) {
            val deliveryType = KotlinUtils.getPreferredDeliveryType()?.type ?: ""

            mApiInterface.addItemsToCart(
                "", "", getSessionToken(),
                getDeviceIdentityToken(), deliveryType, addToCart
            )
        }
    }

    fun getShoppingCart(): Call<ShoppingCartResponse> {
        return mApiInterface.getShoppingCart(getSessionToken(), getDeviceIdentityToken())
    }

    suspend fun getShoppingCartV2() : retrofit2.Response<ShoppingCartResponse>{
        return withContext(Dispatchers.IO){
            mApiInterface.getShoppingCartV2(getSessionToken(), getDeviceIdentityToken())
        }
    }

    suspend fun changeProductQuantityRequest(changeQuantity: ChangeQuantity?): retrofit2.Response<ShoppingCartResponse>{
        return mApiInterface.changeProductQuantityRequest(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            changeQuantity?.commerceId,
            changeQuantity
        )
    }

    fun removeCartItem(commerceId: String): Call<ShoppingCartResponse> {
        return mApiInterface.removeItemFromCart(
            getSessionToken(),
            getDeviceIdentityToken(),
            commerceId
        )
    }

    suspend fun removeSingleCartItem(commerceId: String) : retrofit2.Response<ShoppingCartResponse>{
        return withContext(Dispatchers.IO){
            mApiInterface.removeCartItem(getSessionToken(), getDeviceIdentityToken(), commerceId)
        }
    }

    suspend fun removeAllCartItems(): retrofit2.Response<ShoppingCartResponse> {
        return mApiInterface.removeAllCartItems(getSessionToken(), getDeviceIdentityToken())
    }

    fun productDetail(
        productId: String,
        skuId: String,
        isUserBrowsing: Boolean = false
    ): Call<ProductDetailResponse> {
        val loc = getMyLocation()
        val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()
        val deliveryType =
            if (isUserBrowsing) KotlinUtils.browsingDeliveryType?.type ?: Delivery.STANDARD.type
            else KotlinUtils.getDeliveryType()?.deliveryType ?: Delivery.STANDARD.type
        return if (Utils.isLocationEnabled(appContextProvider.appContext())) {
            mApiInterface.productDetail(
                "",
                "",
                loc.longitude,
                loc.latitude,
                getSessionToken(),
                getDeviceIdentityToken(),
                productId,
                skuId,
                suburbId,
                storeId,
                deliveryType = deliveryType,
                deliveryDetails = KotlinUtils.getDeliveryDetails(isUserBrowsing)
            )
        } else {
            mApiInterface.productDetail(
                "", "",
                getSessionToken(), getDeviceIdentityToken(),
                productId, skuId, suburbId, storeId, deliveryType = deliveryType,
                deliveryDetails = KotlinUtils.getDeliveryDetails(isUserBrowsing)
            )
        }
    }

    fun getRatingNReview(productId: String,limit: Int, offset: Int): Call<RatingAndReviewData> {
        return mApiInterface.getRatingNReview(getSessionToken(), getDeviceIdentityToken(),productId, limit, offset)
    }

    fun getShoppingLists(): Call<ShoppingListsResponse> {
        return mApiInterface.getShoppingLists(getSessionToken(), getDeviceIdentityToken())
    }
    suspend fun getShoppingList(): retrofit2.Response<ShoppingListsResponse> =
        mApiInterface.getShoppingList(getSessionToken(), getDeviceIdentityToken())

    suspend fun createNewList(listName: CreateList): retrofit2.Response<ShoppingListsResponse> =
        withContext(Dispatchers.IO){
            mApiInterface.createNewList(getSessionToken(), getDeviceIdentityToken(), listName)
    }

    suspend fun getShoppingListItems(listId: String): retrofit2.Response<ShoppingListItemsResponse> {
        return withContext(Dispatchers.IO) {
            mApiInterface.getShoppingListItems(
                getSessionToken(),
                getDeviceIdentityToken(),
                listId
            )
        }
    }

    fun deleteShoppingList(listId: String): Call<ShoppingListsResponse> {
        return mApiInterface.deleteShoppingList(getSessionToken(), getDeviceIdentityToken(), listId)
    }

    fun deleteShoppingListItem(
        listId: String,
        id: String,
        productId: String,
        catalogRefId: String
    ): Call<ShoppingListItemsResponse> {
        return mApiInterface.deleteShoppingListItem(
            getSessionToken(), getDeviceIdentityToken(), listId, id, productId,
            catalogRefId
        )
    }

    suspend fun getInventorySkusForStore(
        store_id: String,
        multipleSku: String,
        isUserBrowsing: Boolean
    ): retrofit2.Response<SkusInventoryForStoreResponse> {
        return withContext(Dispatchers.IO) {
            if ((isUserBrowsing && Delivery.DASH.type == KotlinUtils.browsingDeliveryType?.type) ||
                (!isUserBrowsing && Delivery.DASH.type == KotlinUtils.getDeliveryType()?.deliveryType)
            ) {
                mApiInterface.fetchDashInventorySKUForStore(
                    getSessionToken(),
                    getDeviceIdentityToken(),
                    store_id,
                    multipleSku
                )
            } else
                mApiInterface.getInventorySKUForStore(
                    getSessionToken(),
                    getDeviceIdentityToken(),
                    store_id,
                    multipleSku
                )
        }
    }

    fun getInventorySkuForStore(
        store_id: String,
        multipleSku: String,
        isUserBrowsing: Boolean
    ): Call<SkusInventoryForStoreResponse> {
        return if ((isUserBrowsing && Delivery.DASH.type == KotlinUtils.browsingDeliveryType?.type) ||
            (!isUserBrowsing && Delivery.DASH.type == KotlinUtils.getDeliveryType()?.deliveryType)
        ) {
            mApiInterface.fetchDashInventorySKUsForStore(
                getSessionToken(),
                getDeviceIdentityToken(),
                store_id,
                multipleSku
            )
        } else
            mApiInterface.getInventorySKUsForStore(
                getSessionToken(),
                getDeviceIdentityToken(),
                store_id,
                multipleSku
            )
    }

    suspend fun fetchInventorySkuForStore(
        store_id: String,
        multipleSku: String,
        isUserBrowsing: Boolean
    ): retrofit2.Response<SkusInventoryForStoreResponse> {
        return withContext(Dispatchers.IO) {
            if ((isUserBrowsing && Delivery.DASH.type == KotlinUtils.browsingDeliveryType?.type) ||
                (!isUserBrowsing && Delivery.DASH.type == KotlinUtils.getDeliveryType()?.deliveryType)
            ) {
                mApiInterface.fetchDashInventorySKUForStore(
                    getSessionToken(),
                    getDeviceIdentityToken(),
                    store_id,
                    multipleSku
                )
            }
            else {
                mApiInterface.fetchInventorySKUForStore(
                    getSessionToken(),
                    getDeviceIdentityToken(),
                    store_id,
                    multipleSku
                )
            }
        }
    }

    fun getPDFResponse(getStatement: GetStatement): Call<ResponseBody> {
        return mApiInterface.getStatement(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            getStatement.docId,
            getStatement.productOfferingId,
            getStatement.docDesc
        )
    }

    fun getOrders(): Call<OrdersResponse> {
        return mApiInterface.getOrders(getSessionToken(), getDeviceIdentityToken())
    }

    fun getOrderDetails(orderId: String): Call<OrderDetailsResponse> {
        return mApiInterface.getOrderDetails(getSessionToken(), getDeviceIdentityToken(), orderId)
    }

    suspend fun addToListByOrderId(
        orderId: String,
        body: OrderToShoppingListRequestBody
    ): retrofit2.Response<OrderToListReponse> = mApiInterface.addToListByOrderId(
        getSessionToken(), getDeviceIdentityToken(), orderId, body
    ).execute()

    fun getOrderTaxInvoice(taxNoteNumber: String): Call<OrderTaxInvoiceResponse> {
        return mApiInterface.getTaxInvoice(
            getSessionToken(),
            getDeviceIdentityToken(),
            taxNoteNumber
        )
    }

    fun getCreditCardToken(): Call<CreditCardTokenResponse> {
        return mApiInterface.getCreditCardToken("", "", getSessionToken(), getDeviceIdentityToken())
    }

    fun postBlockMyCard(
        blockCardRequestBody: BlockCardRequestBody,
        productOfferingId: String
    ): Call<BlockMyCardResponse> {
        return mApiInterface.blockStoreCard(
            getOsVersion(), "", getSessionToken(),
            getDeviceIdentityToken(), productOfferingId, blockCardRequestBody
        )
    }

    fun getStoreCards(storeCardsRequestBody: StoreCardsRequestBody): Call<StoreCardsResponse> {
        val lastSavedLocation = Utils.getLastSavedLocation()
        return mApiInterface.getStoreCards(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            lastSavedLocation?.latitude,
            lastSavedLocation?.longitude,
            storeCardsRequestBody
        )
    }

    fun getLinkNewCardOTP(otpMethodType: OTPMethodType): Call<LinkNewCardOTP> {
        return mApiInterface.getLinkNewCardOTP(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), otpMethodType.name
        )
    }

    fun linkStoreCardRequest(linkStoreCard: LinkStoreCard): Call<LinkNewCardResponse> {
        return mApiInterface.linkStoreCard(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), linkStoreCard
        )
    }

    fun unblockStoreCard(
        productOfferingId: String,
        requestBody: UnblockStoreCardRequestBody
    ): Call<UnblockStoreCardResponse> {
        return mApiInterface.unblockStoreCard(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), productOfferingId, requestBody
        )
    }

    fun activateCreditCardRequest(requestBody: CreditCardActivationRequestBody): Call<CreditCardActivationResponse> {
        return mApiInterface.activateCreditCard(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), requestBody
        )
    }

    fun retrieveOTP(
        otpMethodType: OTPMethodType,
        productOfferingId: String
    ): Call<RetrieveOTPResponse> {
        return mApiInterface.retrieveOTP(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), otpMethodType.name, productOfferingId
        )
    }

    fun validateOTP(
        validateOTPRequest: ValidateOTPRequest,
        productOfferingId: String
    ): Call<ValidateOTPResponse> {
        return mApiInterface.validateOTP(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), validateOTPRequest, productOfferingId
        )
    }

    fun queryServiceCancelOrder(orderId: String): Call<CancelOrderResponse> {
        return mApiInterface.queryServiceCancelOrder(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), orderId
        )
    }

    fun getCreditCardDeliveryStatus(
        envelopeReference: String,
        productOfferingId: String
    ): Call<CreditCardDeliveryStatusResponse> {
        return mApiInterface.cardDeliveryStatus(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), envelopeReference, productOfferingId
        )
    }

    fun getPossibleAddress(
        searchPhrase: String,
        productOfferingId: String,
        envelopeNumber: String
    ): Call<PossibleAddressResponse> {
        return mApiInterface.possibleAddress(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            searchPhrase,
            envelopeNumber,
            productOfferingId
        )
    }

    fun getAvailableTimeSlots(
        envelopeReference: String,
        productOfferingId: String,
        x: String,
        y: String,
        shipByDate: String
    ): Call<AvailableTimeSlotsResponse> {
        return mApiInterface.availableTimeSlots(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            envelopeReference,
            productOfferingId,
            x,
            y,
            shipByDate
        )
    }

    fun postScheduleDelivery(
        productOfferingId: String,
        envelopeNumber: String,
        schedule: Boolean,
        bookingReference: String,
        scheduleDeliveryRequest: ScheduleDeliveryRequest
    ): Call<CreditCardDeliveryStatusResponse> {
        return mApiInterface.scheduleDelivery(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            productOfferingId,
            envelopeNumber,
            schedule,
            bookingReference,
            scheduleDeliveryRequest
        )
    }

    fun queryServicePostEvent(featureName: String?, appScreen: String?): Call<Response> {
        return mApiInterface.postEvent(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), featureName
                ?: "", appScreen ?: ""
        )
    }

    fun queryServicePayUMethod(): Call<PaymentMethodsResponse> {
        return mApiInterface.getPaymentPAYUMethod(
            "", "",
            getSessionToken(), getDeviceIdentityToken()
        )
    }

    fun queryServicePostPayU(payUPay: PayUPay): Call<PayUResponse> {
        return mApiInterface.postPayUpPay(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), payUPay
        )
    }

    fun queryServicePaymentResult(request: PayUPayResultRequest): Call<PayUPayResultResponse> {
        return mApiInterface.getPaymentPayUResult(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            request.customer,
            request.payment_id,
            request.charge_id,
            request.status,
            request.productOfferingID
        )
    }

    fun validateSelectedSuburb(
        suburbId: String,
        isStore: Boolean
    ): Call<ValidateSelectedSuburbResponse> {
        return mApiInterface.validateSelectedSuburb(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), suburbId, isStore
        )
    }

    suspend fun getValidateLocation(placeId: String): retrofit2.Response<ValidateLocationResponse> {
        return withContext(Dispatchers.IO) {
            mApiInterface.validatePlace(
                "",
                "",
                getSessionToken(),
                getDeviceIdentityToken(),
                placeId,
                false
            )
        }
    }

    fun applyVouchers(vouchers: List<SelectedVoucher>): Call<ShoppingCartResponse> {
        return mApiInterface.applyVouchers(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), vouchers
        )
    }

    fun applyPromoCode(couponClaimCode: CouponClaimCode): Call<ShoppingCartResponse> {
        return mApiInterface.applyPromoCode(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), couponClaimCode
        )
    }

    suspend fun removePromoCode(couponClaimCode: CouponClaimCode): retrofit2.Response<ShoppingCartResponse> {
        return mApiInterface.removePromoCode(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), couponClaimCode
        )
    }

    fun queryServicePayURemovePaymentMethod(paymentToken: String): Call<DeleteResponse> {
        return mApiInterface.payURemovePaymentMethod(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), paymentToken
        )
    }

    fun getSizeGuideContent(contentId: String): Call<SizeGuideResponse> {
        return mApiInterface.getSizeGuideContent(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), contentId
        )
    }

    fun getAccountsByProductOfferingId(productOfferingId: String): Call<AccountsResponse> {
        return mApiInterface.getAccountsByProductOfferingId(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), productOfferingId
        )
    }

    fun getLinkDeviceOtp(otpMethod: String): Call<RetrieveOTPResponse> {
        return mApiInterface.getLinkDeviceOTP(
            "",
            "",
            getDeviceIdentityToken(),
            getSessionToken(),
            otpMethod
        )
    }

    fun changePrimaryDeviceApi(
        deviceIdentityId: String,
        otp: String?,
        otpMethod: String?
    ): Call<ViewAllLinkedDeviceResponse> {
        return mApiInterface.changePrimaryDeviceApi(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            deviceIdentityId,
            otp,
            otpMethod
        )
    }

    fun linkDeviceApi(
        deviceName: String,
        appInstanceId: String,
        location: String?,
        primaryDevice: Boolean,
        firebaseToken: String,
        tokenProvider: String,
        otp: String?,
        otpMethod: String?
    ): Call<LinkedDeviceResponse> {
        val body =
            LinkDeviceBody(appInstanceId, location, primaryDevice, firebaseToken, tokenProvider)

        return mApiInterface.linkDeviceApi(
            "",
            "",
            getSessionToken(),
            URLEncoder.encode(deviceName, "UTF-8"),
            body,
            otp,
            otpMethod
        )
    }

    fun getAllLinkedDevices(isForced: Boolean): Call<ViewAllLinkedDeviceResponse> {
        forceNetworkUpdate = isForced
        return mApiInterface.getAllLinkedDevices(
            "",
            "",
            getDeviceIdentityToken(),
            getSessionToken()
        )

    }

    fun deleteDevice(
        deviceIdentityId: String,
        newPrimaryDeviceIdentityId: String?,
        otp: String?,
        otpMethod: String?
    ): Call<ViewAllLinkedDeviceResponse> {
        return mApiInterface.deleteDevice(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            deviceIdentityId,
            newPrimaryDeviceIdentityId,
            otp,
            otpMethod
        )
    }

    fun confirmStoreCardEmail(body: StoreCardEmailConfirmBody): Call<GenericResponse> {
        return mApiInterface.confirmStoreCardEmail(
            "", "",
            getSessionToken(), getDeviceIdentityToken(), body
        )
    }

    open fun getVocSurvey(triggerEvent: VocTriggerEvent): Call<SurveyDetailsResponse> {
        return mApiInterface.getVocSurvey(
            userAgent = "",
            userAgentVersion = "",
            sessionToken = getSessionToken(),
            triggerEvent = triggerEvent.value
        )
    }

    open fun submitVocSurveyReplies(
        surveyDetails: SurveyDetails,
        surveyAnswers: HashMap<Long, SurveyAnswer>
    ): Call<Void> {
        return mApiInterface.submitVocSurveyReplies(
            userAgent = "",
            userAgentVersion = "",
            sessionToken = getSessionToken(),
            surveyId = surveyDetails.id,
            surveyReplies = SurveyRepliesBody(
                surveyId = surveyDetails.id,
                appInstanceId = Utils.getUniqueDeviceID(),
                participantReplies = surveyAnswers.values.toList()
            )
        )
    }

    open fun optOutVocSurvey(): Call<Void> {
        return mApiInterface.optOutVocSurvey(
            userAgent = "",
            userAgentVersion = "",
            sessionToken = getSessionToken(),
            optOutBody = SurveyOptOutBody(
                appInstanceId = Utils.getUniqueDeviceID()
            )
        )
    }

    fun getSubmittedOrder(): Call<SubmittedOrderResponse> {
        return mApiInterface.getSubmittedOrder(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    fun getBPITermsAndConditionsInfo(productGroupCode: String): Call<BPITermsConditionsResponse> {
        return mApiInterface.getBPITermsAndConditionsInfo(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            productGroupCode
        )
    }

    fun emailBPITermsAndConditions(productGroupCode: String): Call<GenericResponse> {
        return mApiInterface.emailBPITermsAndConditions(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            BPIBody(productGroupCode)
        )
    }

    fun postInsuranceLeadGenOptIn(
        insuranceType: String,
        insuranceTypeOptInBody: InsuranceTypeOptInBody
    ): Call<GenericResponse> {
        return mApiInterface.postInsuranceLeadGenOptIn(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            insuranceType,
            insuranceTypeOptInBody
        )
    }

    fun getFicaResponse(): Call<FicaModel> {
        return mApiInterface.getFica(
            getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    fun getConfirmDeliveryAddressDetails(body: ConfirmLocationRequest): Call<ConfirmDeliveryAddressResponse> {
        return mApiInterface.confirmLocation(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            body
        )
    }

    fun deleteAccount(): Call<DeleteAccountResponse> {
        return mApiInterface.deleteAccount(
            "",
            "",
            getSessionToken()
        )
    }

    fun authenticateOneCart(): Call<OCAuthenticationResponse> {
        return mApiInterface.authenticateOneCart(getSessionToken(), getDeviceIdentityToken())
    }

    suspend fun getOCAuthData(): retrofit2.Response<OCAuthenticationResponse> {
        return withContext(Dispatchers.IO) {
            mApiInterface.getOCAuth(getSessionToken(), getDeviceIdentityToken())
        }
    }

    suspend fun getLastDashOrder(): retrofit2.Response<LastOrderDetailsResponse> {
        return withContext(Dispatchers.IO) {
            mApiInterface.getLastDashOrder(getSessionToken(), getDeviceIdentityToken())
        }
    }

    suspend fun recommendation(recommendationRequest: RecommendationRequest): retrofit2.Response<RecommendationResponse> {
        return withContext(Dispatchers.IO) {
            mApiInterface.recommendation(
                getSessionToken(),
                getDeviceIdentityToken(),
                recommendationRequest
            )
        }
    }

    fun getFeatureEnablementResponse(): Call<FeatureEnablementModel> {
        return mApiInterface.getFeatureEnablement(
            getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    fun getPetInsuranceResponse(): Call<PetInsuranceModel> {
        return mApiInterface.getPetInsurance(
            getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    fun getAppGUIDResponse(appGUIDRequestType: AppGUIDRequestType): Call<AppGUIDModel> {
        return mApiInterface.getAppGUID(
            getSessionToken(),
            getDeviceIdentityToken(),
            getRequestBody(appGUIDRequestType)
        )
    }

    suspend fun dynamicYieldHomePage(dyHomePageRequestEvent: HomePageRequestEvent): retrofit2.Response<DynamicYieldChooseVariationResponse> {
        return mApiInterface.dynamicYieldHomePage(
            getSessionToken(),
            getDeviceIdentityToken(),
            dyHomePageRequestEvent
        )
    }

    suspend fun dynamicYieldChangeAttribute(dyPrepareChangeAttributeRequestEvent: PrepareChangeAttributeRequestEvent): retrofit2.Response<DyChangeAttributeResponse> {
        return mApiInterface.dynamicYieldChangeAttribute(
            getSessionToken(),
            getDeviceIdentityToken(),
            dyPrepareChangeAttributeRequestEvent
        )
    }

    fun verifyUserIsInStore(body: UserLocationRequestBody): Call<UserLocationResponse> {
        return mApiInterface.verifyUserIsInStore(
            "", "", getSessionToken(),
            getDeviceIdentityToken(), body
        )
    }

    suspend fun writeAReviewForm(productId: String?, prepareWriteAReviewFormRequestEvent: PrepareWriteAReviewFormRequestEvent): retrofit2.Response<WriteAReviewFormResponse> {
        return mApiInterface.writeAReviewForm(
            getSessionToken(),
            getDeviceIdentityToken(),
            productId,
            prepareWriteAReviewFormRequestEvent
        )
    }

}