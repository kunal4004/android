package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account

import android.content.Intent
import android.util.Log
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.contracts.IPaymentOptionContract
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PaymentMethod
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.PayMyCardHeaderItem
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs
import za.co.woolworths.financial.services.android.ui.extension.bindString
import java.lang.RuntimeException

class PayMyAccountPresenterImpl(private var mainView: IPaymentOptionContract.PayMyAccountView?, private var model: IPaymentOptionContract.PayMyAccountModel) : IPaymentOptionContract.PayMyAccountPresenter, IPaymentOptionContract.PayMyAccountModel {

    companion object {
        const val ACCOUNT_INFO = "ACCOUNT_INFO"
    }

    var mAccountDetails: Pair<ApplyNowState, Account>? = null

    override fun retrieveAccountBundle(intent: Intent?) {
        mAccountDetails = Gson().fromJson(intent?.getStringExtra(ACCOUNT_INFO), object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)
    }

    override fun getAccount(): Account? {
        return mAccountDetails?.second
    }

    override fun getElectronicFundTransferBankingDetail(): Map<String, String> {
        return Gson().fromJson(getAccount()?.bankingDetails, object : TypeToken<Map<String, String>>() {}.type)
    }

    override fun displayPaymentDetail() {
        mainView?.showPaymentDetail(getElectronicFundTransferBankingDetail())
    }

    override fun getAccountDetailValues(): HashMap<String, String?> {
        return model.getAccountDetailValues()
    }

    override fun getDrawableHeader(): List<PayMyCardHeaderItem> {
        return model.getDrawableHeader()
    }

    override fun getATMPaymentInfo(): MutableList<Int> {
        return model.getATMPaymentInfo()
    }

    override fun getPayMyAccountCardDrawable() {
        val drawableHeader = getDrawableHeader()
        mainView?.setHowToPayLogo(when (mAccountDetails?.first) {
            ApplyNowState.STORE_CARD -> drawableHeader[0]
            ApplyNowState.SILVER_CREDIT_CARD -> drawableHeader[3]
            ApplyNowState.GOLD_CREDIT_CARD -> drawableHeader[2]
            ApplyNowState.BLACK_CREDIT_CARD -> drawableHeader[1]
            ApplyNowState.PERSONAL_LOAN -> drawableHeader[4]
            else -> throw RuntimeException("Invalid ApplyNowState ${mAccountDetails?.first}")
        })

        (mAccountDetails?.first)?.let { applyNowState -> setWhatsAppChatWithUsVisibility(applyNowState) }
    }

    override fun loadABSACreditCardInfoIfNeeded() {
        when (mAccountDetails?.first) {
            ApplyNowState.SILVER_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD -> mainView?.showABSAInfo()
            else -> mainView?.hideABSAInfo()
        }
    }

    override fun getPaymentMethod(): MutableList<PaymentMethod>? {
        return getAccount()?.paymentMethods
    }

    override fun displayPaymentMethod() {
        val paymentMethod = getPaymentMethod()
        mainView?.setPaymentOption(paymentMethod)
    }

    override fun setWhatsAppChatWithUsVisibility(applyNowState: ApplyNowState) {
        with(WhatsAppChatToUs()) {
            when (applyNowState) {
                ApplyNowState.SILVER_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD -> mainView?.setWhatsAppChatWithUsVisibility(isCCPaymentOptionsEnabled)
                ApplyNowState.STORE_CARD -> mainView?.setWhatsAppChatWithUsVisibility(isSCPaymentOptionsEnabled)
                ApplyNowState.PERSONAL_LOAN -> mainView?.setWhatsAppChatWithUsVisibility(isPLPaymentOptionsEnabled)
            }
        }
    }

    override fun getAppScreenName(): String {
        return when (mAccountDetails?.first) {
            ApplyNowState.SILVER_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD -> bindString(R.string.credit_card_payment_option_label)
            ApplyNowState.STORE_CARD -> bindString(R.string.store_card_payment_option_label)
            ApplyNowState.PERSONAL_LOAN -> bindString(R.string.personal_loan_payment_option_label)
            else -> "whatsApp AppScreenName unknown state"
        }
    }

    override fun initView() {
        getPayMyAccountCardDrawable()
        displayPaymentDetail()
        displayPaymentMethod()
        loadABSACreditCardInfoIfNeeded()
    }

    override fun getPayMyCardCardItem(): PayMyCardHeaderItem {
        val drawableHeader = getDrawableHeader()
        Log.e("firstBru",mAccountDetails?.first?.name.toString())
        return when (mAccountDetails?.first) {
            ApplyNowState.STORE_CARD -> drawableHeader[0]
            ApplyNowState.SILVER_CREDIT_CARD -> drawableHeader[3]
            ApplyNowState.GOLD_CREDIT_CARD -> drawableHeader[2]
            ApplyNowState.BLACK_CREDIT_CARD -> drawableHeader[1]
            ApplyNowState.PERSONAL_LOAN -> drawableHeader[4]
            else -> throw RuntimeException("Invalid ApplyNowState ${mAccountDetails?.first}")
        }
    }
}