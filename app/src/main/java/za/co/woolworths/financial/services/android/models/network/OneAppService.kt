package za.co.woolworths.financial.services.android.models.network

import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import za.co.absa.openbankingapi.woolworths.integration.dto.PayUResponse
import za.co.woolworths.financial.services.android.checkout.service.network.*
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.bpi.BPIBody
import za.co.woolworths.financial.services.android.models.dto.bpi.InsuranceTypeOptInBody
import za.co.woolworths.financial.services.android.models.dto.cart.SubmittedOrderResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_activation.CreditCardActivationRequestBody
import za.co.woolworths.financial.services.android.models.dto.credit_card_activation.CreditCardActivationResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.AvailableTimeSlotsResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.PossibleAddressResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.ScheduleDeliveryRequest
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkDeviceBody
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.dto.npc.*
import za.co.woolworths.financial.services.android.models.dto.otp.RetrieveOTPResponse
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPRequest
import za.co.woolworths.financial.services.android.models.dto.otp.ValidateOTPResponse
import za.co.woolworths.financial.services.android.models.dto.pma.DeleteResponse
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.dto.size_guide.SizeGuideResponse
import za.co.woolworths.financial.services.android.models.dto.statement.*
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardResponse
import za.co.woolworths.financial.services.android.models.dto.voc.*
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CouponClaimCode
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.SelectedVoucher
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent
import java.net.URLEncoder

object OneAppService : RetrofitConfig() {

    var forceNetworkUpdate: Boolean = false

    fun login(loginRequest: LoginRequest): Call<LoginResponse> {
        return mApiInterface.login("",
            "", getSessionToken(), getDeviceIdentityToken(), loginRequest)
    }

    fun getAccounts(): Call<AccountsResponse> {
        return mApiInterface.getAccounts("", "",
            getSessionToken(), getDeviceIdentityToken())
    }

    fun authoriseLoan(authoriseLoanRequest: AuthoriseLoanRequest): Call<AuthoriseLoanResponse> {
        return mApiInterface.authoriseLoan("", "", getSessionToken(),
            getDeviceIdentityToken(), authoriseLoanRequest)
    }

    fun getAccountTransactionHistory(productOfferingId: String): Call<TransactionHistoryResponse> {
        return mApiInterface.getAccountTransactionHistory("", "", getSessionToken(),
            getDeviceIdentityToken(), productOfferingId)
    }

    fun getVouchers(): Call<VoucherResponse> {
        return mApiInterface.getVouchers("", "", getSessionToken(),
            getDeviceIdentityToken())
    }

    fun getVouchersCount(): Call<VoucherCount> {
        return mApiInterface.getVouchersCount("", "", getSessionToken(),
            getDeviceIdentityToken())
    }

    fun queryServiceGetStore(latitude: Double? = 0.0, longitude: Double? = 0.0, searchTextField: String): Call<LocationResponse> {
        return mApiInterface.queryServiceGetStore("", "",
            getSessionToken(), getDeviceIdentityToken(), latitude.toString(), longitude.toString(), searchTextField)
    }

    fun getStoresForNPC(latitude: Double? = 0.0, longitude: Double? = 0.0, searchTextField: String, npc: Boolean?): Call<LocationResponse> {
        return mApiInterface.getStoresForNPC("", "", getSessionToken(),
            getDeviceIdentityToken(), latitude.toString(), longitude.toString(), searchTextField, npc)
    }

    fun getLocationsItem(sku: String, startRadius: String, endRadius: String): Call<LocationResponse> {
        val loc = getMyLocation()
        return if (startRadius != null && startRadius == "") {
            //This should never happen for now
            mApiInterface.getStoresLocationItem("", "", loc.latitude.toString(),
                loc.longitude.toString(), getSessionToken(), getDeviceIdentityToken(), sku, startRadius, endRadius, true)
        } else {
            mApiInterface.getStoresLocationItem("", "", loc.latitude.toString(),
                loc.longitude.toString(), getSessionToken(), getDeviceIdentityToken(), sku, startRadius, endRadius, true)
        }
    }

    fun getMessagesResponse(pageSize: Int, pageNumber: Int): Call<MessageResponse> {
        return mApiInterface.getMessages("", "", getSessionToken(),
            getDeviceIdentityToken(), pageSize, pageNumber)
    }

    fun cliCreateApplication(offerRequest: CreateOfferRequest): Call<OfferActive> {
        return mApiInterface.cliCreateApplication("", "",
            getSessionToken(), getDeviceIdentityToken(), offerRequest)
    }

    fun cliUpdateApplication(offerRequest: CreateOfferRequest, cliId: String): Call<OfferActive> {
        return mApiInterface.cliUpdateApplication("", "",
            getSessionToken(), getDeviceIdentityToken(), cliId, offerRequest)
    }

    fun createOfferDecision(createOfferDecision: CLIOfferDecision, cliId: String): Call<OfferActive> {
        return mApiInterface.createOfferDecision("", "",
            getSessionToken(), getDeviceIdentityToken(), cliId, createOfferDecision)
    }

    fun getDeaBanks(): Call<DeaBanks> {
        return mApiInterface.getDeaBanks("", "",
            getSessionToken(), getDeviceIdentityToken(), 0, 0)
    }

    fun getBankAccountTypes(): Call<BankAccountTypes> {
        return mApiInterface.getBankAccountTypes("", "",
            getSessionToken(), getDeviceIdentityToken(), 0, 0)
    }

    fun getActiveOfferRequest(productOfferingId: String): Call<OfferActive> {
        return mApiInterface.getActiveOfferRequest( "", "",
            getSessionToken(), getDeviceIdentityToken(), productOfferingId)
    }

    fun getDeleteMessagesResponse(id: String): Call<DeleteMessageResponse> {
        return mApiInterface.getDeleteresponse("", "",
            getSessionToken(), getDeviceIdentityToken(), id)
    }

    fun getReadMessagesResponse(readMessages: MessageReadRequest): Call<ReadMessagesResponse> {
        return mApiInterface.setReadMessages("", "",
            getSessionToken(), getDeviceIdentityToken(), readMessages)
    }

    fun cliEmailResponse(): Call<CLIEmailResponse> {
        return mApiInterface.cliSendEmailRquest( getSessionToken(), getDeviceIdentityToken())
    }

    fun cliUpdateBankDetail(updateBankDetail: UpdateBankDetail): Call<UpdateBankDetailResponse> {
        return mApiInterface.cliUpdateBankRequest( "", "",
            getSessionToken(), getDeviceIdentityToken(), updateBankDetail)
    }

    fun getResponseOnCreateUpdateDevice(device: CreateUpdateDevice): Call<CreateUpdateDeviceResponse> {
        return mApiInterface.createUpdateDevice( "", "",
            getSessionToken(), getDeviceIdentityToken(), device)
    }

    fun issueLoan(issueLoan: IssueLoan): Call<IssueLoanResponse> {
        return mApiInterface.issueLoan( "", "", getSessionToken(),
            getDeviceIdentityToken(), issueLoan)
    }

    fun addToList(addToListRequest: MutableList<AddToListRequest>, listId: String): Call<ShoppingListItemsResponse> {
        return mApiInterface.addToList( getSessionToken(), getDeviceIdentityToken(), listId,
            addToListRequest)
    }

    fun getPromotions(): Call<PromotionsResponse> {
        return mApiInterface.getPromotions( "", "", getSessionToken(),
            getDeviceIdentityToken())
    }

    fun getRootCategory(locationEnabled: Boolean, location: Location?, deliveryType: String): Call<RootCategories> {
        val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()
        // Pass storeId value of 01 fulfillment type
        var locationCord = location
        if (!locationEnabled) {
            locationCord = null
        }

        return mApiInterface.getRootCategories(
            getSessionToken(),
            getDeviceIdentityToken(), locationCord?.latitude, locationCord?.longitude, suburbId, storeId, deliveryType)
    }

    suspend fun getDashCategoryNavigation(location: Location?): retrofit2.Response<RootCategories> {
        return withContext(Dispatchers.IO) {
            val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()

             mApiInterface.getDashCategoriesNavigation(
                getSessionToken(),
                getDeviceIdentityToken(), location?.latitude, location?.longitude, suburbId, storeId, "OnDemand")
        }
    }

    suspend fun getDashLandingDetails(): retrofit2.Response<DashCategories> {
        return withContext(Dispatchers.IO) {
             mApiInterface.getDashLandingDetails(getSessionToken(), getDeviceIdentityToken())
        }
    }

    fun getSubCategory(category_id: String, version: String): Call<SubCategories> {
        val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()

        return mApiInterface.getSubCategory(
            getSessionToken(), getDeviceIdentityToken(),
            category_id, version, suburbId, storeId)
    }

    fun getProvinces(): Call<ProvincesResponse> {
        return mApiInterface.getProvinces( "", "", getSessionToken(),
            getDeviceIdentityToken())
    }

    fun getSavedAddresses(): Call<SavedAddressResponse> {
        return mApiInterface.getSavedAddresses( "", "", getSit4Environment(), getSessionToken(),
            getDeviceIdentityToken())
    }

    fun addAddress(addAddressRequestBody: AddAddressRequestBody): Call<AddAddressResponse> {
        return mApiInterface.addAddress(
            "",
            "",
            getSit4Environment(),
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
            getSit4Environment(),
            getSessionToken(), getDeviceIdentityToken(), addressId, addAddressRequestBody
        )
    }
    fun deleteAddress(addressId: String): Call<DeleteAddressResponse> {
        return mApiInterface.deleteAddress(getSessionToken(), getSit4Environment(), getDeviceIdentityToken(), addressId)
    }

    fun changeAddress(nickName: String): Call<ChangeAddressResponse> {
        return mApiInterface.changeAddress(nickName, "", "", getSit4Environment(), getSessionToken(),
            getDeviceIdentityToken())
    }

    fun getShippingDetails(body: ShippingDetailsBody): Call<ShippingDetailsResponse>{
        return mApiInterface.getShippingDetails("", "", getSit4Environment(), getSessionToken(),
        getDeviceIdentityToken(), body)
    }

    fun getStorePickupInfo(body: StorePickupInfoBody): Call<ConfirmDeliveryAddressResponse> {
        return mApiInterface.getStorePickupInfo("", "", getSessionToken(),
        getDeviceIdentityToken(), body)
    }

    fun setConfirmSelection(confirmSelectionRequestBody: ConfirmSelectionRequestBody): Call<ConfirmSelectionResponse>{
        return mApiInterface.setConfirmSelection("", "", getSessionToken(),
        getDeviceIdentityToken(), confirmSelectionRequestBody)
    }

    fun getCartSummary(): Call<CartSummaryResponse> {
        return mApiInterface.getCartSummary( "", getDev4Environment(), "", getSessionToken(),
            getDeviceIdentityToken())
    }

    fun getSuburbs(locationId: String): Call<SuburbsResponse> {
        return mApiInterface.getSuburbs( "", "", getSessionToken(),
            getDeviceIdentityToken(), locationId)
    }

    fun setSuburb(suburbId: String): Call<SetDeliveryLocationSuburbResponse> {
        val request = SetDeliveryLocationSuburbRequest(suburbId)
        return mApiInterface.setDeliveryLocationSuburb( getSessionToken(), getDeviceIdentityToken(),
            request)
    }


    fun getProducts(requestParams: ProductsRequestParams): Call<ProductView> {
        val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()

        return if (Utils.isLocationEnabled(appContext())) {
            mApiInterface.getProducts("", "", getSit4Environment(),  "",
                "", getSessionToken(), getDeviceIdentityToken(), requestParams.searchTerm, requestParams.searchType.value,
                requestParams.responseType.value, requestParams.pageOffset, Utils.PAGE_SIZE, requestParams.sortOption,
                requestParams.refinement, suburbId = suburbId, storeId = storeId, filterContent = requestParams.filterContent
            )
        } else {
            mApiInterface.getProductsWithoutLocation("", "", getSit4Environment(), getSessionToken(),
                getDeviceIdentityToken(), requestParams.searchTerm, requestParams.searchType.value, requestParams.responseType.value,
                requestParams.pageOffset, Utils.PAGE_SIZE, requestParams.sortOption, requestParams.refinement, suburbId = suburbId,
                storeId = storeId, filterContent =  requestParams.filterContent)
        }
    }

    private fun getSuburbOrStoreId(): Pair<String?, String?> {
        val suburbId: String? = null
        val storeId: String? = null
        return Pair(suburbId, storeId)
    }

    fun getFAQ(): Call<FAQ> {
        return mApiInterface.getFAQ("", "", getSessionToken(),
            getDeviceIdentityToken())
    }

    fun getCardDetails(): Call<CardDetailsResponse> {
        return mApiInterface.getCardDetails( getSessionToken(), getDeviceIdentityToken())
    }

    fun getStatementResponse(statement: UserStatement): Call<StatementResponse> {
        return mApiInterface.getUserStatement( "", "", getSessionToken(),
            getDeviceIdentityToken(), statement.productOfferingId, statement.accountNumber
                ?: "", statement.startDate, statement.endDate)
    }

    fun sendStatementRequest(statement: SendUserStatementRequest): Call<SendUserStatementResponse> {
        return mApiInterface.sendUserStatement( "", "", getSessionToken(),
            getDeviceIdentityToken(), statement)
    }

    fun addItemToCart(addToCart: MutableList<AddItemToCart>): Call<AddItemToCartResponse> {

        val deliveryType = KotlinUtils.getPreferredDeliveryType()?.type ?: ""

        return mApiInterface.addItemToCart( "", "", getDev4Environment(), getSessionToken(),
            getDeviceIdentityToken(),deliveryType, addToCart)
    }

    fun getShoppingCart(): Call<ShoppingCartResponse> {
        return mApiInterface.getShoppingCart( getSessionToken(), getDev4Environment(), getDeviceIdentityToken())
    }

    fun getChangeQuantity(changeQuantity: ChangeQuantity): Call<ShoppingCartResponse> {
        return mApiInterface.changeQuantityRequest( "",
                "", getSessionToken(), getDeviceIdentityToken(), changeQuantity.commerceId, changeQuantity)
    }

    fun removeCartItem(commerceId: String): Call<ShoppingCartResponse> {
        return mApiInterface.removeItemFromCart( getSessionToken(), getDeviceIdentityToken(), getDev4Environment(), commerceId)
    }

    fun removeAllCartItems(): Call<ShoppingCartResponse> {
        return mApiInterface.removeAllCartItems( getSessionToken(), getDev4Environment(), getDeviceIdentityToken())
    }

    fun productDetail(productId: String, skuId: String): Call<ProductDetailResponse> {
        val loc = getMyLocation()
        val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()
        return if (Utils.isLocationEnabled(appContext())) {
            mApiInterface.productDetail("", "", getSit4Environment(),
                     loc.longitude, loc.latitude, getSessionToken(), getDeviceIdentityToken(),
                    productId, skuId, suburbId, storeId)
        } else {
            mApiInterface.productDetail( "", "",
                getSit4Environment(), getSessionToken(), getDeviceIdentityToken(),
                    productId, skuId, suburbId, storeId)
        }
    }

    fun getShoppingLists(): Call<ShoppingListsResponse> {
        return mApiInterface.getShoppingLists( getSessionToken(), getDeviceIdentityToken())
    }

    fun createList(listName: CreateList): Call<ShoppingListsResponse> {
        return mApiInterface.createList( getSessionToken(), getDeviceIdentityToken(), listName)
    }


    fun getShoppingListItems(listId: String): Call<ShoppingListItemsResponse> {
        return mApiInterface.getShoppingListItems( getSessionToken(), getDeviceIdentityToken(), listId)
    }

    fun deleteShoppingList(listId: String): Call<ShoppingListsResponse> {
        return mApiInterface.deleteShoppingList( getSessionToken(), getDeviceIdentityToken(), listId)
    }

    fun deleteShoppingListItem(listId: String, id: String, productId: String, catalogRefId: String): Call<ShoppingListItemsResponse> {
        return mApiInterface.deleteShoppingListItem( getSessionToken(), getDeviceIdentityToken(), listId, id, productId,
            catalogRefId)
    }

    fun getInventorySku(multipleSku: String): Call<SkuInventoryResponse> {
        return mApiInterface.getInventorySKU( getSessionToken(), getDeviceIdentityToken(), getDev4Environment(), multipleSku)
    }

    fun getInventorySkuForStore(store_id: String, multipleSku: String): Call<SkusInventoryForStoreResponse> {
        return mApiInterface.getInventorySKUForStore( getSessionToken(), getDeviceIdentityToken(), getDev4Environment(), store_id, multipleSku)

    }

    fun getPDFResponse(getStatement: GetStatement): Call<ResponseBody> {
        return mApiInterface.getStatement( "", "", getSessionToken(),
            getDeviceIdentityToken(), getStatement.docId, getStatement.productOfferingId, getStatement.docDesc)
    }

    fun postCheckoutSuccess(checkoutSuccess: CheckoutSuccess): Call<Void> {
        return mApiInterface.postCheckoutSuccess( getSessionToken(), getSit4Environment(), getDeviceIdentityToken(), checkoutSuccess)
    }

    fun getOrders(): Call<OrdersResponse> {
        return mApiInterface.getOrders(getSessionToken(), getDeviceIdentityToken())
    }

    fun getOrderDetails(orderId: String): Call<OrderDetailsResponse> {
        return mApiInterface.getOrderDetails( getSessionToken(), getDeviceIdentityToken(), orderId)
    }

    fun addOrderToList(orderId: String, orderToShoppingListRequestBody: OrderToShoppingListRequestBody): Call<OrderToListReponse> {
        return mApiInterface.addOrderToList( getSessionToken(), getDeviceIdentityToken(), orderId,
            orderToShoppingListRequestBody)
    }

    fun getOrderTaxInvoice(taxNoteNumber: String): Call<OrderTaxInvoiceResponse> {
        return mApiInterface.getTaxInvoice(getSessionToken(), getDeviceIdentityToken(), taxNoteNumber)
    }

    fun getCreditCardToken(): Call<CreditCardTokenResponse> {
        return mApiInterface.getCreditCardToken("", "", getSessionToken(), getDeviceIdentityToken())
    }

    fun postBlockMyCard(blockCardRequestBody: BlockCardRequestBody, productOfferingId: String): Call<BlockMyCardResponse> {
        return mApiInterface.blockStoreCard(getOsVersion(), "", getSessionToken(),
            getDeviceIdentityToken(), productOfferingId, blockCardRequestBody)
    }

    fun getStoreCards(storeCardsRequestBody: StoreCardsRequestBody): Call<StoreCardsResponse> {
        val lastSavedLocation = Utils.getLastSavedLocation()
        return mApiInterface.getStoreCards("", "", getSessionToken(),
            getDeviceIdentityToken(),lastSavedLocation?.latitude, lastSavedLocation?.longitude, storeCardsRequestBody)
    }

    fun getLinkNewCardOTP(otpMethodType: OTPMethodType): Call<LinkNewCardOTP> {
        return mApiInterface.getLinkNewCardOTP( "", "", getSessionToken(),
            getDeviceIdentityToken(), otpMethodType.name)
    }

    fun linkStoreCardRequest(linkStoreCard: LinkStoreCard): Call<LinkNewCardResponse> {
        return mApiInterface.linkStoreCard( "", "", getSessionToken(),
            getDeviceIdentityToken(), linkStoreCard)
    }

    fun unblockStoreCard(productOfferingId: String, requestBody: UnblockStoreCardRequestBody): Call<UnblockStoreCardResponse> {
        return mApiInterface.unblockStoreCard( "", "", getSessionToken(),
            getDeviceIdentityToken(), productOfferingId, requestBody)
    }

    fun activateCreditCardRequest(requestBody: CreditCardActivationRequestBody): Call<CreditCardActivationResponse> {
        return mApiInterface.activateCreditCard( "", "", getSessionToken(),
            getDeviceIdentityToken(), requestBody)
    }

    fun retrieveOTP(otpMethodType: OTPMethodType, productOfferingId: String): Call<RetrieveOTPResponse> {
        return mApiInterface.retrieveOTP( "", "", getSessionToken(),
            getDeviceIdentityToken(), otpMethodType.name, productOfferingId)
    }

    fun validateOTP(validateOTPRequest: ValidateOTPRequest, productOfferingId: String): Call<ValidateOTPResponse> {
        return mApiInterface.validateOTP( "", "", getSessionToken(),
            getDeviceIdentityToken(), validateOTPRequest, productOfferingId)
    }

    fun queryServiceCancelOrder(orderId: String): Call<CancelOrderResponse> {
        return mApiInterface.queryServiceCancelOrder( "", "",
            getSessionToken(), getDeviceIdentityToken(), orderId)
    }

    fun getCreditCardDeliveryStatus(envelopeReference: String, productOfferingId: String): Call<CreditCardDeliveryStatusResponse> {
        return mApiInterface.cardDeliveryStatus( "", "",
            getSessionToken(), getDeviceIdentityToken(), envelopeReference, productOfferingId)
    }

    fun getPossibleAddress(searchPhrase: String, productOfferingId: String, envelopeNumber: String): Call<PossibleAddressResponse> {
        return mApiInterface.possibleAddress( "", "",
            getSessionToken(), getDeviceIdentityToken(), searchPhrase, envelopeNumber, productOfferingId)
    }

    fun getAvailableTimeSlots(envelopeReference: String, productOfferingId: String, x: String, y: String, shipByDate: String): Call<AvailableTimeSlotsResponse> {
        return mApiInterface.availableTimeSlots( "", "",
            getSessionToken(), getDeviceIdentityToken(), envelopeReference, productOfferingId, x, y, shipByDate)
    }

    fun postScheduleDelivery(productOfferingId: String, envelopeNumber: String, schedule: Boolean, bookingReference: String, scheduleDeliveryRequest: ScheduleDeliveryRequest): Call<CreditCardDeliveryStatusResponse> {
        return mApiInterface.scheduleDelivery( "", "",
            getSessionToken(), getDeviceIdentityToken(), productOfferingId, envelopeNumber, schedule, bookingReference,
            scheduleDeliveryRequest)
    }

    fun queryServicePostEvent(featureName: String?, appScreen: String?): Call<Response> {
        return mApiInterface.postEvent( "", "",
            getSessionToken(), getDeviceIdentityToken(), featureName
                ?: "", appScreen ?: "")
    }

    fun queryServicePayUMethod(): Call<PaymentMethodsResponse> {
        return mApiInterface.getPaymentPAYUMethod("", "",
            getSessionToken(), getDeviceIdentityToken())
    }

    fun queryServicePostPayU(payUPay: PayUPay): Call<PayUResponse> {
        return mApiInterface.postPayUpPay( "", "", getSessionToken(),
            getDeviceIdentityToken(), payUPay)
    }

    fun queryServicePaymentResult(request: PayUPayResultRequest): Call<PayUPayResultResponse> {
        return mApiInterface.getPaymentPayUResult( "", "", getSessionToken(),
            getDeviceIdentityToken(), request.customer, request.payment_id, request.charge_id, request.status,
            request.productOfferingID)
    }

    fun validateSelectedSuburb(suburbId: String, isStore: Boolean): Call<ValidateSelectedSuburbResponse> {
        return mApiInterface.validateSelectedSuburb( "", "",
            getSessionToken(), getDeviceIdentityToken(), suburbId, isStore)
    }

    suspend fun getValidateLocation(placeId: String): retrofit2.Response<ValidateLocationResponse> {
        return withContext(Dispatchers.IO) {
            mApiInterface.validatePlace("",
                getSit4Environment(),
                "",
                getSessionToken(),
                getDeviceIdentityToken(),
                placeId)
        }
    }

    fun applyVouchers(vouchers: List<SelectedVoucher>): Call<ShoppingCartResponse> {
        return mApiInterface.applyVouchers( "", "", getSessionToken(),
            getDeviceIdentityToken(), vouchers)
    }

    fun applyPromoCode(couponClaimCode: CouponClaimCode): Call<ShoppingCartResponse> {
        return mApiInterface.applyPromoCode( "", "", getSessionToken(),
            getDeviceIdentityToken(), couponClaimCode)
    }

    fun removePromoCode(couponClaimCode: CouponClaimCode): Call<ShoppingCartResponse> {
        return mApiInterface.removePromoCode( "", "", getSessionToken(),
            getDeviceIdentityToken(), couponClaimCode)
    }

    fun queryServicePayURemovePaymentMethod(paymenToken: String): Call<DeleteResponse> {
        return mApiInterface.payURemovePaymentMethod( "", "", getSessionToken(),
            getDeviceIdentityToken(), paymenToken)
    }

    fun getSizeGuideContent(contentId: String): Call<SizeGuideResponse> {
        return mApiInterface.getSizeGuideContent( "", "", getSessionToken(),
            getDeviceIdentityToken(), contentId)
    }

    fun getAccountsByProductOfferingId(productOfferingId: String): Call<AccountsResponse> {
        return mApiInterface.getAccountsByProductOfferingId( "", "", getSessionToken(),
            getDeviceIdentityToken(), productOfferingId)
    }

    fun getLinkDeviceOtp(otpMethod: String): Call<RetrieveOTPResponse> {
        return mApiInterface.getLinkDeviceOTP(
            "",
            "",
            getDeviceIdentityToken(),
            getSessionToken(),
            otpMethod)
    }

    fun changePrimaryDeviceApi(deviceIdentityId: String, otp: String?, otpMethod: String?): Call<ViewAllLinkedDeviceResponse> {
        return mApiInterface.changePrimaryDeviceApi(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            deviceIdentityId,
            otp,
            otpMethod)
    }

    fun linkDeviceApi(
        deviceName: String,
        appInstanceId: String,
        location: String?,
        primaryDevice: Boolean,
        firebaseToken: String,
        otp: String?,
        otpMethod: String?): Call<LinkedDeviceResponse> {
        val body = LinkDeviceBody(appInstanceId, location, primaryDevice, firebaseToken)

        return mApiInterface.linkDeviceApi(
            "",
            "",
            getSessionToken(),
            URLEncoder.encode(deviceName, "UTF-8"),
            body,
            otp,
            otpMethod)
    }

    fun getAllLinkedDevices(isForced: Boolean): Call<ViewAllLinkedDeviceResponse> {
       forceNetworkUpdate = isForced
       return mApiInterface.getAllLinkedDevices(
           "",
           "",
           getDeviceIdentityToken(),
           getSessionToken())

    }

    fun deleteDevice(deviceIdentityId: String,
                     newPrimaryDeviceIdentityId: String?,
                     otp: String?,
                     otpMethod: String?): Call<ViewAllLinkedDeviceResponse> {
        return mApiInterface.deleteDevice(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            deviceIdentityId,
            newPrimaryDeviceIdentityId,
            otp,
            otpMethod)
    }

    fun confirmStoreCardEmail(body: StoreCardEmailConfirmBody): Call<GenericResponse> {
        return mApiInterface.confirmStoreCardEmail( "", "",
            getSessionToken(), getDeviceIdentityToken(),  body)
    }

    fun getVocSurvey(triggerEvent: VocTriggerEvent): Call<SurveyDetailsResponse> {
        return mApiInterface.getVocSurvey(
                userAgent = "",
                userAgentVersion =  "",
                sessionToken = getSessionToken(),
                triggerEvent = triggerEvent.value
        )
    }

    fun submitVocSurveyReplies(surveyDetails: SurveyDetails, surveyAnswers: HashMap<Long, SurveyAnswer>): Call<Void> {
        return mApiInterface.submitVocSurveyReplies(
                userAgent = "",
                userAgentVersion =  "",
                sessionToken = getSessionToken(),
                surveyId = surveyDetails.id,
                surveyReplies = SurveyRepliesBody(
                        surveyId = surveyDetails.id,
                        appInstanceId = Utils.getUniqueDeviceID(),
                        participantReplies = surveyAnswers.values.toList()
                )
        )
    }

    fun optOutVocSurvey(): Call<Void> {
        return mApiInterface.optOutVocSurvey(
                userAgent = "",
                userAgentVersion =  "",
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
            getSit4Environment(),
            getSessionToken(),
            getDeviceIdentityToken())
    }

    fun getBPITermsAndConditionsInfo(productGroupCode: String): Call<BPITermsConditionsResponse>{
        return mApiInterface.getBPITermsAndConditionsInfo(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            productGroupCode)
    }

    fun emailBPITermsAndConditions(productGroupCode: String): Call<GenericResponse>{
        return mApiInterface.emailBPITermsAndConditions(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            BPIBody(productGroupCode)
        )
    }

    fun postInsuranceLeadGenOptIn(insuranceType: String, insuranceTypeOptInBody: InsuranceTypeOptInBody): Call<GenericResponse> {
        return mApiInterface.postInsuranceLeadGenOptIn(
            "",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            insuranceType,
            insuranceTypeOptInBody
        )
    }


    fun getConfirmDeliveryAddressDetails(body: ConfirmLocationRequest): Call<ConfirmDeliveryAddressResponse>{
        return mApiInterface.confirmLocation("",
            "",
            getSit4Environment(),
            getSessionToken(),
            getDeviceIdentityToken(),
            body
        )
    }
}