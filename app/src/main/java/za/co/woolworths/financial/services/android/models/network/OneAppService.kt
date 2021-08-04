package za.co.woolworths.financial.services.android.models.network

import android.location.Location
import okhttp3.ResponseBody
import retrofit2.Call
import za.co.absa.openbankingapi.woolworths.integration.dto.PayUResponse
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.credit_card_activation.CreditCardActivationRequestBody
import za.co.woolworths.financial.services.android.models.dto.credit_card_activation.CreditCardActivationResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.AvailableTimeSlotsResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.PossibleAddressResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.ScheduleDeliveryRequest
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
import za.co.woolworths.financial.services.android.models.dto.statement.*
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardResponse
import za.co.woolworths.financial.services.android.models.dto.voc.*
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CouponClaimCode
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.SelectedVoucher
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent

object OneAppService : RetrofitConfig() {

    var forceNetworkUpdate: Boolean = false

    fun login(loginRequest: LoginRequest): Call<LoginResponse> {
        return mApiInterface.login(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "",
            "", getSessionToken(), getDeviceIdentityToken(), loginRequest)
    }

    fun getAccounts(): Call<AccountsResponse> {
        return mApiInterface.getAccounts(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken())
    }

    fun authoriseLoan(authoriseLoanRequest: AuthoriseLoanRequest): Call<AuthoriseLoanResponse> {
        return mApiInterface.authoriseLoan(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), authoriseLoanRequest)
    }

    fun getAccountTransactionHistory(productOfferingId: String): Call<TransactionHistoryResponse> {
        return mApiInterface.getAccountTransactionHistory(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), productOfferingId)
    }

    fun getVouchers(): Call<VoucherResponse> {
        return mApiInterface.getVouchers(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken())
    }

    fun getVouchersCount(): Call<VoucherCount> {
        return mApiInterface.getVouchersCount(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken())
    }

    fun queryServiceGetStore(latitude: Double? = 0.0, longitude: Double? = 0.0, searchTextField: String): Call<LocationResponse> {
        return mApiInterface.queryServiceGetStore(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), latitude.toString(), longitude.toString(), searchTextField)
    }

    fun getStoresForNPC(latitude: Double? = 0.0, longitude: Double? = 0.0, searchTextField: String, npc: Boolean?): Call<LocationResponse> {
        return mApiInterface.getStoresForNPC(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), latitude.toString(), longitude.toString(), searchTextField, npc)
    }

    fun getLocationsItem(sku: String, startRadius: String, endRadius: String): Call<LocationResponse> {
        val loc = getMyLocation()
        return if (startRadius != null && startRadius == "") {
            //This should never happen for now
            mApiInterface.getStoresLocationItem(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
                getNetworkCarrier(), getOS(), getOsVersion(), "", "", loc.getLatitude().toString(),
                loc.getLongitude().toString(), getSessionToken(), getDeviceIdentityToken(), sku, startRadius, endRadius, true)
        } else {
            mApiInterface.getStoresLocationItem(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
                getNetworkCarrier(), getOS(), getOsVersion(), "", "", loc.getLatitude().toString(),
                loc.getLongitude().toString(), getSessionToken(), getDeviceIdentityToken(), sku, startRadius, endRadius, true)
        }
    }

    fun getMessagesResponse(pageSize: Int, pageNumber: Int): Call<MessageResponse> {
        return mApiInterface.getMessages(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), pageSize, pageNumber)
    }

    fun cliCreateApplication(offerRequest: CreateOfferRequest): Call<OfferActive> {
        return mApiInterface.cliCreateApplication(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), offerRequest)
    }

    fun cliUpdateApplication(offerRequest: CreateOfferRequest, cliId: String): Call<OfferActive> {
        return mApiInterface.cliUpdateApplication(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), cliId, offerRequest)
    }

    fun createOfferDecision(createOfferDecision: CLIOfferDecision, cliId: String): Call<OfferActive> {
        return mApiInterface.createOfferDecision(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), cliId, createOfferDecision)
    }

    fun getDeaBanks(): Call<DeaBanks> {
        return mApiInterface.getDeaBanks(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), 0, 0)
    }

    fun getBankAccountTypes(): Call<BankAccountTypes> {
        return mApiInterface.getBankAccountTypes(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), 0, 0)
    }

    fun getActiveOfferRequest(productOfferingId: String): Call<OfferActive> {
        return mApiInterface.getActiveOfferRequest(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), productOfferingId)
    }

    fun getDeleteMessagesResponse(id: String): Call<DeleteMessageResponse> {
        return mApiInterface.getDeleteresponse(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), id)
    }

    fun getReadMessagesResponse(readMessages: MessageReadRequest): Call<ReadMessagesResponse> {
        return mApiInterface.setReadMessages(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), readMessages)
    }

    fun cliEmailResponse(): Call<CLIEmailResponse> {
        return mApiInterface.cliSendEmailRquest(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken())
    }

    fun cliUpdateBankDetail(updateBankDetail: UpdateBankDetail): Call<UpdateBankDetailResponse> {
        return mApiInterface.cliUpdateBankRequest(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), updateBankDetail)
    }

    fun getResponseOnCreateUpdateDevice(device: CreateUpdateDevice): Call<CreateUpdateDeviceResponse> {
        return mApiInterface.createUpdateDevice(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), device)
    }

    fun issueLoan(issueLoan: IssueLoan): Call<IssueLoanResponse> {
        return mApiInterface.issueLoan(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), issueLoan)
    }

    fun addToList(addToListRequest: MutableList<AddToListRequest>, listId: String): Call<ShoppingListItemsResponse> {
        return mApiInterface.addToList(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(), listId,
            addToListRequest)
    }

    fun getPromotions(): Call<PromotionsResponse> {
        return mApiInterface.getPromotions(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken())
    }

    fun getRootCategory(locationEnabled: Boolean, location: Location?): Call<RootCategories> {
        val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()
        var locationCord = location
        if (!locationEnabled) {
            locationCord = null
            // Hardcoding only for testing purpose.
//            location.latitude = -33.907630
//            location.longitude = 18.408380
        }

        return mApiInterface.getRootCategories(getOsVersion(), getApiId(), getOS(), getSha1Password(),
            getDeviceModel(), getNetworkCarrier(), getDeviceManufacturer(), "Android", getSessionToken(),
            getDeviceIdentityToken(), locationCord?.latitude, locationCord?.longitude, suburbId, storeId)
    }

    fun getSubCategory(category_id: String, version: String): Call<SubCategories> {
        return mApiInterface.getSubCategory(getOsVersion(), getApiId(), getOS(), getSha1Password(), getDeviceModel(),
            getNetworkCarrier(), getDeviceManufacturer(), "Android", getSessionToken(), getDeviceIdentityToken(),
            category_id, version)
    }

    fun getProvinces(): Call<ProvincesResponse> {
        return mApiInterface.getProvinces(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken())
    }

    fun getCartSummary(): Call<CartSummaryResponse> {
        return mApiInterface.getCartSummary(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken())
    }

    fun getSuburbs(locationId: String): Call<SuburbsResponse> {
        return mApiInterface.getSuburbs(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), locationId)
    }

    fun setSuburb(suburbId: String): Call<SetDeliveryLocationSuburbResponse> {
        val request = SetDeliveryLocationSuburbRequest(suburbId)
        return mApiInterface.setDeliveryLocationSuburb(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(),
            request)
    }

    fun getProducts(requestParams: ProductsRequestParams): Call<ProductView> {
        val loc = getMyLocation()
        val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()

        return if (Utils.isLocationEnabled(appContext())) {
            mApiInterface.getProducts(getOsVersion(), getDeviceModel(), getDeviceManufacturer(), getOS(),
                getNetworkCarrier(), getApiId(), "", "", getSha1Password(), "",
                "", getSessionToken(), getDeviceIdentityToken(), requestParams.searchTerm, requestParams.searchType.value,
                requestParams.responseType.value, requestParams.pageOffset, Utils.PAGE_SIZE, requestParams.sortOption,
                requestParams.refinement, suburbId = suburbId, storeId = storeId)
        } else {
            mApiInterface.getProductsWithoutLocation(getOsVersion(), getDeviceModel(), getDeviceManufacturer(), getOS(),
                getNetworkCarrier(), getApiId(), "", "", getSha1Password(), getSessionToken(),
                getDeviceIdentityToken(), requestParams.searchTerm, requestParams.searchType.value, requestParams.responseType.value,
                requestParams.pageOffset, Utils.PAGE_SIZE, requestParams.sortOption, requestParams.refinement, suburbId = suburbId,
                storeId = storeId)
        }
    }

    private fun getSuburbOrStoreId(): Pair<String?, String?> {
        var suburbId: String? = null
        var storeId: String? = null
        Utils.getPreferredDeliveryLocation()?.apply {
            if (province?.id.isNullOrEmpty()) return Pair(null, null)
            if (storePickup) {
                storeId = store.id
            } else {
                suburbId = suburb?.id
            }
        }
        return Pair(suburbId, storeId)
    }

    fun getFAQ(): Call<FAQ> {
        return mApiInterface.getFAQ(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken())
    }

    fun getCardDetails(): Call<CardDetailsResponse> {
        return mApiInterface.getCardDetails(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken())
    }

    fun getStatementResponse(statement: UserStatement): Call<StatementResponse> {
        return mApiInterface.getUserStatement(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), statement.productOfferingId, statement.accountNumber
                ?: "", statement.startDate, statement.endDate)
    }

    fun sendStatementRequest(statement: SendUserStatementRequest): Call<SendUserStatementResponse> {
        return mApiInterface.sendUserStatement(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), statement)
    }

    fun addItemToCart(addToCart: MutableList<AddItemToCart>): Call<AddItemToCartResponse> {
        return mApiInterface.addItemToCart(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), addToCart)
    }

    fun getShoppingCart(): Call<ShoppingCartResponse> {
        return mApiInterface.getShoppingCart(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken())
    }

    fun getChangeQuantity(changeQuantity: ChangeQuantity): Call<ShoppingCartResponse> {
        return mApiInterface.changeQuantityRequest(getApiId(), getSha1Password(), getDeviceManufacturer(),
                getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "",
                "", getSessionToken(), getDeviceIdentityToken(), changeQuantity.commerceId, changeQuantity)
    }

    fun removeCartItem(commerceId: String): Call<ShoppingCartResponse> {
        return mApiInterface.removeItemFromCart(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(), commerceId)
    }

    fun removeAllCartItems(): Call<ShoppingCartResponse> {
        return mApiInterface.removeAllCartItems(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken())
    }

    fun productDetail(productId: String, skuId: String): Call<ProductDetailResponse> {
        val loc = getMyLocation()
        val (suburbId: String?, storeId: String?) = getSuburbOrStoreId()
        return if (Utils.isLocationEnabled(appContext())) {
            mApiInterface.productDetail(getOsVersion(), getDeviceModel(), getDeviceManufacturer(),
                    getOS(), getNetworkCarrier(), getApiId(), "", "",
                    getSha1Password(), loc.longitude, loc.latitude, getSessionToken(), getDeviceIdentityToken(),
                    productId, skuId, suburbId, storeId)
        } else {
            mApiInterface.productDetail(getOsVersion(), getDeviceModel(), getDeviceManufacturer(),
                    getOS(), getNetworkCarrier(), getApiId(), "", "",
                    getSha1Password(), getSessionToken(), getDeviceIdentityToken(),
                    productId, skuId, suburbId, storeId)
        }
    }

    fun getShoppingLists(): Call<ShoppingListsResponse> {
        return mApiInterface.getShoppingLists(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken())
    }

    fun createList(listName: CreateList): Call<ShoppingListsResponse> {
        return mApiInterface.createList(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(), listName)
    }


    fun getShoppingListItems(listId: String): Call<ShoppingListItemsResponse> {
        return mApiInterface.getShoppingListItems(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(), listId)
    }

    fun deleteShoppingList(listId: String): Call<ShoppingListsResponse> {
        return mApiInterface.deleteShoppingList(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(), listId)
    }

    fun deleteShoppingListItem(listId: String, id: String, productId: String, catalogRefId: String): Call<ShoppingListItemsResponse> {
        return mApiInterface.deleteShoppingListItem(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(), listId, id, productId,
            catalogRefId)
    }

    fun getInventorySku(multipleSku: String): Call<SkuInventoryResponse> {
        return mApiInterface.getInventorySKU(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(), multipleSku)
    }

    fun getInventorySkuForStore(store_id: String, multipleSku: String): Call<SkusInventoryForStoreResponse> {
        return mApiInterface.getInventorySKUForStore(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(), store_id, multipleSku)

    }

    fun getPDFResponse(getStatement: GetStatement): Call<ResponseBody> {
        return mApiInterface.getStatement(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), getStatement.docId, getStatement.productOfferingId, getStatement.docDesc)
    }

    fun postCheckoutSuccess(checkoutSuccess: CheckoutSuccess): Call<Void> {
        return mApiInterface.postCheckoutSuccess(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(), checkoutSuccess)
    }

    fun getOrders(): Call<OrdersResponse> {
        return mApiInterface.getOrders(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken())
    }

    fun getOrderDetails(orderId: String): Call<OrderDetailsResponse> {
        return mApiInterface.getOrderDetails(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(), orderId)
    }

    fun addOrderToList(orderId: String, orderToShoppingListRequestBody: OrderToShoppingListRequestBody): Call<OrderToListReponse> {
        return mApiInterface.addOrderToList(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(), orderId,
            orderToShoppingListRequestBody)
    }

    fun getOrderTaxInvoice(taxNoteNumber: String): Call<OrderTaxInvoiceResponse> {
        return mApiInterface.getTaxInvoice(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), getDeviceIdentityToken(), taxNoteNumber)
    }

    fun getCreditCardToken(): Call<CreditCardTokenResponse> {
        return mApiInterface.getCreditCardToken(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), getDeviceIdentityToken())
    }

    fun postBlockMyCard(blockCardRequestBody: BlockCardRequestBody, productOfferingId: String): Call<BlockMyCardResponse> {
        return mApiInterface.blockStoreCard(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), getOsVersion(), "", getSessionToken(),
            getDeviceIdentityToken(), productOfferingId, blockCardRequestBody)
    }

    fun getStoreCards(storeCardsRequestBody: StoreCardsRequestBody): Call<StoreCardsResponse> {
        val lastSavedLocation = Utils.getLastSavedLocation()
        return mApiInterface.getStoreCards(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(),lastSavedLocation?.latitude, lastSavedLocation?.longitude, storeCardsRequestBody)
    }

    fun getLinkNewCardOTP(otpMethodType: OTPMethodType): Call<LinkNewCardOTP> {
        return mApiInterface.getLinkNewCardOTP(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), otpMethodType.name)
    }

    fun linkStoreCardRequest(linkStoreCard: LinkStoreCard): Call<LinkNewCardResponse> {
        return mApiInterface.linkStoreCard(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), linkStoreCard)
    }

    fun unblockStoreCard(productOfferingId: String, requestBody: UnblockStoreCardRequestBody): Call<UnblockStoreCardResponse> {
        return mApiInterface.unblockStoreCard(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), productOfferingId, requestBody)
    }

    fun activateCreditCardRequest(requestBody: CreditCardActivationRequestBody): Call<CreditCardActivationResponse> {
        return mApiInterface.activateCreditCard(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), requestBody)
    }

    fun retrieveOTP(otpMethodType: OTPMethodType, productOfferingId: String): Call<RetrieveOTPResponse> {
        return mApiInterface.retrieveOTP(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), otpMethodType.name, productOfferingId)
    }

    fun validateOTP(validateOTPRequest: ValidateOTPRequest, productOfferingId: String): Call<ValidateOTPResponse> {
        return mApiInterface.validateOTP(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), validateOTPRequest, productOfferingId)
    }

    fun queryServiceCancelOrder(orderId: String): Call<CancelOrderResponse> {
        return mApiInterface.queryServiceCancelOrder(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), orderId)
    }

    fun getCreditCardDeliveryStatus(envelopeReference: String, productOfferingId: String): Call<CreditCardDeliveryStatusResponse> {
        return mApiInterface.cardDeliveryStatus(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), envelopeReference, productOfferingId)
    }

    fun getPossibleAddress(searchPhrase: String, productOfferingId: String, envelopeNumber: String): Call<PossibleAddressResponse> {
        return mApiInterface.possibleAddress(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), searchPhrase, envelopeNumber, productOfferingId)
    }

    fun getAvailableTimeSlots(envelopeReference: String, productOfferingId: String, x: String, y: String, shipByDate: String): Call<AvailableTimeSlotsResponse> {
        return mApiInterface.availableTimeSlots(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), envelopeReference, productOfferingId, x, y, shipByDate)
    }

    fun postScheduleDelivery(productOfferingId: String, envelopeNumber: String, schedule: Boolean, bookingReference: String, scheduleDeliveryRequest: ScheduleDeliveryRequest): Call<CreditCardDeliveryStatusResponse> {
        return mApiInterface.scheduleDelivery(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), productOfferingId, envelopeNumber, schedule, bookingReference,
            scheduleDeliveryRequest)
    }

    fun queryServicePostEvent(featureName: String?, appScreen: String?): Call<Response> {
        return mApiInterface.postEvent(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), featureName
                ?: "", appScreen ?: "")
    }

    fun queryServicePayUMethod(): Call<PaymentMethodsResponse> {
        return mApiInterface.getPaymentPAYUMethod(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken())
    }

    fun queryServicePostPayU(payUPay: PayUPay): Call<PayUResponse> {
        return mApiInterface.postPayUpPay(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), payUPay)
    }

    fun queryServicePaymentResult(request: PayUPayResultRequest): Call<PayUPayResultResponse> {
        return mApiInterface.getPaymentPayUResult(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), request.customer, request.payment_id, request.charge_id, request.status,
            request.productOfferingID)
    }

    fun validateSelectedSuburb(suburbId: String, isStore: Boolean): Call<ValidateSelectedSuburbResponse> {
        return mApiInterface.validateSelectedSuburb(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(), suburbId, isStore)
    }

    fun applyVouchers(vouchers: List<SelectedVoucher>): Call<ShoppingCartResponse> {
        return mApiInterface.applyVouchers(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), vouchers)
    }

    fun applyPromoCode(couponClaimCode: CouponClaimCode): Call<ShoppingCartResponse> {
        return mApiInterface.applyPromoCode(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), couponClaimCode)
    }

    fun removePromoCode(couponClaimCode: CouponClaimCode): Call<ShoppingCartResponse> {
        return mApiInterface.removePromoCode(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), couponClaimCode)
    }

    fun queryServicePayURemovePaymentMethod(paymenToken: String): Call<DeleteResponse> {
        return mApiInterface.payURemovePaymentMethod(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), paymenToken)
    }

    fun getSizeGuideContent(contentId: String): Call<SizeGuideResponse> {
        return mApiInterface.getSizeGuideContent(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(),
            getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), contentId)
    }

    fun getAccountsByProductOfferingId(productOfferingId: String): Call<AccountsResponse> {
        return mApiInterface.getAccountsByProductOfferingId(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(),
            getDeviceIdentityToken(), productOfferingId)
    }

    fun getLinkDeviceOtp(otpMethod: String): Call<RetrieveOTPResponse> {
        return mApiInterface.getLinkDeviceOTP(
            getApiId(),
            getSha1Password(),
            getDeviceManufacturer(),
            getDeviceModel(),
            getNetworkCarrier(),
            getOS(),
            getOsVersion(),
            "",
            "",
            getDeviceIdentityToken(),
            getSessionToken(),
            otpMethod)
    }

    fun changePrimaryDeviceApi(deviceIdentityId: String, otp: String?, otpMethod: String?): Call<ViewAllLinkedDeviceResponse> {
        return mApiInterface.changePrimaryDeviceApi(
            getApiId(),
            getSha1Password(),
            getDeviceManufacturer(),
            getDeviceModel(),
            getNetworkCarrier(),
            getOS(),
            getOsVersion(),
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
            getApiId(),
            getSha1Password(),
            getDeviceManufacturer(),
            getDeviceModel(),
            getNetworkCarrier(),
            getOS(),
            getOsVersion(),
            "",
            "",
            getSessionToken(),
            deviceName,
            body,
            otp,
            otpMethod)
    }

    fun getAllLinkedDevices(isForced: Boolean): Call<ViewAllLinkedDeviceResponse> {
       forceNetworkUpdate = isForced
       return mApiInterface.getAllLinkedDevices(
           getApiId(),
           getSha1Password(),
           getDeviceManufacturer(),
           getDeviceModel(),
           getNetworkCarrier(),
           getOS(),
           getOsVersion(),
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
            getApiId(),
            getSha1Password(),
            getDeviceManufacturer(),
            getDeviceModel(),
            getNetworkCarrier(),
            getOS(),
            getOsVersion(),
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
        return mApiInterface.confirmStoreCardEmail(getApiId(), getSha1Password(), getDeviceManufacturer(),
            getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "",
            getSessionToken(), getDeviceIdentityToken(),  body)
    }

    fun getVocSurvey(triggerEvent: VocTriggerEvent): Call<SurveyDetailsResponse> {
        return mApiInterface.getVocSurvey(
                apiId = getApiId(),
                sha1Password = getSha1Password(),
                deviceVersion = getDeviceManufacturer(),
                deviceModel = getDeviceModel(),
                network = getNetworkCarrier(),
                os = getOS(),
                osVersion = getOsVersion(),
                userAgent = "",
                userAgentVersion =  "",
                sessionToken = getSessionToken(),
                triggerEvent = triggerEvent.value
        )
    }

    fun submitVocSurveyReplies(surveyDetails: SurveyDetails, surveyAnswers: HashMap<Long, SurveyAnswer>): Call<Void> {
        return mApiInterface.submitVocSurveyReplies(
                apiId = getApiId(),
                sha1Password = getSha1Password(),
                deviceVersion = getDeviceManufacturer(),
                deviceModel = getDeviceModel(),
                network = getNetworkCarrier(),
                os = getOS(),
                osVersion = getOsVersion(),
                userAgent = "",
                userAgentVersion =  "",
                sessionToken = getSessionToken(),
                surveyId = surveyDetails.id,
                surveyReplies = SurveyRepliesBody(
                        surveyId = surveyDetails.id,
                        appInstanceId = Utils.getUniqueDeviceID(WoolworthsApplication.getInstance().applicationContext),
                        participantReplies = surveyAnswers.values.toList()
                )
        )
    }

    fun optOutVocSurvey(): Call<Void> {
        return mApiInterface.optOutVocSurvey(
                apiId = getApiId(),
                sha1Password = getSha1Password(),
                deviceVersion = getDeviceManufacturer(),
                deviceModel = getDeviceModel(),
                network = getNetworkCarrier(),
                os = getOS(),
                osVersion = getOsVersion(),
                userAgent = "",
                userAgentVersion =  "",
                sessionToken = getSessionToken(),
                optOutBody = SurveyOptOutBody(
                        appInstanceId = Utils.getUniqueDeviceID(WoolworthsApplication.getInstance().applicationContext)
                )
        )
    }
}