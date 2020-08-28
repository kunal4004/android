package za.co.woolworths.financial.services.android.ui.fragments.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.util.FontHyperTextParser
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

class PayMyAccountViewModel : ViewModel() {

    enum class PAYUMethodType { CREATE_USER, CARD_UPDATE, ERROR }

    private var paymentMethodsResponse: MutableLiveData<PaymentMethodsResponse?> = MutableLiveData()
    private var accountSection: MutableLiveData<Pair<ApplyNowState, Account>> = MutableLiveData()
    private var cvvNumber: MutableLiveData<String> = MutableLiveData()
    private var accountAndCardItem: MutableLiveData<Pair<Pair<ApplyNowState, Account>, AddCardResponse>> = MutableLiveData()
    private var payUMethodType: MutableLiveData<PAYUMethodType?> = MutableLiveData()

    var queryPaymentMethod: MutableLiveData<Boolean> = MutableLiveData()
    var paymentMethodList: MutableLiveData<MutableList<GetPaymentMethod>>? = MutableLiveData()
    var amountEntered: MutableLiveData<String> = MutableLiveData()

    fun createCard(): Pair<Pair<ApplyNowState, Account>?, AddCardResponse> {
        val paymentMethod = getSelectedPaymentMethodCard()
        val account = getAccountProduct()
        val cvvNumber = getCVVNumber()
        val expiryDate = paymentMethod?.expirationDate?.split("/")
        val expDate = expiryDate?.get(0) ?: ""
        val expYear = expiryDate?.get(1) ?: ""

        val pmaCard = PMACard(
                paymentMethod?.cardNumber ?: "",
                "",
                expDate,
                expYear,
                cvvNumber,
                1,
                paymentMethod?.vendor
                        ?: "",
                paymentMethod?.type ?: "")
        val cardResponse = AddCardResponse(paymentMethod?.token ?: "", pmaCard, false)
        account?.let { setPaymentAccountDetail(Pair(it, cardResponse)) }
        return Pair(account, cardResponse)
    }

    fun setPaymentMethodList(list: MutableList<GetPaymentMethod>?) {
        paymentMethodList?.value = list
    }

    fun getPaymentMethodList(): MutableList<GetPaymentMethod>? {
        val list = paymentMethodList?.value
        val checkedList = list?.filter { it.isCardChecked }
        if (checkedList?.isNullOrEmpty() == true) {
            list[0].isCardChecked = true
        }
        return list
    }

    fun isPaymentMethodListChecked(): Boolean {
        val list = paymentMethodList?.value
        return list?.filter { it.isCardChecked }?.isNullOrEmpty() ?: false
    }

    fun setAmountEntered(amount: String?) {
        amountEntered.value = amount
    }

    fun getAmountEntered() = if (amountEntered.value?.isNotEmpty() == true) {
        amountEntered.value
    } else {
        val amountDue = getAccountProduct()?.second?.totalAmountDue ?: 0
        val amountDueOutput = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(amountDue), 1, WoolworthsApplication.getAppContext()))
        amountDueOutput
    }

    fun setCVVNumber(number: String) {
        cvvNumber.value = number
    }

    private fun getCVVNumber() = cvvNumber.value ?: ""

    private fun setPaymentAccountDetail(item: Pair<Pair<ApplyNowState, Account>, AddCardResponse>) {
        accountAndCardItem.value = item
    }

    fun getPaymentAccountDetail() = accountAndCardItem.value

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

    fun setAccount(name: String) {
        accountSection.value = Gson().fromJson(name, object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)
    }

    fun setAccountProduct(accounts: Pair<ApplyNowState, Account>) {
        accountSection.value = accounts
    }

    fun getAccountProduct() = accountSection.value

    fun queryServiceGetPaymentMethod(onPaymentMethodSuccess: (PaymentMethodsResponse) -> Unit, onPaymentMethodFailure: (Throwable?) -> Unit) {
        request(OneAppService.queryServicePayUMethod(), object : IGenericAPILoaderView<Any> {
            override fun onSuccess(response: Any?) {
                setPaymentMethodType(PAYUMethodType.CREATE_USER)
                (response as? PaymentMethodsResponse)?.apply {
                    when (httpCode) {
                        200 -> {
                            setPaymentMethodType(when (this.paymentMethods.size > 0) {
                                true -> PAYUMethodType.CARD_UPDATE
                                else -> PAYUMethodType.CREATE_USER
                            })
                        }
                        400 -> {
                            setPaymentMethodType(PAYUMethodType.CREATE_USER)
                        }
                        440 -> {
                        }
                        else -> {
                            // onPaymentMethodSuccess(onPaymentMethodFailure)
                        }
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
        paymentMethodResponse?.paymentMethods?.let { setPaymentMethodList(it) }
        paymentMethodsResponse.value = paymentMethodResponse
    }

    fun getPaymentMethodType(): PAYUMethodType? = payUMethodType.value

    fun setPaymentMethodType(type: PAYUMethodType) {
        payUMethodType.value = type
    }
}