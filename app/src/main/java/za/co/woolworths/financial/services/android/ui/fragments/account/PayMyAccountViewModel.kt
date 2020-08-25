package za.co.woolworths.financial.services.android.ui.fragments.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.util.FontHyperTextParser
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

class PayMyAccountViewModel : ViewModel() {

    private var accountSection: MutableLiveData<Pair<ApplyNowState, Account>> = MutableLiveData()
    var amountEntered: MutableLiveData<String> = MutableLiveData()
    private var cvvNumber: MutableLiveData<String> = MutableLiveData()

    var paymentMethodList: MutableLiveData<MutableList<GetPaymentMethod>>? = MutableLiveData()

    private var accountAndCardItem: MutableLiveData<Pair<Pair<ApplyNowState, Account>, AddCardResponse>> = MutableLiveData()

    fun createCard(): Pair<Pair<ApplyNowState, Account>?, AddCardResponse> {
        val paymentMethod = getSelectedPaymentMethodCard()
        val account = getAccountProduct()
        val cvvNumber = getCVVNumber()
        val expiryDate = paymentMethod?.expirationDate?.split("/")
        val exp_date = expiryDate?.get(0) ?: ""
        val exp_year = expiryDate?.get(1) ?: ""

        val pmaCard = PMACard(
                paymentMethod?.cardNumber ?: "",
                "",
                exp_date,
                exp_year,
                cvvNumber,
                1,
                paymentMethod?.vendor
                        ?: "",
                paymentMethod?.type ?: "")
        val cardResponse = AddCardResponse(paymentMethod?.token ?: "", pmaCard, true)
        account?.let { setPaymentAccountDetail(Pair(it, cardResponse))}
        return Pair(account, cardResponse)
    }

    fun setPaymentMethod(list: MutableList<GetPaymentMethod>?) {
        paymentMethodList?.value = list
    }

    fun getPaymentMethod() = paymentMethodList?.value

    fun setAmountEntered(amount: String?) {
        amountEntered.value = amount
    }

    fun getAmountEntered() = if (amountEntered.value?.isNotEmpty() == true) {
        amountEntered.value
    } else {
        val amountDue = getAccountProduct()?.second?.amountOverdue ?: 0
        val amountDueOutput = Utils.removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.newAmountFormat(amountDue), 1, WoolworthsApplication.getAppContext()))
        amountDueOutput
    }

    fun setCVVNumber(number: String) {
        cvvNumber.value = number
    }

    private fun setPaymentAccountDetail(item: Pair<Pair<ApplyNowState, Account>, AddCardResponse>) {
        accountAndCardItem.value = item
    }

    fun getPaymentAccountDetail() = accountAndCardItem.value

    private fun getCVVNumber() = cvvNumber.value ?: ""

    fun getSelectedPaymentMethodCard(): GetPaymentMethod? {
        val paymentMethod: MutableList<GetPaymentMethod>? = getPaymentMethod()
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
}