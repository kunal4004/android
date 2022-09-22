package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.app.Activity
import android.net.UrlQuerySanitizer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.awfs.coordination.R
import com.google.gson.Gson
import retrofit2.Call
import za.co.absa.openbankingapi.woolworths.integration.dto.PMARedirection
import za.co.absa.openbankingapi.woolworths.integration.dto.PayUResponse
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.pma.DeleteResponse
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.helper.BeginPayMyAccountJourneyActionImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.helper.PMATrackFirebaseEvent
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.helper.PayMyAccountPresenter
import za.co.woolworths.financial.services.android.util.eliteplan.ElitePlanModel
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.wenum.PMAVendorCardEnum
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent
import java.net.ConnectException
import java.util.*
import javax.annotation.Nullable

class PayMyAccountViewModel : ViewModel() {

    private var mQueryServicePostPayU: Call<PayUResponse>? = null
    private var mQueryServiceDeletePaymentMethod: Call<DeleteResponse>? = null
    private var mQueryServiceGetPaymentMethods: Call<PaymentMethodsResponse>? = null
    private var paymentMethodsResponse: MutableLiveData<PaymentMethodsResponse?> = MutableLiveData()
    private var onDialogDismiss: MutableLiveData<OnNavigateBack> = MutableLiveData()
    private val pmaFirebaseEvent: PMATrackFirebaseEvent = PMATrackFirebaseEvent()
    private var addCardResponse: MutableLiveData<AddCardResponse> = MutableLiveData()
    private var isQueryServiceGetRedirectionCompleted: Boolean = false
    var isAddNewCardFormLoaded = false
    var mSelectExpiredPaymentMethod : GetPaymentMethod? = null
    var isQueryPayUPaymentMethodComplete :Boolean = false
    var elitePlanModel : ElitePlanModel? = null

    var pmaCardPopupModel: MutableLiveData<PMACardPopupModel?> = MutableLiveData()
    var queryPaymentMethod: MutableLiveData<Boolean> = MutableLiveData()
    var deleteCardList: MutableList<Pair<GetPaymentMethod?, Int>>? = mutableListOf()
    private var payUPayResultRequest: MutableLiveData<PayUPayResultRequest> = MutableLiveData()

   val payMyAccountPresenter : PayMyAccountPresenter =  PayMyAccountPresenter(BeginPayMyAccountJourneyActionImpl(this))

    var pma3dSecureRedirection: PMARedirection? = null

    enum class PAYUMethodType { CREATE_USER, CARD_UPDATE, ERROR }

    sealed class OnNavigateBack {
        object Retry : OnNavigateBack()
        data class Remove (var position : Int, var isCardExpired : Boolean = false) : OnNavigateBack()
        object Add : OnNavigateBack()
        object None : OnNavigateBack()
        object MaxCardLimit : OnNavigateBack()

    }
    companion object {
        const val DEFAULT_RAND_CURRENCY = "R 0.00"
        const val MAX_AMOUNT_LIMIT = 50000
    }

    private fun createCard(): AddCardResponse {
        val paymentMethod = getSelectedPaymentMethodCard()
        val cvvNumber = getCVVNumber()
        val expiryDate = paymentMethod?.expirationDate?.split("/")
        val expDate = expiryDate?.get(0) ?: ""
        val expYear = expiryDate?.get(1) ?: ""

        val pmaCard = PMACard(paymentMethod?.cardNumber
                ?: "", "", expDate, expYear, cvvNumber ?: "0", 1, paymentMethod?.vendor
                ?: "", paymentMethod?.type ?: "")
        return AddCardResponse(paymentMethod?.token ?: "", pmaCard, false)
    }
    fun getSelectedPosition() = getCardDetail()?.selectedCardPosition ?: 0
    fun getPaymentMethodList(): MutableList<GetPaymentMethod>? {
        val cardDetail = getCardDetail()
        val paymentList = cardDetail?.paymentMethodList
        val selectedPosition = cardDetail?.selectedCardPosition ?: 0
        paymentList?.forEach {
            it.isCardChecked = false
        }
        if (paymentList?.isNotEmpty() == true &&
            paymentList.size > selectedPosition) {
            paymentList[selectedPosition].isCardChecked = true
        }
        return paymentList
    }

    fun isPaymentMethodListChecked(): Boolean {
        return getPaymentMethodList()?.filter { it.isCardChecked }?.isNullOrEmpty() ?: false
    }

    fun setCVVNumber(number: String?) {
        val cardDetail = getCardDetail()
        cardDetail?.cvvNumber = number
        setPMACardInfo(cardDetail)
    }

    private fun getCVVNumber() = getCardDetail()?.cvvNumber

    fun getSelectedPaymentMethodCard(): GetPaymentMethod? {
        val paymentMethodList: MutableList<GetPaymentMethod>? = getPaymentMethodList()
        if (paymentMethodList?.size ?: 0 > 0) {
            val cardDetail = getCardDetail()
            val selectedPosition = cardDetail?.selectedCardPosition ?: 0

            getPaymentMethodList()?.forEach { it.isCardChecked = false }

            paymentMethodList?.get(selectedPosition)?.isCardChecked = true

            return paymentMethodList?.get(selectedPosition)
        }
        return null
    }

    fun setPaymentMethodsResponse(paymentMethodResponse: PaymentMethodsResponse?) {
        val cardDetail = getCardDetail()
        val selectedPosition = getCardDetail()?.selectedCardPosition
        val cardLists = paymentMethodResponse?.paymentMethods
        if (cardLists?.size ?: 0 > 0)
            cardLists?.get(selectedPosition ?: 0)?.isCardChecked = true
        cardDetail?.paymentMethodList = cardLists

        cardDetail?.payuMethodType = if (cardLists?.isNullOrEmpty() == true) PAYUMethodType.CREATE_USER else PAYUMethodType.CARD_UPDATE

        setPMACardInfo(cardDetail)
        paymentMethodsResponse.value = paymentMethodResponse
    }

    fun getPaymentMethodType(): PAYUMethodType? = getCardDetail()?.payuMethodType

    fun setPMACardInfo(cardPopupModel: PMACardPopupModel?) {
        pmaCardPopupModel.value = cardPopupModel
    }

    fun setPMACardInfo(card: String) {
        pmaCardPopupModel.value = Gson().fromJson(card, PMACardPopupModel::class.java)
    }

    fun getCardDetail(): PMACardPopupModel? = pmaCardPopupModel.value


    fun getCardDetailInStringFormat(): String? = Gson().toJson(getCardDetail())

    fun setNavigationResult(onDismiss: OnNavigateBack) {
        onDialogDismiss.value = onDismiss
    }

    fun getNavigationResult(): MutableLiveData<OnNavigateBack> = onDialogDismiss


    fun queryServicePayUPaymentMethod(onSuccessResult: (MutableList<GetPaymentMethod>?) -> Unit, onSessionExpired: (String?) -> Unit, onGeneralError: (String) -> Unit, onFailureHandler: (Throwable?) -> Unit) {
        var payUMethodType: PAYUMethodType
        mQueryServiceGetPaymentMethods = request(OneAppService.queryServicePayUMethod(), object : IGenericAPILoaderView<Any> {
            override fun onSuccess(response: Any?) {
                (response as? PaymentMethodsResponse)?.apply {
                    setPaymentMethodsResponse(this)
                    when (httpCode) {
                        AppConstant.HTTP_OK -> {
                            payUMethodType = when (paymentMethods?.size ?: 0 > 0 || paymentMethods?.isNullOrEmpty() == false) {
                                true -> PAYUMethodType.CARD_UPDATE
                                else -> PAYUMethodType.CREATE_USER
                            }

                            onSuccessResult(paymentMethods)
                        }

                        AppConstant.HTTP_SESSION_TIMEOUT_400 -> {
                            val code = this.response.code
                            payUMethodType = when (code.startsWith("P0453")) {
                                true -> PAYUMethodType.CREATE_USER
                                else -> PAYUMethodType.ERROR
                            }

                            onSuccessResult(paymentMethods)
                        }

                        AppConstant.HTTP_SESSION_TIMEOUT_440  -> {
                            payUMethodType = PAYUMethodType.ERROR
                            onSessionExpired(this.response.stsParams)
                        }

                        else -> {
                            payUMethodType = PAYUMethodType.ERROR
                            onGeneralError(this.response.desc)
                        }
                    }
                    val cardInfo = getCardDetail()
                    val updatedCard = PMACardPopupModel(cardInfo?.amountEntered, paymentMethods, cardInfo?.account, payUMethodType, cardInfo?.selectedCardPosition
                            ?: 0)
                    setPMACardInfo(updatedCard)
                }
            }

            override fun onFailure(error: Throwable?) {
                super.onFailure(error)
                onFailureHandler(error)
            }
        })
    }

    fun isPaymentMethodListSizeLimitedToTenItem(): Boolean = getPaymentMethodList()?.size ?: 0 >= 10

    fun getProductGroupCode(): String = getAccount()?.productGroupCode ?: ""

    fun getProductOfferingId(): Int? = getAccount()?.productOfferingId

    private fun getProductOfferingIdInStringFormat(): String? = getAccount()?.productOfferingId?.toString()

    fun getApplyNowState() = getCardDetail()?.account?.first

    fun getProductLabelId() = when (getApplyNowState()) {
        ApplyNowState.STORE_CARD -> R.string.store_card_title
        ApplyNowState.PERSONAL_LOAN -> R.string.personal_loan_card_title
        else ->  R.string.credit_card_title
    }

    fun getAccountWithApplyNowState() = getCardDetail()?.account

    fun getAccount() = getCardDetail()?.account?.second

    fun isAccountChargedOff(): Boolean {
        getAccount()?.let {
            return !it.productOfferingGoodStanding && it.productOfferingStatus.equals(Utils.ACCOUNT_CHARGED_OFF, ignoreCase = true)
        }
        return false
    }

    fun getOverdueAmount(): String? {
        return getAccount()?.amountOverdue?.let { formatAndRemoveNegativeSymbol(it) }?.replace("R  ","R ")
    }

    fun getTotalAmountDue(): String? {
        return getAccount()?.totalAmountDue?.let { formatAndRemoveNegativeSymbol(it) }?.replace("R  ","R ")
    }

    fun getCurrentBalance(): String? {
        return getAccount()?.currentBalance?.let { formatAndRemoveNegativeSymbol(it) }?.replace("R  ","R ")
    }

    fun getSavedAmount(): String? {
        return  "R " + elitePlanModel?.discountAmount
    }
    fun getDiscountAmount(): String? {
        return  "R " + elitePlanModel?.settlementAmount
    }

    private fun formatAndRemoveNegativeSymbol(amount: Int): String? {
        return Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(CurrencyFormatter.formatAmountToRandAndCent(amount), 1))
    }

    //Disable change button when amount is R0.00
    fun isChangeIconEnabled(amountEntered: String?): Boolean {
        return amountEntered != DEFAULT_RAND_CURRENCY
    }

    fun isConfirmPaymentButtonEnabled(cvvLength: Int, amountEntered: String?): Boolean {
        return cvvLength > 2 &&
                isChangeIconEnabled(amountEntered) &&
                isAmountBelowMaxLimit(amountEntered)
    }

    fun isMaxCVVLength(size: Int): Boolean {
        return size == 3
    }

    fun isSelectedCardExpired(): Boolean {
        return getSelectedPaymentMethodCard()?.cardExpired == true
    }

    fun getVendorCardDrawableId(vendor: String?): Int {
        return when (PMAVendorCardEnum.getCard(vendor)) {
            PMAVendorCardEnum.VISA -> R.drawable.card_visa
            PMAVendorCardEnum.MASTERCARD -> R.drawable.card_mastercard
            else -> 0
        }
    }

    fun getVendorCardLargeDrawableId() : Int {
        return when (PMAVendorCardEnum.getCard(mSelectExpiredPaymentMethod?.vendor)) {
            PMAVendorCardEnum.VISA -> R.drawable.card_visa_large
            PMAVendorCardEnum.MASTERCARD -> R.drawable.card_mastercard_large
            else -> 0
        }
    }

    fun updateAmountEntered(amountEntered: String?): String {
        val overdueAmount = if (getOverdueAmount()?.contains("R ")==true) getOverdueAmount() else "R ${getOverdueAmount()}"
        return if (amountEntered.isNullOrEmpty() || amountEntered == DEFAULT_RAND_CURRENCY) overdueAmount ?: DEFAULT_RAND_CURRENCY else amountEntered
    }

    fun isPaymentListEmpty(paymentMethodList: MutableList<GetPaymentMethod>?): Boolean {
        return paymentMethodList?.isEmpty() == true
    }

    fun triggerFirebaseEventForEditAmount(activity: Activity) {
        getAccount()?.productGroupCode?.toLowerCase(Locale.getDefault())?.let { productGroupCode -> pmaFirebaseEvent.sendFirebaseEventForAmountEdit(productGroupCode, activity) }
    }

    fun triggerFirebaseEventForPaymentComplete(activity: Activity) {
        getAccount()?.productGroupCode?.toLowerCase(Locale.getDefault())?.let { productGroupCode -> pmaFirebaseEvent.sendFirebaseEventForPaymentComplete(productGroupCode, activity) }
    }

    fun getVocTriggerEventPaymentOptions(): VocTriggerEvent? {
        getAccount()?.productGroupCode?.let {
            return when {
                it.equals(AccountsProductGroupCode.STORE_CARD.groupCode, ignoreCase = true) -> {
                    VocTriggerEvent.CHAT_SC_PAYMENTOPTIONS
                }
                it.equals(AccountsProductGroupCode.PERSONAL_LOAN.groupCode, ignoreCase = true) -> {
                    VocTriggerEvent.CHAT_PL_PAYMENTOPTIONS
                }
                else -> {
                    VocTriggerEvent.CHAT_CC_PAYMENTOPTIONS
                }
            }
        } ?: kotlin.run {
            return null
        }
    }

    fun getVocTriggerEventMyAccounts(): VocTriggerEvent? {
        getAccount()?.productGroupCode?.let {
            return when {
                it.equals(AccountsProductGroupCode.STORE_CARD.groupCode, ignoreCase = true) -> {
                    VocTriggerEvent.CHAT_SC_MYACCOUNTS
                }
                it.equals(AccountsProductGroupCode.PERSONAL_LOAN.groupCode, ignoreCase = true) -> {
                    VocTriggerEvent.CHAT_PL_MYACCOUNTS
                }
                else -> {
                    VocTriggerEvent.CHAT_CC_MYACCOUNTS
                }
            }
        } ?: kotlin.run {
            return null
        }
    }

    @Nullable
    fun getAddNewCardUrl(): String? {
        return AppConfigSingleton.mPayMyAccount?.addCardUrl(getProductGroupCode())
    }

    fun setAddCardResponse(addCardResponse: AddCardResponse) {
        this.addCardResponse.value = addCardResponse
    }

    private fun getAddCardResponse() = addCardResponse.value ?: createCard()

    fun setSaveAndPayCardNow(isChecked: Boolean?) {
        val addCardResponse = getAddCardResponse()
        addCardResponse.saveChecked = isChecked ?: false
        setAddCardResponse(addCardResponse)
    }

    fun queryServiceDeletePaymentMethod(card: GetPaymentMethod?, position: Int, result: () -> Unit, failure: () -> Unit) {
        deleteCardList?.add(Pair(card, position))
        mQueryServiceDeletePaymentMethod = request(OneAppService.queryServicePayURemovePaymentMethod(card?.token ?: ""), object : IGenericAPILoaderView<Any> {
            override fun onSuccess(response: Any?) {
                (response as? DeleteResponse)?.apply {
                    when (httpCode) {
                        AppConstant.HTTP_OK -> showResultOnEmptyList(result)
                        else -> {
                            showResultOnEmptyList(result)
                            failure()}
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                when (error) {
                    is ConnectException -> {
                        deleteCardList?.clear()
                        result()
                    }
                    else -> showResultOnEmptyList(result)
                }
            }
        })
    }

    private fun showResultOnEmptyList(result: () -> Unit) {
        deleteCardList?.removeLast()
        if (isDeleteCardListEmpty()) result()
    }

    fun isDeleteCardListEmpty() = deleteCardList?.isEmpty() == true

    override fun onCleared() {
        cancelRetrofitRequest(mQueryServiceGetPaymentMethods)
        cancelRetrofitRequest(mQueryServiceDeletePaymentMethod)
        cancelRetrofitRequest(mQueryServicePostPayU)
        super.onCleared()
    }

    fun getAmountEntered(): String {
        val cardInfo = getCardDetail()
        val account = getAccount()
        return if (cardInfo?.paymentMethodList?.isEmpty() == true) {
            if (isAccountChargedOff()) account?.currentBalance?.toString() ?: ""
            else account?.amountOverdue?.toString() ?: ""
        } else {
            cardInfo?.amountEntered ?: ""
        }
    }

    fun convertRandFormatToDouble(item: String?): Double {
        val number: String? = item?.replace("[R ]".toRegex(), "")
        return if (number.isNullOrEmpty()) 0.0 else number.toDouble()
    }

    fun convertRandFormatToInt(item: String?): Int {
        val number: String? = item?.replace("[,.R$ ]".toRegex(), "")
        return if (number.isNullOrEmpty()) 0 else number.toInt()
    }

    fun validateAmountEntered(amount: Double, minAmount: () -> Unit, maxAmount: () -> Unit, validAmount: () -> Unit) {
        when {
            amount < 1.toDouble() -> minAmount()
            amount > 50000.toDouble() -> maxAmount()
            else -> validAmount()
        }
    }

    fun switchToConfirmPaymentOrDoneButton(buttonLabel: String?, done: () -> Unit, confirmPayment: () -> Unit) {
        when (buttonLabel) {
            bindString(R.string.done) -> done()
            else -> confirmPayment()
        }
    }

    fun resetAmountEnteredToDefault() {
        val card = getCardDetail()
        if (isAccountChargedOff()) {
            if (elitePlanModel !=null ){
                card?.amountEntered = elitePlanModel!!.settlementAmount
            }else{
                card?.amountEntered = getCurrentBalance()
            }
        } else {
            card?.amountEntered = getOverdueAmount()
        }
        setPMACardInfo(card)
    }

    private fun payURequestBody(): PayUPay {

        val cardInfo = getCardDetail()
        val amountEntered = cardInfo?.amountEnteredInInt() ?: 0
        val cardDetailArgs: AddCardResponse? = getAddCardResponse()

        val creditCardCVV = cardDetailArgs?.card?.cvv ?: getCVVNumber()
        val token = cardDetailArgs?.token
        val type = cardDetailArgs?.card?.type
        val isSaveCardChecked = cardDetailArgs?.saveChecked
        val currency = "ZAR"

        val account = cardInfo?.account?.second
        // Select absaCardToken for credit card products and account number for personal loan and store card
        val accountNumber = if (account?.productGroupCode?.equals(AccountsProductGroupCode.CREDIT_CARD.groupCode, ignoreCase = true) == true)
            account.cards?.get(0)?.absaCardToken ?: ""
        else
            account?.accountNumber ?: ""
        val productOfferingId = account?.productOfferingId ?: 0
        val paymentMethod = PayUPaymentMethod(token ?: "", creditCardCVV ?: "", type ?: "")

        return PayUPay(amountEntered, currency, productOfferingId, isSaveCardChecked ?: false, paymentMethod, accountNumber)
    }

    // Retrieve 3d secure merchant url
    fun queryServicePostPayU(result: (PayUResponse?) -> Unit, stsParams: (String?) -> Unit, generalHttpCodeFailure: (String?) -> Unit, failure: (Throwable?) -> Unit) {
        val payURequestBody = payURequestBody()
        mQueryServicePostPayU = request(OneAppService.queryServicePostPayU(payURequestBody), object : IGenericAPILoaderView<Any> {

            override fun onSuccess(response: Any?) {
                (response as? PayUResponse)?.apply {
                    isQueryServiceGetRedirectionCompleted = true
                    when (httpCode) {
                        AppConstant.HTTP_OK -> {
                            pma3dSecureRedirection = this.redirection
                            result(this)
                        }
                        AppConstant.HTTP_SESSION_TIMEOUT_440 -> this.response.stsParams?.let { params -> stsParams(params) }
                        else -> this.response.desc?.let { desc -> generalHttpCodeFailure(desc) }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                isQueryServiceGetRedirectionCompleted = false
                failure(error)
            }
        })
    }

    fun isRedirectionAPICompleted() = isQueryServiceGetRedirectionCompleted

    fun getMerchantSiteAndMerchantUrl(): Pair<String?, String?> {
        val merchantSiteUrl = pma3dSecureRedirection?.merchantSiteUrl?.replace("[\\u003d]".toRegex(), "=")
                ?: ""
        val merchantUrl = pma3dSecureRedirection?.url?.replace("[\\u003d]".toRegex(), "=") ?: ""
        return Pair(merchantSiteUrl, merchantUrl)
    }

    fun constructPayUPayResultCallback(url: String?, stopLoading: () -> Unit, result: () -> Unit) {

        if (getMerchantSiteAndMerchantUrl().first?.let { url?.contains(it) } == true) {
            stopLoading()

            val merchantSiteURI = url?.let { UrlQuerySanitizer(it) }
            val customer = merchantSiteURI?.getValue("customer") ?: ""
            val paymentId = merchantSiteURI?.getValue("payment_id") ?: ""
            val chargeId = merchantSiteURI?.getValue("charge_id") ?: ""
            val status = merchantSiteURI?.getValue("status") ?: ""
            val productOfferingId = getProductOfferingIdInStringFormat() ?: ""

            val output = PayUPayResultRequest(customer, paymentId, chargeId, status, productOfferingId)
            payUPayResultRequest.value = output
            result()
        }
    }

    fun getPayUPayResultRequest(): PayUPayResultRequest?  = payUPayResultRequest.value

    fun getPayOrSaveNowCardDetails(): Triple<CharSequence?, String, String>? {
        val addCardResponse = getAddCardResponse()
        addCardResponse.card.apply {
            val cardHolderName = KotlinUtils.capitaliseFirstWordAndLetters(name_card)
            val expiredMonthYear = "$exp_month / $exp_year"
            val maskedCardNumber = "**** **** **** $number"
            return Triple(cardHolderName, expiredMonthYear, maskedCardNumber)
        }
    }

    fun isAmountBelowMaxLimit(amount: String?): Boolean {
        return convertRandFormatToDouble(amount) <= MAX_AMOUNT_LIMIT
    }
}