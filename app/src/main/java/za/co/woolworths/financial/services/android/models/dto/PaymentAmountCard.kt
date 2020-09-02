package za.co.woolworths.financial.services.android.models.dto

import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState

class PaymentAmountCard(var amountEntered: String?, var paymentMethodList: MutableList<GetPaymentMethod>?, var account: Pair<ApplyNowState, Account>?) {


    fun amountEnteredInInt() = amountEntered?.replace("[,.R ]".toRegex(), "")?.toInt()

    fun selectedCard(): GetPaymentMethod? {
        paymentMethodList?.forEach { item ->
            if (item.isCardChecked) {
                return item
            }
        }
        paymentMethodList?.get(0)?.isCardChecked = true
        return paymentMethodList?.get(0)
    }

}