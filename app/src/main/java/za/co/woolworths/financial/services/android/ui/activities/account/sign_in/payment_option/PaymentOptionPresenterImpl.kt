package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.payment_option

import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.contracts.PaymentOptionContract
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PaymentMethod
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.HeaderDrawable
import java.lang.RuntimeException

class PaymentOptionPresenterImpl(private var mainView: PaymentOptionContract.PaymentOptionView?, private var model: PaymentOptionContract.PaymentOptionModel) : PaymentOptionContract.PaymentOptionPresenter, PaymentOptionContract.PaymentOptionModel {

    companion object {
        const val ACCOUNT_INFO = "ACCOUNT_INFO"
    }

    private var mAccountDetails: Pair<ApplyNowState, Account>? = null

    override fun retrieveAccountBundle(intent: Intent?) {
        mAccountDetails =
                Gson().fromJson(intent?.getStringExtra(ACCOUNT_INFO), object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)
    }

    override fun getAccount(): Account? {
        return mAccountDetails?.second
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

    override fun getDrawableHeader(): List<HeaderDrawable> {
        return model.getDrawableHeader()
    }

    override fun setHowToPayLogo() {
        val drawableHeader = getDrawableHeader()
        mainView?.setHowToPayLogo(when (mAccountDetails?.first) {
            ApplyNowState.STORE_CARD -> drawableHeader[0]
            ApplyNowState.SILVER_CREDIT_CARD -> drawableHeader[3]
            ApplyNowState.GOLD_CREDIT_CARD -> drawableHeader[2]
            ApplyNowState.BLACK_CREDIT_CARD -> drawableHeader[1]
            ApplyNowState.PERSONAL_LOAN -> drawableHeader[4]
            else -> throw RuntimeException("Invalid ApplyNowState ${mAccountDetails?.first}")
        })
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

    override fun initView() {
        setHowToPayLogo()
        displayPaymentDetail()
        displayPaymentMethod()
        loadABSACreditCardInfoIfNeeded()
    }
}