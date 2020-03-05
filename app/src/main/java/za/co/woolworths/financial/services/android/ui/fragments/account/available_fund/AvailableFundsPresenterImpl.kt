package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund

import android.os.Bundle
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.absa.openbankingapi.woolworths.integration.AbsaSecureCredentials
import za.co.woolworths.financial.services.android.contracts.AvailableFundsContract
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.Card
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl

class AvailableFundsPresenterImpl(private var mainView: AvailableFundsContract.AvailableFundsView?, private var model: AvailableFundsContract.AvailableFundsModel?) : AvailableFundsContract.AvailableFundsPresenter, IGenericAPILoaderView<Any> {

    private var mAccountPair: Pair<ApplyNowState, Account>? = null
    private var mAccount: Account? = null


    override fun setBundle(bundle: Bundle?) {
        val account = bundle?.getString(AccountSignedInPresenterImpl.MY_ACCOUNT_RESPONSE)
                ?: throw RuntimeException("Accounts object is null or not found")
        mAccountPair = Gson().fromJson(account, object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)
        mAccount = mAccountPair?.second
    }

    override fun getBundle(): Pair<ApplyNowState, Account>? = mAccountPair

    override fun queryABSAServiceGetUserCreditCardToken() {
        mainView?.showABSAServiceGetUserCreditCardTokenProgressBar()
        model?.queryABSAServiceGetUserCreditCardToken(this)
    }

    override fun onSuccess(apiResponse: Any?) {
        with(apiResponse) {
            when (this) {
                is CreditCardTokenResponse -> {
                    when (httpCode) {
                        200 -> handleUserCreditCardToken(this)
                        440 -> response?.stsParams?.let { stsParams -> mainView?.handleSessionTimeOut(stsParams) }
                        else -> mainView?.handleUnknownHttpResponse(response?.desc)
                    }
                    mainView?.hideABSAServiceGetUserCreditCardTokenProgressBar()
                }
                else -> throw RuntimeException("onSuccess:: unknown response $apiResponse")
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
                val creditCardNumber = getCreditCardNumber(cards)
                when (creditCardNumber.isEmpty()) {
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

    override fun getCreditCardNumber(cards: ArrayList<Card>?): String {
        return cards?.filter { card -> card.cardStatus.trim { it <= ' ' } == "AAA" }?.get(0)?.absaCardToken
                ?: ""
    }

    override fun getAccount(): Account? = mAccount

    override fun onDestroy() {
        mainView = null
    }
}