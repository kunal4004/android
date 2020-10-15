package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.payment_option

import android.content.Intent
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.contracts.IPaymentOptionContract
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PaymentMethod
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.PaymentOptionHeaderItem
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs
import za.co.woolworths.financial.services.android.ui.extension.bindString
import java.lang.RuntimeException

class PaymentOptionPresenterImpl(private var mainView: IPaymentOptionContract.PaymentOptionView?, private var model: IPaymentOptionContract.PaymentOptionModel) : IPaymentOptionContract.PaymentOptionPresenter, IPaymentOptionContract.PaymentOptionModel {

    companion object {
        const val ACCOUNT_INFO = "ACCOUNT_INFO"
    }

    private lateinit var mAccountCardDetails: Pair<ApplyNowState, Account>

    override fun retrieveAccountBundle(intent: Intent?) {
        mAccountCardDetails = Gson().fromJson(intent?.getStringExtra(ACCOUNT_INFO), object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)
    }

    override fun getAccount(): Account {
        return mAccountCardDetails.second
    }

    override fun getPaymentDetail(): Map<String, String> {
        return Gson().fromJson(getAccount()?.bankingDetails, object : TypeToken<Map<String, String>>() {}.type)
    }

    override fun displayPaymentDetail() {
        mainView?.showPaymentDetail(getPaymentDetail())
    }

    override fun getAccountDetailValues(): HashMap<String, String?> {
        return model.getAccountDetailValues()
    }

    override fun getDrawableHeader(): List<PaymentOptionHeaderItem> {
        return model.getDrawableHeader()
    }

    @Throws(RuntimeException::class)
    override fun setHowToPayLogo() {
        val drawableHeader = getDrawableHeader()
        mainView?.setHowToPayLogo(when (mAccountCardDetails?.first) {
            ApplyNowState.STORE_CARD -> drawableHeader[0]
            ApplyNowState.SILVER_CREDIT_CARD -> drawableHeader[3]
            ApplyNowState.GOLD_CREDIT_CARD -> drawableHeader[2]
            ApplyNowState.BLACK_CREDIT_CARD -> drawableHeader[1]
            ApplyNowState.PERSONAL_LOAN -> drawableHeader[4]
            else -> throw RuntimeException("Invalid ApplyNowState ${mAccountCardDetails?.first}")
        })

        (mAccountCardDetails?.first)?.let { applyNowState -> setWhatsAppChatWithUsVisibility(applyNowState) }
    }

    override fun loadABSACreditCardInfoIfNeeded() {
        when (mAccountCardDetails.first) {
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
                else ->  ""
            }
        }
    }

    override fun getAppScreenName(): String {
        return when (mAccountCardDetails.first) {
            ApplyNowState.SILVER_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD -> bindString(R.string.credit_card_payment_option_label)
            ApplyNowState.STORE_CARD -> bindString(R.string.store_card_payment_option_label)
            ApplyNowState.PERSONAL_LOAN -> bindString(R.string.personal_loan_payment_option_label)
            else -> "whatsApp AppScreenName unknown state"
        }
    }

    override fun initView() {
        setHowToPayLogo()
        displayPaymentDetail()
        displayPaymentMethod()
        loadABSACreditCardInfoIfNeeded()
    }

    override fun chatWithCollectionAgent() {
        mainView?.chatToCollectionAgent( mAccountCardDetails.first, arrayListOf(getAccount()))
    }
}