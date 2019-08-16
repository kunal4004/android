package za.co.woolworths.financial.services.android.models.network

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.chat.*
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.statement.*
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

object OneAppService : RetrofitConfig() {

    var forceNetworkUpdate: Boolean = false

    fun getConfig(): Call<ConfigResponse> = mApiInterface.getConfig(
            WoolworthsApplication.getApiId(),
            getSha1Password(),
            getDeviceManufacturer(),
            getDeviceModel(),
            getNetworkCarrier(),
            getOS(),
            getOsVersion(),
            getSessionToken(),
            WoolworthsApplication.getAppVersionName())

    fun login(loginRequest: LoginRequest): Call<LoginResponse> {
        return mApiInterface.login(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), loginRequest)
    }

    fun getAccounts(): Call<AccountsResponse> {
        return mApiInterface.getAccounts(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken())
    }

    fun authoriseLoan(authoriseLoanRequest: AuthoriseLoanRequest): Call<AuthoriseLoanResponse> {
        return mApiInterface.authoriseLoan(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), authoriseLoanRequest)
    }

    fun getAccountTransactionHistory(productOfferingId: String): Call<TransactionHistoryResponse> {
        return mApiInterface.getAccountTransactionHistory(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), productOfferingId)
    }

    fun getVouchers(): Call<VoucherResponse> {
        return mApiInterface.getVouchers(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken())
    }

    fun getLocations(lat: String, lon: String, searchString: String, radius: String?): Call<LocationResponse> {
        return if (radius != null && radius == "") {
            //This should never happen for now
            mApiInterface.getStoresLocation(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), lat, lon, searchString, radius)
        } else {

            mApiInterface.getStoresLocation(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), lat, lon, searchString)
        }
    }

    fun getLocationsItem(sku: String, startRadius: String, endRadius: String): Call<LocationResponse> {
        val loc = getMyLocation()
        return if (startRadius != null && startRadius == "") {
            //This should never happen for now
            mApiInterface.getStoresLocationItem(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", loc.getLatitude().toString(), loc.getLongitude().toString(), getSessionToken(), sku, startRadius, endRadius, true)
        } else {
            mApiInterface.getStoresLocationItem(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", loc.getLatitude().toString(), loc.getLongitude().toString(), getSessionToken(), sku, startRadius, endRadius, true)
        }
    }

    fun getMessagesResponse(pageSize: Int, pageNumber: Int): Call<MessageResponse> {
        return mApiInterface.getMessages(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), pageSize, pageNumber)
    }

    fun cliCreateApplication(offerRequest: CreateOfferRequest): Call<OfferActive> {
        return mApiInterface.cliCreateApplication(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), offerRequest)
    }

    fun cliUpdateApplication(offerRequest: CreateOfferRequest, cliId: String): Call<OfferActive> {
        return mApiInterface.cliUpdateApplication(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), cliId, offerRequest)
    }

    fun createOfferDecision(createOfferDecision: CLIOfferDecision, cliId: String): Call<OfferActive> {
        return mApiInterface.createOfferDecision(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), cliId, createOfferDecision)
    }

    fun getDeaBanks(): Call<DeaBanks> {
        return mApiInterface.getDeaBanks(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), 0, 0)
    }

    fun getBankAccountTypes(): Call<BankAccountTypes> {
        return mApiInterface.getBankAccountTypes(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), 0, 0)
    }

    fun getActiveOfferRequest(productOfferingId: String): Call<OfferActive> {
        return mApiInterface.getActiveOfferRequest(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), productOfferingId)
    }

    fun getDeleteMessagesResponse(id: String): Call<DeleteMessageResponse> {
        return mApiInterface.getDeleteresponse(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), id)
    }

    fun getReadMessagesResponse(readMessages: MessageReadRequest): Call<ReadMessagesResponse> {
        return mApiInterface.setReadMessages(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), readMessages)
    }

    fun cliEmailResponse(): Call<CLIEmailResponse> {
        return mApiInterface.cliSendEmailRquest(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken())
    }

    fun cliUpdateBankDetail(updateBankDetail: UpdateBankDetail): Call<UpdateBankDetailResponse> {
        return mApiInterface.cliUpdateBankRequest(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), updateBankDetail)
    }

    fun getResponseOnCreateUpdateDevice(device: CreateUpdateDevice): Call<CreateUpdateDeviceResponse> {
        return mApiInterface.createUpdateDevice(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), device)
    }

    fun issueLoan(issueLoan: IssueLoan): Call<IssueLoanResponse> {
        return mApiInterface.issueLoan(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), issueLoan)
    }

    fun addToList(addToListRequest: MutableList<AddToListRequest>, listId: String): Call<ShoppingListItemsResponse> {
        return mApiInterface.addToList(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), listId, addToListRequest)
    }

    fun getPromotions(): Call<PromotionsResponse> {
        return mApiInterface.getPromotions(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken())
    }

    fun getRootCategory(): Call<RootCategories> {
        return mApiInterface.getRootCategories(getOsVersion(), getApiId(), getOS(), getSha1Password(), getDeviceModel(), getNetworkCarrier(), getDeviceManufacturer(), "Android", getSessionToken())
    }

    fun getSubCategory(category_id: String): Call<SubCategories> {
        return mApiInterface.getSubCategory(getOsVersion(), getApiId(), getOS(), getSha1Password(), getDeviceModel(), getNetworkCarrier(), getDeviceManufacturer(), "Android", getSessionToken(), category_id)
    }

    fun getProvinces(): Call<ProvincesResponse> {
        return mApiInterface.getProvinces(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken())
    }

    fun getCartSummary(): Call<CartSummaryResponse> {
        return mApiInterface.getCartSummary(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken())
    }

    fun getSuburbs(locationId: String): Call<SuburbsResponse> {
        return mApiInterface.getSuburbs(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), locationId)
    }

    fun setSuburb(suburbId: String): Call<SetDeliveryLocationSuburbResponse> {
        val request = SetDeliveryLocationSuburbRequest(suburbId)
        return mApiInterface.setDeliveryLocationSuburb(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), request)
    }

    fun getProducts(requestParams: ProductsRequestParams): Call<ProductView> {
        val loc = getMyLocation()
        return if (Utils.isLocationEnabled(appContext())) {
            mApiInterface.getProducts(getOsVersion(), getDeviceModel(), getDeviceManufacturer(), getOS(), getNetworkCarrier(), getApiId(), "", "", getSha1Password(), loc.getLatitude(), loc.getLongitude(), getSessionToken(), requestParams.searchTerm, requestParams.searchType.value, requestParams.responseType.value, requestParams.pageOffset, Utils.PAGE_SIZE, requestParams.sortOption, requestParams.refinement)
        } else {
            mApiInterface.getProductsWithoutLocation(getOsVersion(), getDeviceModel(), getDeviceManufacturer(), getOS(), getNetworkCarrier(), getApiId(), "", "", getSha1Password(), getSessionToken(), requestParams.searchTerm, requestParams.searchType.value, requestParams.responseType.value, requestParams.pageOffset, Utils.PAGE_SIZE, requestParams.sortOption, requestParams.refinement)
        }
    }

    fun getFAQ(): Call<FAQ> {
        return mApiInterface.getFAQ(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken())
    }

    fun getCardDetails(): Call<CardDetailsResponse> {
        return mApiInterface.getCardDetails(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken())
    }

    fun getStatementResponse(statement: UserStatement): Call<StatementResponse> {
        return mApiInterface.getUserStatement(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), statement.productOfferingId, statement.accountNumber ?: "" , statement.startDate, statement.endDate)
    }

    fun sendStatementRequest(statement: SendUserStatementRequest): Call<SendUserStatementResponse> {
        return mApiInterface.sendUserStatement(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), statement)
    }

    fun addItemToCart(addToCart: MutableList<AddItemToCart>): Call<AddItemToCartResponse> {
        return mApiInterface.addItemToCart(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), addToCart)
    }

    fun getShoppingCart(): Call<ShoppingCartResponse> {
        return mApiInterface.getShoppingCart(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken())
    }

    fun getChangeQuantity(changeQuantity: ChangeQuantity): Call<ShoppingCartResponse> {
        return mApiInterface.changeQuantityRequest(getApiId(), getSha1Password(), getDeviceManufacturer(),
                getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "",
                "", getSessionToken(), changeQuantity.commerceId, changeQuantity)
    }

    fun removeCartItem(commerceId: String): Call<ShoppingCartResponse> {
        return mApiInterface.removeItemFromCart(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), commerceId)
    }

    fun removeAllCartItems(): Call<ShoppingCartResponse> {
        return mApiInterface.removeAllCartItems(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken())
    }

    fun productDetail(productId: String, skuId: String): Call<ProductDetailResponse> {
        val loc = getMyLocation()
        return if (Utils.isLocationEnabled(appContext())) {
            mApiInterface.productDetail(getOsVersion(), getDeviceModel(), getDeviceManufacturer(),
                    getOS(), getNetworkCarrier(), getApiId(), "", "",
                    getSha1Password(), loc.getLongitude(), loc.getLatitude(), getSessionToken(), productId, skuId)
        } else {
            mApiInterface.productDetail(getOsVersion(), getDeviceModel(), getDeviceManufacturer(),
                    getOS(), getNetworkCarrier(), getApiId(), "", "",
                    getSha1Password(), getSessionToken(), productId, skuId)
        }
    }

    fun getShoppingLists(): Call<ShoppingListsResponse> {
        return mApiInterface.getShoppingLists(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken())
    }

    fun createList(listName: CreateList): Call<ShoppingListsResponse> {
        return mApiInterface.createList(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), listName)
    }


    fun getShoppingListItems(listId: String): Call<ShoppingListItemsResponse> {
        return mApiInterface.getShoppingListItems(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), listId)
    }

    fun deleteShoppingList(listId: String): Call<ShoppingListsResponse> {
        return mApiInterface.deleteShoppingList(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), listId)
    }

    fun deleteShoppingListItem(listId: String, id: String, productId: String, catalogRefId: String): Call<ShoppingListItemsResponse> {
        return mApiInterface.deleteShoppingListItem(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), listId, id, productId, catalogRefId)
    }

    fun getInventorySku(multipleSku: String): Call<SkuInventoryResponse> {
        return mApiInterface.getInventorySKU(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), multipleSku)
    }

    fun getInventorySkuForStore(store_id: String, multipleSku: String): Call<SkusInventoryForStoreResponse> {
        return mApiInterface.getInventorySKUForStore(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), store_id, multipleSku)

    }

    fun getPDFResponse(getStatement: GetStatement): Call<ResponseBody> {
        return mApiInterface.getStatement(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), getStatement.docId, getStatement.productOfferingId, getStatement.docDesc)
    }

    fun postCheckoutSuccess(checkoutSuccess: CheckoutSuccess): Call<Void> {
        return mApiInterface.postCheckoutSuccess(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), checkoutSuccess)
    }

    fun getOrders(): Call<OrdersResponse> {
        return mApiInterface.getOrders(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken())
    }

    fun getOrderDetails(orderId: String): Call<OrderDetailsResponse> {
        return mApiInterface.getOrderDetails(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), orderId)
    }

    fun addOrderToList(orderId: String, orderToShoppingListRequestBody: OrderToShoppingListRequestBody): Call<OrderToListReponse> {
        return mApiInterface.addOrderToList(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), orderId, orderToShoppingListRequestBody)
    }

    fun getOrderTaxInvoice(taxNoteNumber: String): Call<OrderTaxInvoiceResponse> {
        return mApiInterface.getTaxInvoice(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getSessionToken(), taxNoteNumber)
    }

    fun getCreditCardToken(): Call<CreditCardTokenResponse> {
        return mApiInterface.getCreditCardToken(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken())
    }

    fun postBlockMyCard(blockCardRequestBody: BlockCardRequestBody, productOfferingId: String): Call<BlockMyCardResponse> {
        return mApiInterface.blockStoreCard(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), getOsVersion(), "", getSessionToken(), productOfferingId, blockCardRequestBody)
    }

    fun pollAgentsAvailable(): Observable<AgentsAvailableResponse> {
        return mApiInterface.pollAgentsAvailable(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken())
    }

    fun createChatSession(requestBody: CreateChatSession): Call<CreateChatSessionResponse> {
        return mApiInterface.createChatSession(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), requestBody)
    }

    fun pollChatSessionState(chatId: String): Observable<PollChatSessionStateResponse> {
        return mApiInterface.pollChatSessionState(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), chatId)
    }

    fun sendChatMessage(chatId: String, requestBody: SendMessageRequestBody): Call<SendChatMessageResponse> {
        return mApiInterface.sendChatMessage(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), chatId, requestBody)
    }

    fun endChatSession(chatId: String): Call<EndChatSessionResponse> {
        return mApiInterface.endChatSession(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), chatId)
    }

    fun userTyping(chatId: String): Call<UserTypingResponse> {
        return mApiInterface.userTyping(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), chatId, JsonObject())
    }

    fun userStoppedTyping(chatId: String): Call<UserTypingResponse> {
        return mApiInterface.userStoppedTyping(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), chatId, JsonObject())
    }

}