package za.co.woolworths.financial.services.android.contracts

import android.os.Bundle
import android.view.View
import retrofit2.Call
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.util.wenum.PayMyAccountStartDestinationType

interface IAvailableFundsContract {

    interface AvailableFundsView {
        fun navigateToOnlineBankingActivity(creditCardNumber: String, isRegistered: Boolean)
        fun displayCardNumberNotFound()
        fun handleUnknownHttpResponse(desc: String?)
        fun handleSessionTimeOut(stsParams: String)
        fun showABSAServiceGetUserCreditCardTokenProgressBar()
        fun hideABSAServiceGetUserCreditCardTokenProgressBar()
        fun navigateToRecentTransactionActivity(cardType: String)
        fun navigateToLoanWithdrawalActivity()
        fun navigateToPayMyAccountActivity(payMyAccountStartDestinationType: PayMyAccountStartDestinationType)
        fun navigateToStatementActivity()
        fun navigateToPaymentOptionsActivity(payMyAccountStartDestinationType: PayMyAccountStartDestinationType)
        fun setPushViewDownAnimation(view: View)
        fun onABSACreditCardFailureHandler(error: Throwable?)
        fun navigateToABSAStatementActivity()
        fun onPayUMethodSuccess(paymentMethodsResponse: PaymentMethodsResponse?)
        fun onPayUMethodFailure(error: Throwable?)
    }

    interface AvailableFundsPresenter {
        fun setBundle(bundle: Bundle?)
        fun getBundle(): Pair<ApplyNowState, Account>?
        fun queryABSAServiceGetUserCreditCardToken()
        fun handleUserCreditCardToken(creditCardTokenResponse: CreditCardTokenResponse)
        fun getCreditCardNumber(cards: ArrayList<Card>?): String?
        fun getAccount(): Account?
        fun onDestroy()
        fun queryPayUPaymentMethod()
        fun isPersonalLoanAndStoreCardVisible(): Boolean?
        fun getAccountDetail(): Pair<ApplyNowState, Account>?
        fun productHasAmountOverdue(): Boolean

    }

    interface AvailableFundsModel {
        fun queryABSAServiceGetUserCreditCardToken(requestListener: IGenericAPILoaderView<Any>): Call<CreditCardTokenResponse>?
        fun queryPayUPaymentMethods(requestListener: IGenericAPILoaderView<Any>): Call<PaymentMethodsResponse>?
    }
}