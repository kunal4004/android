package za.co.woolworths.financial.services.android.ui.fragments.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.util.SessionUtilities

class PayMyAccountViewModel : ViewModel() {

    enum class PAYUMethodType { CREATE_USER, CARD_UPDATE, ERROR }

    private var paymentMethodsResponse: MutableLiveData<PaymentMethodsResponse?> = MutableLiveData()
    private var accountWithApplyNowState: MutableLiveData<Pair<ApplyNowState, Account>> = MutableLiveData()
    private var cvvNumber: MutableLiveData<String> = MutableLiveData()
    private var accountAndCardItem: MutableLiveData<Pair<Pair<ApplyNowState, Account>, AddCardResponse>> = MutableLiveData()
    private var payUMethodType: MutableLiveData<PAYUMethodType?> = MutableLiveData()

    var paymentAmountCard: MutableLiveData<PaymentAmountCard?> = MutableLiveData()
    var queryPaymentMethod: MutableLiveData<Boolean> = MutableLiveData()

    fun createCard(): Pair<Pair<ApplyNowState, Account>?, AddCardResponse> {
        val paymentMethod = getSelectedPaymentMethodCard()
        val account = getAccountProduct() ?: getCardDetail()?.account
        val cvvNumber = getCVVNumber()
        val expiryDate = paymentMethod?.expirationDate?.split("/")
        val expDate = expiryDate?.get(0) ?: ""
        val expYear = expiryDate?.get(1) ?: ""

        val pmaCard = PMACard(paymentMethod?.cardNumber
                ?: "", "", expDate, expYear, cvvNumber, 1, paymentMethod?.vendor
                ?: "", paymentMethod?.type ?: "")
        val cardResponse = AddCardResponse(paymentMethod?.token ?: "", pmaCard, false)
        account?.let { setPaymentAccountDetail(Pair(it, cardResponse)) }
        return Pair(account, cardResponse)
    }

    fun getPaymentMethodList(): MutableList<GetPaymentMethod>? {
        val cardDetail = getCardDetail()
        val list = cardDetail?.paymentMethodList
        if (list != null && !list.isNullOrEmpty()) {
            setPaymentMethodType(PAYUMethodType.CARD_UPDATE)
            val checkedList = list.filter { it.isCardChecked }
            if (checkedList.isNullOrEmpty()) {
                list[0].isCardChecked = true
            }
        } else {
            setPaymentMethodType(PAYUMethodType.CREATE_USER)
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

    private fun setPaymentAccountDetail(item: Pair<Pair<ApplyNowState, Account>, AddCardResponse>) {
        accountAndCardItem.value = item
    }

    fun getSelectedPaymentMethodCard(): GetPaymentMethod? {
        val paymentMethod: MutableList<GetPaymentMethod>? = getPaymentMethodList()
        paymentMethod?.forEach { item ->
            if (item.isCardChecked) {
                return item
            }
        }
        paymentMethod?.get(0)?.isCardChecked = true
        return paymentMethod?.get(0)
    }

    fun setAccountProduct(accounts: Pair<ApplyNowState, Account>) {
        accountWithApplyNowState.value = accounts
    }

    fun getAccountProduct() = accountWithApplyNowState.value

    fun queryServiceGetPaymentMethod(onPaymentMethodSuccess: (PaymentMethodsResponse) -> Unit, onPaymentMethodFailure: (Throwable?) -> Unit) {
        request(OneAppService.queryServicePayUMethod(), object : IGenericAPILoaderView<Any> {
            override fun onSuccess(response: Any?) {
                setPaymentMethodType(PAYUMethodType.CREATE_USER)
                (response as? PaymentMethodsResponse)?.apply {
                    when (httpCode) {
                        200 -> setPaymentMethodType(when (this.paymentMethods.size > 0) {
                            true -> PAYUMethodType.CARD_UPDATE
                            else -> PAYUMethodType.CREATE_USER
                        })
                        400 -> setPaymentMethodType(PAYUMethodType.CREATE_USER)
                        440 -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response.stsParams)
                        else -> setPaymentMethodType(PAYUMethodType.ERROR)
                    }
                    setPaymentMethodsResponse(this)
                    onPaymentMethodSuccess(this)
                }
            }

            override fun onFailure(error: Throwable?) {
                super.onFailure(error)
                onPaymentMethodFailure(error)
            }
        })
    }

    fun setPaymentMethodsResponse(paymentMethodResponse: PaymentMethodsResponse?) {
        val cardDetail = getCardDetail()
        val cardLists = paymentMethodResponse?.paymentMethods
        cardDetail?.paymentMethodList = cardLists
        setPMAVendorCard(cardDetail)

        setPaymentMethodType(if (cardLists?.isNullOrEmpty() == true) PAYUMethodType.CREATE_USER else PAYUMethodType.CARD_UPDATE)

        paymentMethodsResponse.value = paymentMethodResponse
    }

    fun setPaymentMethodType(type: PAYUMethodType) {
        payUMethodType.value = type
    }

    fun getPaymentMethodType(): PAYUMethodType? = payUMethodType.value

    fun setPMAVendorCard(card: PaymentAmountCard?) {
        paymentAmountCard.value = card
    }

    fun setPMAVendorCard(card: String) {
        val cardInfo = Gson().fromJson(card, PaymentAmountCard::class.java)
        paymentAmountCard.value = cardInfo
    }

    fun getCardDetail(): PaymentAmountCard? = paymentAmountCard.value

}