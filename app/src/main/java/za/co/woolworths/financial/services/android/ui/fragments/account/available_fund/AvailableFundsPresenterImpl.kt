package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund

import android.os.Bundle
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.absa.openbankingapi.woolworths.integration.AbsaSecureCredentials
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IAvailableFundsContract
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.Card
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.helper.FirebaseEventDetailManager

class AvailableFundsPresenterImpl(private var mainView: IAvailableFundsContract.AvailableFundsView?, private var model: IAvailableFundsContract.AvailableFundsModel?) : IAvailableFundsContract.AvailableFundsPresenter, IGenericAPILoaderView<Any> {

    private var mAccountPair: Pair<ApplyNowState, Account>? = null
    private var mAccount: Account? = null

    @Throws(RuntimeException::class)
    override fun setBundle(bundle: Bundle?) {
        val account = bundle?.getString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE)
        mAccountPair = Gson().fromJson(account, object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)
        mAccount = mAccountPair?.second
    }

    override fun getBundle(): Pair<ApplyNowState, Account>? = mAccountPair

    override fun queryABSAServiceGetUserCreditCardToken() {
        mainView?.showABSAServiceGetUserCreditCardTokenProgressBar()
        model?.queryABSAServiceGetUserCreditCardToken(this)
    }

    @Throws(RuntimeException::class)
    override fun onSuccess(response: Any?) {
        with(response) {
            when (this) {
                is CreditCardTokenResponse -> {
                    when (httpCode) {
                        200 -> handleUserCreditCardToken(this)
                        440 -> {
                            this.response?.stsParams?.let { stsParams -> mainView?.handleSessionTimeOut(stsParams) } }
                        else -> mainView?.handleUnknownHttpResponse(this.response?.desc)
                    }
                    mainView?.hideABSAServiceGetUserCreditCardTokenProgressBar()
                }
                else -> throw RuntimeException("onSuccess:: unknown response $response")
            }
        }
    }

    override fun onFailure(error: Throwable?) {
        mainView?.onABSACreditCardFailureHandler(error)
        mainView?.hideABSAServiceGetUserCreditCardTokenProgressBar()
    }

    override fun handleUserCreditCardToken(creditCardTokenResponse: CreditCardTokenResponse) {
        val cards = creditCardTokenResponse.cards
        when (cards.isNullOrEmpty()) {
            true -> mainView?.displayCardNumberNotFound()
            false -> {
                val creditCardNumber: String? = getCreditCardNumber(cards)
                when (creditCardNumber.isNullOrEmpty()) {
                    true -> mainView?.displayCardNumberNotFound()
                    false -> {
                        val absaSecureCredentials = AbsaSecureCredentials()
                        val aliasID = absaSecureCredentials.aliasId
                        val deviceID = absaSecureCredentials.deviceId
                        mainView?.navigateToOnlineBankingActivity(creditCardNumber, !(TextUtils.isEmpty(aliasID) || TextUtils.isEmpty(deviceID)))
                    }
                }
            }
        }
    }

    override fun getCreditCardNumber(cards: ArrayList<Card>?): String? {
        return cards?.filter { card -> card.cardStatus?.trim { it <= ' ' } == "AAA" }
                ?.takeIf { it.isNotEmpty() }
                ?.let { it[0].absaCardToken }
    }

    override fun getAccount(): Account? = mAccount
    override fun getApplyNowState(): ApplyNowState? = mAccountPair?.first


    override fun productHasAmountOverdue(): Boolean = getAccount()?.amountOverdue ?: 0 > 0

    override fun onDestroy() {
        mainView = null
    }

    override fun isPersonalLoanAndStoreCardVisible(): Boolean? {
        return when (mAccountPair?.first) {
            ApplyNowState.PERSONAL_LOAN, ApplyNowState.STORE_CARD -> true
            else -> false
        }
    }

    override fun getAccountDetail(): Pair<ApplyNowState, Account>? {
        return mAccountPair
    }
}