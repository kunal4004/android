package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.awfs.coordination.R
import com.google.gson.Gson
import retrofit2.Call
import za.co.absa.openbankingapi.woolworths.integration.dto.PMARedirection
import za.co.absa.openbankingapi.woolworths.integration.dto.PayUResponse
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.pma.DeleteResponse
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.helper.PMATrackFirebaseEvent
import za.co.woolworths.financial.services.android.util.*
import java.net.ConnectException
import java.util.*

class PayMyAccountViewModel : ViewModel() {

    private var mQueryServicePostPayU: Call<PayUResponse>? = null
    private var mQueryServiceDeletePaymentMethod: Call<DeleteResponse>? = null
    private var mQueryServiceGetPaymentMethods: Call<PaymentMethodsResponse>? = null
    private var paymentMethodsResponse: MutableLiveData<PaymentMethodsResponse?> = MutableLiveData()
    private var cvvNumber: MutableLiveData<String> = MutableLiveData()
    private var onDialogDismiss: MutableLiveData<OnBackNavigation> = MutableLiveData()
    private val pmaFirebaseEvent: PMATrackFirebaseEvent = PMATrackFirebaseEvent()
    private var addCardResponse: MutableLiveData<AddCardResponse> = MutableLiveData()
    private var isQueryServiceGetRedirectionCompleted: Boolean = false

    var paymentAmountCard: MutableLiveData<PaymentAmountCard?> = MutableLiveData()
    var queryPaymentMethod: MutableLiveData<Boolean> = MutableLiveData()
    var deleteCardList: MutableList<Pair<GetPaymentMethod?, Int>>? = mutableListOf()

    var pma3dSecureRedirection: PMARedirection? = null

    enum class PAYUMethodType { CREATE_USER, CARD_UPDATE, ERROR }
    enum class OnBackNavigation { RETRY, REMOVE, ADD, NONE, MAX_CARD_LIMIT } // TODO: Navigation graph: Communicate result from dialog to fragment destination

    companion object {
        const val DEFAULT_RAND_CURRENCY = "R 0.00"
    }

    fun createCard(): Pair<Pair<ApplyNowState, Account>?, AddCardResponse> {
        val paymentMethod = getSelectedPaymentMethodCard()
        val selectedAccountProduct = getCardDetail()?.account
        val cvvNumber = getCVVNumber()
        val expiryDate = paymentMethod?.expirationDate?.split("/")
        val expDate = expiryDate?.get(0) ?: ""
        val expYear = expiryDate?.get(1) ?: ""

        val pmaCard = PMACard(paymentMethod?.cardNumber
                ?: "", "", expDate, expYear, cvvNumber, 1, paymentMethod?.vendor
                ?: "", paymentMethod?.type ?: "")
        return Pair(selectedAccountProduct, AddCardResponse(paymentMethod?.token
                ?: "", pmaCard, false))
    }

    fun getPaymentMethodList(): MutableList<GetPaymentMethod>? {
        val cardDetail = getCardDetail()
        val paymentList = cardDetail?.paymentMethodList
        val selectedPosition = cardDetail?.selectedCardPosition ?: 0
        paymentList?.forEach {
            it.isCardChecked = false
        }
        if (paymentList?.size ?: 0 > 0)
            paymentList?.get(selectedPosition)?.isCardChecked = true
        return paymentList
    }

    fun isPaymentMethodListChecked(): Boolean {
        val list = getPaymentMethodList()
        return list?.filter { it.isCardChecked }?.isNullOrEmpty() ?: false
    }

    fun setCVVNumber(number: String) {
        cvvNumber.value = number
    }

    private fun getCVVNumber() = cvvNumber.value ?: ""

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

    fun setPMACardInfo(card: PaymentAmountCard?) {
        paymentAmountCard.value = card
    }

    fun setPMACardInfo(card: String) {
        paymentAmountCard.value = Gson().fromJson(card, PaymentAmountCard::class.java)
    }

    fun getCardDetail(): PaymentAmountCard? = paymentAmountCard.value

    fun getCardDetailInStringFormat(): String? = Gson().toJson(getCardDetail())

    fun setNavigationResult(onDismiss: OnBackNavigation) {
        onDialogDismiss.value = onDismiss
        onDialogDismiss.value = OnBackNavigation.NONE
    }

    fun getNavigationResult(): MutableLiveData<OnBackNavigation> {
        return onDialogDismiss
    }

    fun queryServicePayUPaymentMethod(onSuccessResult: (MutableList<GetPaymentMethod>?) -> Unit, onSessionExpired: (String?) -> Unit, onGeneralError: (String) -> Unit, onFailureHandler: (Throwable?) -> Unit) {
        var payUMethodType: PAYUMethodType
        mQueryServiceGetPaymentMethods = request(OneAppService.queryServicePayUMethod(), object : IGenericAPILoaderView<Any> {
            override fun onSuccess(response: Any?) {
                (response as? PaymentMethodsResponse)?.apply {
                    setPaymentMethodsResponse(this)
                    when (httpCode) {
                        200 -> {
                            payUMethodType = when (paymentMethods?.size ?: 0 > 0 || paymentMethods?.isNullOrEmpty() == false) {
                                true -> PAYUMethodType.CARD_UPDATE
                                else -> PAYUMethodType.CREATE_USER
                            }

                            onSuccessResult(paymentMethods)
                        }

                        400 -> {
                            val code = this.response.code
                            payUMethodType = when (code.startsWith("P0453")) {
                                true -> PAYUMethodType.CREATE_USER
                                else -> PAYUMethodType.ERROR
                            }

                            onSuccessResult(paymentMethods)
                        }

                        440 -> {
                            payUMethodType = PAYUMethodType.ERROR
                            onSessionExpired(this.response.stsParams)
                        }

                        else -> {
                            payUMethodType = PAYUMethodType.ERROR
                            onGeneralError(this.response.desc)
                        }
                    }
                    val cardInfo = getCardDetail()
                    val updatedCard = PaymentAmountCard(cardInfo?.amountEntered, paymentMethods, cardInfo?.account, payUMethodType, cardInfo?.selectedCardPosition
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

    private fun getProductGroupCode(): String = getAccount()?.productGroupCode ?: ""

    fun getProductOfferingId(): Int? = getAccount()?.productOfferingId

    private fun getProductOfferingIdInStringFormat(): String? = getAccount()?.productOfferingId?.toString()

    fun getApplyNowState() = getCardDetail()?.account?.first

    fun getPaymentMethodListInStringFormat(): String? {
        val paymentMethodList = getPaymentMethodList()
        return Gson().toJson(paymentMethodList)
    }

    fun getAccount() = getCardDetail()?.account?.second

    fun getOverdueAmount(): String? {
        return getAccount()?.amountOverdue?.let { formatAndRemoveNegativeSymbol(it) }
    }

    fun getTotalAmountDue(): String? {
        return getAccount()?.totalAmountDue?.let { formatAndRemoveNegativeSymbol(it) }
    }

    fun getApplyNowAccountInStringFormat(): String? = Gson().toJson(getCardDetail()?.account)

    private fun formatAndRemoveNegativeSymbol(amount: Int): String? {
        return Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(amount), 1))
    }

    //Disable change button when amount is R0.00
    fun isChangeIconEnabled(amountEntered: String?): Boolean {
        return amountEntered != DEFAULT_RAND_CURRENCY
    }

    fun isConfirmPaymentButtonEnabled(cvvLength: Int, amountEntered: String?): Boolean {
        return cvvLength > 2 && isChangeIconEnabled(amountEntered)
    }

    fun isMaxCVVLength(size: Int): Boolean {
        return size == 3
    }

    fun isSelectedCardExpired(): Boolean {
        return getSelectedPaymentMethodCard()?.cardExpired == true
    }

    fun getVendorCardDrawableId(vendor: String?): Int {
        return when (vendor?.toLowerCase(Locale.getDefault())) {
            "visa" -> R.drawable.card_visa
            "mastercard" -> R.drawable.card_mastercard
            else -> R.drawable.card_visa_grey
        }
    }

    fun updateAmountEntered(amountEntered: String?): String {
        return if (amountEntered.isNullOrEmpty() || amountEntered == DEFAULT_RAND_CURRENCY) getOverdueAmount()
                ?: "" else amountEntered
    }

    fun isPaymentListEmpty(paymentMethodList: MutableList<GetPaymentMethod>?): Boolean {
        return paymentMethodList?.isEmpty() == true
    }

    fun triggerFirebaseEventForEditAmount() {
        getAccount()?.productGroupCode?.toLowerCase(Locale.getDefault())?.let { productGroupCode -> pmaFirebaseEvent.sendFirebaseEventForAmountEdit(productGroupCode) }
    }

    fun triggerFirebaseEventForPaymentComplete() {
        getAccount()?.productGroupCode?.toLowerCase(Locale.getDefault())?.let { productGroupCode -> pmaFirebaseEvent.sendFirebaseEventForPaymentComplete(productGroupCode) }
    }

    fun getAddNewCardUrl(): String? {
        return WoolworthsApplication.getPayMyAccountOption()?.addCardUrl(getProductGroupCode())
    }

    fun setAddCardResponse(addCardResponse: AddCardResponse) {
        this.addCardResponse.value = addCardResponse
    }

    fun getAddCardResponse() = addCardResponse.value

    fun queryServiceDeletePaymentMethod(card: GetPaymentMethod?, position: Int, result: () -> Unit, failure: () -> Unit) {
        deleteCardList?.add(Pair(card, position))
        mQueryServiceDeletePaymentMethod = request(OneAppService.queryServicePayURemovePaymentMethod(card?.token ?: ""), object : IGenericAPILoaderView<Any> {
            override fun onSuccess(response: Any?) {
                (response as? DeleteResponse)?.apply {
                    when (httpCode) {
                        200 -> showResultOnEmptyList(result)
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
        return if (cardInfo?.paymentMethodList?.isEmpty() == true) account?.amountOverdue?.toString()
                ?: "" else cardInfo?.amountEntered ?: ""
    }

    fun convertRandFormatToDouble(item: String?): Double {
        val number: String? = item?.replace("[R ]".toRegex(), "")
        return if (number.isNullOrEmpty()) 0.0 else number.toDouble()
    }

    private fun convertRandFormatToInt(item: String?): Int {
        val number: String? = item?.replace("[,.R$ ]".toRegex(), "")
        return if (number.isNullOrEmpty()) 0 else number.toInt()
    }

    fun getAmountEnteredAfterTextChanged(item: String?): String? {
        val account = getAccount()
        val inputAmount = convertRandFormatToInt(item)
        val enteredAmount = account?.amountOverdue?.minus(inputAmount) ?: 0
        return Utils.removeNegativeSymbol(WFormatter.newAmountFormat(if (enteredAmount < 0) 0 else enteredAmount))
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
        card?.amountEntered = getOverdueAmount()
        setPMACardInfo(card)
    }


    private fun payURequestBody(cardDetailArgs: AddCardResponse?): PayUPay {

        val cardInfo = getCardDetail()
        val amountEntered = cardInfo?.amountEnteredInInt() ?: 0

        val creditCardCVV = cardDetailArgs?.card?.cvv ?: ""
        val token = cardDetailArgs?.token ?: "0"
        val type = cardDetailArgs?.card?.type ?: ""
        val isSaveCardChecked = cardDetailArgs?.saveChecked ?: false
        val currency = "ZAR"

        val account = cardInfo?.account?.second
        val accountNumber = account?.accountNumber ?: "0"
        val productOfferingId = account?.productOfferingId ?: 0
        val paymentMethod = PayUPaymentMethod(token, creditCardCVV, type)

        return PayUPay(amountEntered, currency, productOfferingId, isSaveCardChecked, paymentMethod, accountNumber)
    }

    // Retrieve 3d secure merchant url
    fun queryServicePostPayU(cardDetailArgs: AddCardResponse?, result: (PayUResponse?) -> Unit, stsParams: (String?) -> Unit, generalHttpCodeFailure: (String?) -> Unit, failure: (Throwable?) -> Unit) {
        val payURequestBody = payURequestBody(cardDetailArgs)
        mQueryServicePostPayU = request(OneAppService.queryServicePostPayU(payURequestBody), object : IGenericAPILoaderView<Any> {

            override fun onSuccess(response: Any?) {
                (response as? PayUResponse)?.apply {
                    isQueryServiceGetRedirectionCompleted = true
                    when (httpCode) {
                        200 -> {
                            pma3dSecureRedirection = this.redirection
                            result(this)
                        }
                        440 -> this.response.stsParams?.let { params -> stsParams(params) }
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

    fun constructPayUPayResultCallback(url: String?, stopLoading: () -> Unit, result: (PayUPayResultRequest) -> Unit) {
        val merchantSiteUrl = getMerchantSiteAndMerchantUrl().first

        if (merchantSiteUrl?.let { url?.contains(it) } == true) {
            stopLoading()
            val siteUrl = url?.substring(url.indexOf("?"), url.length)

            val splitSiteUrlList = siteUrl?.split("&")
            val customer = splitSiteUrlList?.get(0)
            val paymentId = splitSiteUrlList?.get(1)
            val chargeId = splitSiteUrlList?.get(2)
            val status = splitSiteUrlList?.get(3)

            result(PayUPayResultRequest(
                    customer?.substring(customer.indexOf("=").plus(1), customer.length) ?: "",
                    paymentId?.substring(paymentId.indexOf("=").plus(1), paymentId.length) ?: "",
                    chargeId?.substring(chargeId.indexOf("=").plus(1), chargeId.length) ?: "",
                    status?.substring(status.indexOf("=").plus(1), status.length) ?: "",
                    getProductOfferingIdInStringFormat() ?: ""))
        }
    }
}