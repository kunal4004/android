package za.co.woolworths.financial.services.android.models.dto

import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import java.io.Serializable

data class PMACardPopupModel(var  amountEntered: String?= "",
                             var paymentMethodList: MutableList<GetPaymentMethod>? = mutableListOf(),
                             var account: Pair<ApplyNowState, Account>? =  Pair(ApplyNowState.STORE_CARD, Account()),
                             var payuMethodType: PayMyAccountViewModel.PAYUMethodType = PayMyAccountViewModel.PAYUMethodType.CREATE_USER,
                             var selectedCardPosition: Int = 0,
                             var cvvNumber: String?="") : Serializable {

    fun amountEnteredInInt() = amountEntered?.replace("[,.R ]".toRegex(), "")?.toInt()

}