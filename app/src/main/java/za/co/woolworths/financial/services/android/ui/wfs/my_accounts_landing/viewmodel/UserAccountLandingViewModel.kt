package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.MessageResponse
import za.co.woolworths.financial.services.android.models.dto.account.FicaModel
import za.co.woolworths.financial.services.android.models.dto.account.InsuranceProducts
import za.co.woolworths.financial.services.android.models.dto.account.PetInsuranceModel
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.wfs.common.ConnectionState
import za.co.woolworths.financial.services.android.ui.wfs.core.FirebaseAnalyticsUserProperty
import za.co.woolworths.financial.services.android.ui.wfs.core.IFirebaseAnalyticsUserProperty
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.saveToLocalDatabase
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_biometrics.BiometricImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_biometrics.Biometrics
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_chat.logic.WChat
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_chat.logic.WChatImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_deeplink.DeepLinkingRedirection
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_deeplink.DeepLinkingRedirectionImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_fica.logic.FicaProducer
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_fica.logic.FicaProducerImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_message.data.IMessage
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_message.data.IMessageImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.OfferSectionUseCase
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.schema.OfferProductType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountOfferKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductKeys
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.Authenticated
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.NotAuthenticated
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.ProductDetails
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.logic.AccountRemoteRepository
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.logic.IMyAccountProductModel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.logic.MyAccountProductModel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.logic.MyProductsHandler
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.logic.MyProductsProducerImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.logic.ScheduleCreditCardDeliveryMain
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.logic.ScheduleCreditCardDeliveryMainImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_version_info.logic.ApplicationInfoImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_version_info.logic.IApplicationInfo
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_welcome.logic.UserInformationTransformer
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_welcome.logic.UserInformationTransformerImpl
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import javax.inject.Inject

@HiltViewModel
class UserAccountLandingViewModel @Inject constructor(
    private val deepLinking : DeepLinkingRedirectionImpl,
    private val product : MyProductsProducerImpl,
    private val offerUseCase: OfferSectionUseCase,
    private val authenticationImpl: MyAccountProductModel,
    private val applicationInfoImpl: ApplicationInfoImpl,
    private val userInformation: UserInformationTransformerImpl,
    private val creditCardDelivery : ScheduleCreditCardDeliveryMainImpl,
    private val userProperty : FirebaseAnalyticsUserProperty,
    private val biometric : BiometricImpl,
    private val chat : WChatImpl,
    private val messages : IMessageImpl,
    private val fica : FicaProducerImpl,
    val remote : AccountRemoteRepository
) : ViewModel(),
    IApplicationInfo by applicationInfoImpl,
    IMyAccountProductModel by authenticationImpl,
    UserInformationTransformer by userInformation,
    IFirebaseAnalyticsUserProperty by userProperty,
    ScheduleCreditCardDeliveryMain by creditCardDelivery,
    WChat by chat,
    Biometrics by biometric,
    MyProductsHandler by product,
    IMessage by messages,
    FicaProducer by fica,
    DeepLinkingRedirection by deepLinking {

    private var _mapOfMyOffers = mutableMapOf<AccountOfferKeys, CommonItem.OfferItem?>()

    private var userAccountResponse: UserAccountResponse? = null
    var accountProductCardsGroup : AccountProductCardsGroup? = null

    private var _mapOfFinalProductItems = mutableMapOf<String, AccountProductCardsGroup?>()
    val mapOfFinalProductItems: MutableMap<String, AccountProductCardsGroup?> = _mapOfFinalProductItems

    private val _fetchPetInsuranceState = MutableStateFlow(NetworkStatusUI<PetInsuranceModel>())
    val fetchPetInsuranceState = _fetchPetInsuranceState.asStateFlow()

    var onActivityForResultClicked = MutableLiveData<AccountProductCardsGroup?>()

    private val _messageState = MutableStateFlow(NetworkStatusUI<MessageResponse>())
    val messageState = _messageState.asStateFlow()

    private val _scheduleDeliveryNetworkState = MutableStateFlow(NetworkStatusUI<CreditCardDeliveryStatusResponse>())
    val scheduleDeliveryNetworkState = _scheduleDeliveryNetworkState.asStateFlow()

    private val _isDeeplinkParamsAvailable = MutableStateFlow(false)
    val isDeeplinkParamsAvailable = _isDeeplinkParamsAvailable.asStateFlow()

    private val _ficaDeliveryNetworkState = MutableSharedFlow<NetworkStatusUI<FicaModel>>()
    val ficaDeliveryNetworkState = _ficaDeliveryNetworkState.asSharedFlow()

    val isUserAuthenticated = mutableStateOf(isAuthenticated())

    private val _getAllUserAccounts = MutableStateFlow(NetworkStatusUI<UserAccountResponse>())
    val getAllUserAccounts = _getAllUserAccounts.asStateFlow()

    private val _getUserAccountsByProductOfferingId = MutableStateFlow(NetworkStatusUI<UserAccountResponse>())
    val getUserAccountsByProductOfferingId = _getUserAccountsByProductOfferingId.asStateFlow()

    val mapOfMyOffers: MutableMap<AccountOfferKeys, CommonItem.OfferItem?> = _mapOfMyOffers
    var isRefreshButtonRotating by mutableStateOf(false)
    var isAccountRefreshingTriggered by mutableStateOf(false)
    var isAccountFragmentVisible by mutableStateOf(false)
    var isBiometricPopupEnabled by mutableStateOf(false)
    var isAutoReconnectActivated: Boolean = false
    val errorResponse = MutableLiveData<ServerErrorResponse?>()

    init {
        initProductAndOfferItem()
    }

    private fun initProductAndOfferItem() {
        populateMapOfMyProducts()
        populateMapOfMyOffers()
    }

    fun setUserUnAuthenticated(resultCode: Int?) {
        if (resultCode == SSOActivity.SSOActivityResult.SIGNED_OUT.rawValue()) {
            initProductAndOfferItem()
            isUserAuthenticated.value = NotAuthenticated
        }
    }

    fun setUserAuthenticated(resultCode: Int?) {
        viewModelScope.launch {
            if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
                showShimmer(isC2User())
                queryAccountLandingService(true)
                isUserAuthenticated.value = Authenticated
                isBiometricPopupEnabled = isBiometricScreenNeeded()
            }
        }
    }

    private fun showShimmer(isVisible: Boolean) {
        _getAllUserAccounts.update {
            it.copy(
                data = null,
                isLoading = isVisible,
                hasError = false,
                errorMessage = null
            )
        }
    }

    private fun populateMapOfMyProducts() {
        _mapOfFinalProductItems.apply {
            clear()
            putAll(listOfDefaultProductItems())
        }
    }

    private fun populateMapOfMyOffers() {
        _mapOfMyOffers.apply {
            clear()
            putAll(offerUseCase.initialOfferList())
        }
    }

    fun getApplyNowState(accountNumberBin : String?) = product.getApplyNowState(accountNumberBin)

    private fun constructMapOfMyOffers() {
        val listOfOffers = offerUseCase.constructMapOfMyOffers(_mapOfFinalProductItems, _fetchPetInsuranceState)
        _mapOfMyOffers.apply {
            clear()
            putAll(listOfOffers)
        }
    }

    private fun queryUserAccountService(isRefreshing: Boolean? = false) {
        if (!isC2User()) {
            _mapOfFinalProductItems.clear()
            constructMapOfMyOffers()
            showShimmer(false)
            return
        }
        viewModelScope.launch {
            product.queryUserAccountService(isRefreshing, _getAllUserAccounts)
        }
    }

    fun handleUserAccountResponse(userAccountResponse: UserAccountResponse?) {
        this.userAccountResponse = userAccountResponse
        handleUserPropertiesOnGetAccountResponseSuccess(userAccountResponse)
        val mapOfMyProducts: MutableMap<String, AccountProductCardsGroup?> = mutableMapOf()

        val validProductList = userAccountResponse?.products ?: mutableListOf()
        val productDetailList = userAccountResponse?.accountList

        for (validItem in validProductList) {
            val productDetails = productDetailList?.firstOrNull { it.productGroupCode == validItem.productGroupCode }
            val product = transformSingleProductResult(validItem.productGroupCode, productDetails ?: validItem)
            if (productDetails == null) {
             when(product) {
                    is AccountProductCardsGroup.BlackCreditCard ->
                        product.retryOptions = product.retryOptions.copy(isRetryButtonEnabled = true)

                    is AccountProductCardsGroup.GoldCreditCard ->
                        product.retryOptions = product.retryOptions.copy(isRetryButtonEnabled = true)

                    is AccountProductCardsGroup.PersonalLoan ->
                        product.retryOptions = product.retryOptions.copy(isRetryButtonEnabled = true)

                    is AccountProductCardsGroup.SilverCreditCard ->
                        product.retryOptions = product.retryOptions.copy(isRetryButtonEnabled = true)

                    is AccountProductCardsGroup.StoreCard ->
                        product.retryOptions = product.retryOptions.copy(isRetryButtonEnabled = true)

                    else -> Unit
                }
            }
            mapOfMyProducts[validItem.productGroupCode] = product
        }

        if (mapOfMyProducts.isNotEmpty()) {
            addViewApplicationStatusIfNeeded(validProductList, mapOfMyProducts)
        }

        if (!isC2User()) {
            mapOfMyProducts.clear()
        }

        _mapOfFinalProductItems.clear()
        _mapOfFinalProductItems.putAll(mapOfMyProducts)

        if (isAccountFragmentVisible)
            _isDeeplinkParamsAvailable.update { true }

        constructMapOfMyOffers()

        stopLoading()
    }

    fun stopLoading() {
        isAccountRefreshingTriggered = false
        isAutoReconnectActivated = false
        isRefreshButtonRotating = false
    }

    fun fetchUserAccountByProductOfferingId(accountProductCardsGroup: AccountProductCardsGroup?) {
        accountProductCardsGroup ?: return
        this.accountProductCardsGroup = accountProductCardsGroup
        val productDetails = accountProductCardsGroup.productDetails
        viewModelScope.launch {
            productDetails?.productOfferingId?.let {productOfferingId ->
                product.queryUserAccountByProductOfferingId(
                    productOfferingId = productOfferingId,
                    _getUserAccountsByProductOfferingId
                )
            }
        }
    }

    fun setRetryButtonInProgress() {
        accountProductCardsGroup ?: return
        val productGroupCode = accountProductCardsGroup?.productDetails?.productGroupCode
        productGroupCode?.let { code ->
            _mapOfFinalProductItems[code] = accountProductCardsGroup
        }
    }

    fun setProductDetails(
        userAccountResponse: UserAccountResponse,
        productDetails: ProductDetails
    ) {
        val account = userAccountResponse.account

        // add missing product to userAccountResponse
        account?.let {
            handleUserPropertiesOnRetryProduct(productDetails.productGroupCode, it)
            userAccountResponse.accountList?.add(it)
        }

        val productGroupCode = productDetails.productGroupCode
        val productGroup = transformSingleProductResult(productGroupCode, account)
        _mapOfFinalProductItems[productGroupCode] = productGroup

    }

    fun queryServiceScheduleYourDelivery() {
        this.userAccountResponse ?: return
        viewModelScope.launch {
            queryServiceScheduleCreditCardDelivery(userAccountResponse, _scheduleDeliveryNetworkState)
        }
    }

    fun queryFicaRemoteService() {
        if (isAccountFragmentVisible) {
            viewModelScope.launch {
                getFicaRemoteService(_ficaDeliveryNetworkState)
            }
        }
    }

    private fun queryPetInsuranceProduct() {
        if (isAccountFragmentVisible) {
            viewModelScope.launch {
                product.queryPetInsuranceRemoteService(_fetchPetInsuranceState)
            }
        }
    }

    fun queryAccountLandingService(isApiUpdateForced: Boolean = true) {
        isAutoReconnectActivated = true
        queryUserAccountService(isRefreshing = isApiUpdateForced)
        queryPetInsuranceProduct()
    }

    fun queryUserMessagesService() {
        viewModelScope.launch {
            queryMessageRemoteService(_messageState)
        }
    }

    fun isC2UserOrMyProductItemExist(): Boolean {
        return isC2User() || _mapOfFinalProductItems.isNotEmpty()
    }

    fun listOfSignInItem() = listOfSignInItems(appVersion = getAppVersion())

    fun listOfSignedOutItem() = listOfSignedOutItems(appVersion = getAppVersion())

    fun isNowWfsUser(): Boolean = isNowWfsUser(userAccountResponse)

    fun performClick() {
        onActivityForResultClicked.postValue(accountProductCardsGroup)
    }

    fun getUserAccountResponse(): UserAccountResponse? = userAccountResponse

    fun findCreditCardProduct(): ProductDetails? {
        return getUserAccountResponse()?.accountList?.find {
            it.productGroupCode.equals(AccountProductKeys.GoldCreditCard.value, ignoreCase = true)
        }
    }

    fun networkAutoReconnect(
        isAccountLoading: Boolean,
        networkStatus: ConnectionState,
        context: Context?
    ) {
        if (isAccountLoading && isAutoReconnectActivated) {
            when (networkStatus) {
                ConnectionState.Connected,
                ConnectionState.Available -> Unit//queryAccountLandingService(  true)
                ConnectionState.Unavailable -> ErrorHandlerView(context).showToast()
            }
        }
    }

    fun resetUnreadMessageCount() {
        val messageResponse = _messageState.value.data
        messageResponse?.unreadCount = 0
        _messageState.update {
            it.copy(
                data = messageResponse
            )
        }
    }

    fun convertProductToAccountModel(products: MutableList<ProductDetails>?): List<Account> {
        products?: return listOf()
        val gson = Gson()
        val jsonString = gson.toJson(products)
        val accountListType = object : TypeToken<List<Account>>() {}.type
        return gson.fromJson(jsonString, accountListType)
    }

    fun onScheduleCreditCardDeliveryResponse(creditCardDeliveryResponse : CreditCardDeliveryStatusResponse?)
     =  onGetCreditCardDeliveryStatusSuccess(userAccountResponse = userAccountResponse, creditCardDeliveryResponse)


    override fun onCleared() {
        super.onCleared()
         saveToLocalDatabase(SessionDao.KEY.SCHEDULE_CREDIT_CARD_DELIVERY_ON_ACCOUNT_LANDING, null)
    }

    fun getPetInsuranceMobileConfig() = product.getPetInsuranceFromMobileConfig()

    fun isAccountFragmentVisible(isVisible: Boolean) {
        isAccountFragmentVisible = isVisible
    }

    fun setUserAuthentication(){
        isUserAuthenticated.value = isAuthenticated()
    }

    fun getProductByProductGroupCode(productGroupCode : String?): AccountProductCardsGroup? {
        return mapOfFinalProductItems[productGroupCode]
    }

    fun resetDeepLinkParams() {
        _isDeeplinkParamsAvailable.value = false
    }

    fun handlePetInsuranceResult(petModel: PetInsuranceModel,
                                 petAwarenessModelNotCovered : (InsuranceProducts) ->  Unit) {
        // Determine whether Pet insurance model is displayed in product section
        product.setPetInsuranceResult(
            userAccountResponse = userAccountResponse,
            mapOfMyProducts = _mapOfFinalProductItems,
            model= petModel,
            petAwarenessModelNotCovered = petAwarenessModelNotCovered)

        // Determine whether Pet insurance model is displayed in offer section
        if(_mapOfFinalProductItems[AccountOfferKeys.PetInsurance.value] == null && !_fetchPetInsuranceState.value.hasError) {
            val tempOfferList = _mapOfMyOffers.toMutableMap()
            _mapOfMyOffers.clear()
            _mapOfMyOffers[AccountOfferKeys.PetInsurance] = OfferProductType.PetInsurance.value()
            tempOfferList.forEach { (key, value) ->
                _mapOfMyOffers[key] = value
            }

        }
    }
}