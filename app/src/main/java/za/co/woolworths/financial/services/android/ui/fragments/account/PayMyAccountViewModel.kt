package za.co.woolworths.financial.services.android.ui.fragments.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class PayMyAccountViewModel : ViewModel() {

    enum class PAYUMethodType { CREATE_USER, CARD_UPDATE, ERROR }
    enum class OnBackNavigation { RETRY, REMOVE, ADD, NONE } // TODO: Navigation graph: Communicate result from dialog to fragment destination

    private var paymentMethodsResponse: MutableLiveData<PaymentMethodsResponse?> = MutableLiveData()
    private var cvvNumber: MutableLiveData<String> = MutableLiveData()

    var paymentAmountCard: MutableLiveData<PaymentAmountCard?> = MutableLiveData()
    var queryPaymentMethod: MutableLiveData<Boolean> = MutableLiveData()
    private var onDialogDismiss: MutableLiveData<OnBackNavigation> = MutableLiveData()

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
        val list = cardDetail?.paymentMethodList
        if (list != null && !list.isNullOrEmpty()) {
//            cardDetail.payuMethodType = PAYUMethodType.CARD_UPDATE
            val checkedList = list.filter { it.isCardChecked }
            if (checkedList.isNullOrEmpty()) {
                list[0].isCardChecked = true
            }
        } else {
//            cardDetail?.payuMethodType = PAYUMethodType.CREATE_USER
        }
        return list
    }

    fun isPaymentMethodListChecked(): Boolean {
        val cardDetail = getCardDetail()
        val list = cardDetail?.paymentMethodList
        return list?.filter { it.isCardChecked }?.isNullOrEmpty() ?: false
    }

    fun setCVVNumber(number: String) {
        cvvNumber.value = number
    }

    private fun getCVVNumber() = cvvNumber.value ?: ""

    fun getSelectedPaymentMethodCard(): GetPaymentMethod? {
        val paymentMethod: MutableList<GetPaymentMethod>? = getPaymentMethodList()
        paymentMethod?.forEach { item ->
            if (item.isCardChecked) {
                return item
            }
        }
        if (paymentMethod?.size ?: 0 > 0) {
            paymentMethod?.get(0)?.isCardChecked = true
            return paymentMethod?.get(0)
        }
        return null
    }

    fun setPaymentMethodsResponse(paymentMethodResponse: PaymentMethodsResponse?) {
        val cardDetail = getCardDetail()
        val cardLists = paymentMethodResponse?.paymentMethods
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
        val cardInfo = Gson().fromJson(card, PaymentAmountCard::class.java)
        paymentAmountCard.value = cardInfo
    }

    fun getCardDetail(): PaymentAmountCard? = paymentAmountCard.value

    fun setNavigationResult(onDismiss: OnBackNavigation) {
        onDialogDismiss.value = onDismiss
        onDialogDismiss.value = OnBackNavigation.NONE
    }

    fun getNavigationResult(): MutableLiveData<OnBackNavigation> {
        return onDialogDismiss
    }

    fun queryServicePayUPaymentMethod(onSuccessResult: (MutableList<GetPaymentMethod>?) -> Unit, onSessionExpired: (String?) -> Unit, onGeneralError: (String) -> Unit, onFailureHandler: (Throwable?) -> Unit) {
        var payUMethodType: PAYUMethodType
        request(OneAppService.queryServicePayUMethod(), object : IGenericAPILoaderView<Any> {
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
                    val updatedCard = PaymentAmountCard(cardInfo?.amountEntered, paymentMethods, cardInfo?.account, payUMethodType)
                    setPMACardInfo(updatedCard)
                }
            }

            override fun onFailure(error: Throwable?) {
                super.onFailure(error)
                onFailureHandler(error)
            }
        })
    }

    fun isPaymentMethodListSizeLimitedToTenItem(): Boolean {
        return getCardDetail()?.paymentMethodList?.size ?: 0 >= 9
    }

    fun getProductGroupCode(): String {
        val account = getCardDetail()?.account?.second
        return account?.productGroupCode ?: ""
    }
}