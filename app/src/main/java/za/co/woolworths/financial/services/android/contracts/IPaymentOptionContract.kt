package za.co.woolworths.financial.services.android.contracts

import android.content.Intent
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PaymentMethod
import za.co.woolworths.financial.services.android.models.dto.account.HeaderDrawable
import java.util.HashMap

interface IPaymentOptionContract {

    interface PaymentOptionView {
        fun showPaymentDetail(paymentDetail: Map<String, String>?)
        fun setHowToPayLogo(headerDrawable: HeaderDrawable?)
        fun showABSAInfo()
        fun hideABSAInfo()
        fun setPaymentOption(paymentMethods: MutableList<PaymentMethod>?)
    }

    interface PaymentOptionPresenter {
        fun retrieveAccountBundle(intent: Intent?)
        fun getAccount(): Account?
        fun getPaymentDetail(): Map<String, String>
        fun displayPaymentDetail()
        fun setHowToPayLogo()
        fun loadABSACreditCardInfoIfNeeded()
        fun getPaymentMethod(): MutableList<PaymentMethod>?
        fun displayPaymentMethod()
        fun initView()
    }

    interface PaymentOptionModel {
        fun getAccountDetailValues(): HashMap<String, String?>
        fun getDrawableHeader(): List<HeaderDrawable>
    }
}