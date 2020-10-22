package za.co.woolworths.financial.services.android.models.dto

import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import java.io.Serializable

data class PaymentAmountCard(var amountEntered: String? = "",
                        var paymentMethodList: MutableList<GetPaymentMethod>? = null,
                        var account: Pair<ApplyNowState, Account>? = null,
                        var payuMethodType: PayMyAccountViewModel.PAYUMethodType = PayMyAccountViewModel.PAYUMethodType.CREATE_USER,
                        var selectedCardPosition: Int = 0) : Serializable {

    fun amountEnteredInInt() = amountEntered?.replace("[,.R ]".toRegex(), "")?.toInt()

}