package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PaymentMethod
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.PayMyCardHeaderItem
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import java.util.HashMap

interface IPaymentOptionContract {

    interface PayMyAccountView {
        fun showPaymentDetail(paymentDetail: Map<String, String>?){}
        fun setHowToPayLogo(payMyCardHeaderItem: PayMyCardHeaderItem?){}
        fun showABSAInfo(){}
        fun hideABSAInfo(){}
        fun setPaymentOption(paymentMethods: MutableList<PaymentMethod>?){}
        fun setWhatsAppChatWithUsVisibility(isVisible: Boolean){}
        fun getPayMyAccountPresenter(): PayMyAccountPresenterImpl?
        fun configureToolbar(title: String?){}
    }

    interface PayMyAccountPresenter {
        fun retrieveAccountBundle(intent: Pair<ApplyNowState, Account>?){}
        fun getAccount(): Account?
        fun getElectronicFundTransferBankingDetail(): Map<String, String>
        fun displayPaymentDetail()
        fun getPayMyAccountCardDrawable() {}
        fun loadABSACreditCardInfoIfNeeded(){}
        fun getPaymentMethod(): MutableList<PaymentMethod>?
        fun displayPaymentMethod(){}
        fun setWhatsAppChatWithUsVisibility(applyNowState: ApplyNowState)
        fun getAppScreenName():String
        fun initView(){}
        fun getPayMyCardCardItem(): PayMyCardHeaderItem
        fun getTotalAmountDue(totalAmountDue : Int): String
        fun getAmountOutstanding(amountOutstanding: Int): String
        fun getPayMyAccountSection(): ApplyNowState
        fun setFirebaseEventForPayByCardNow()
    }

    interface PayMyAccountModel {
        fun getAccountDetailValues(): HashMap<String, String?>
        fun getHeaderItem(): List<PayMyCardHeaderItem>
        fun getATMPaymentInfo(): MutableList<Int>
    }
}