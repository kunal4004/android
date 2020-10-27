package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account

import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IPaymentOptionContract
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PaymentMethod
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.PayMyCardHeaderItem
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter
import java.lang.RuntimeException

class PayMyAccountPresenterImpl(private var mainView: IPaymentOptionContract.PayMyAccountView?, private var model: IPaymentOptionContract.PayMyAccountModel) : IPaymentOptionContract.PayMyAccountPresenter, IPaymentOptionContract.PayMyAccountModel {

    companion object {
        const val GET_PAYMENT_METHOD: String = "PAYMENT_METHOD"
        const val GET_CARD_RESPONSE = "ADD_CARD_RESPONSE"
        const val SCREEN_TYPE: String = "SCREEN_TYPE"
        const val IS_DONE_BUTTON_ENABLED: String = "IS_DONE_BUTTON_ENABLED"
    }

    var mAccountDetails: Pair<ApplyNowState, Account>? = null

    override fun retrieveAccountBundle(item: Pair<ApplyNowState, Account>?) {
        mAccountDetails = item
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

    override fun getHeaderItem(): List<PayMyCardHeaderItem> {
        return model.getHeaderItem()
    }

    override fun getATMPaymentInfo(): MutableList<Int> {
        return model.getATMPaymentInfo()
    }

    @Throws(RuntimeException::class)
    override fun getPayMyAccountCardDrawable() {
        val drawableHeader = getHeaderItem()
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
                else -> return
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

    @Throws(RuntimeException::class)
    override fun getPayMyCardCardItem(): PayMyCardHeaderItem {
        val headerItem = getHeaderItem()
        return when (mAccountDetails?.first) {
            ApplyNowState.STORE_CARD -> headerItem[0]
            ApplyNowState.SILVER_CREDIT_CARD -> headerItem[3]
            ApplyNowState.GOLD_CREDIT_CARD -> headerItem[2]
            ApplyNowState.BLACK_CREDIT_CARD -> headerItem[1]
            ApplyNowState.PERSONAL_LOAN -> headerItem[4]
            else -> throw RuntimeException("Invalid ApplyNowState ${mAccountDetails?.first}")
        }
    }

    override fun getTotalAmountDue(totalAmountDue: Int): String {
        return Utils.removeNegativeSymbol(WFormatter.newAmountFormat(totalAmountDue))
    }

    override fun getAmountOutstanding(amountOutstanding: Int): String {
        return Utils.removeNegativeSymbol(WFormatter.newAmountFormat(amountOutstanding))
    }

    override fun getPayMyAccountSection(): ApplyNowState {
        return mAccountDetails?.first ?: ApplyNowState.STORE_CARD
    }

    @Throws(RuntimeException::class)
    override fun setFirebaseEventForPayByCardNow() {
        return when (mAccountDetails?.first) {
            ApplyNowState.STORE_CARD -> Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.PMA_SC_PAY)
            ApplyNowState.SILVER_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD -> Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.PMA_CC_PAY)
            ApplyNowState.PERSONAL_LOAN -> Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.PMA_PL_PAY)
            else -> throw RuntimeException("Invalid ApplyNowState ${mAccountDetails?.first}")
        }
    }

    fun getWhatsAppVisibility(): Boolean {
        with(WhatsAppChatToUs()) {
            return when (mAccountDetails?.first) {
                ApplyNowState.SILVER_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD -> isCCPaymentOptionsEnabled
                ApplyNowState.STORE_CARD -> isSCPaymentOptionsEnabled
                ApplyNowState.PERSONAL_LOAN -> isPLPaymentOptionsEnabled
                else -> false
            }
        }
    }
}