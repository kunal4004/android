package za.co.woolworths.financial.services.android.contracts

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.CreditLimitIncreaseStatus

interface AvailableFundsContract {

    interface AvailableFundsView {
        fun navigateToOnlineBankingActivity(creditCardNumber: String, isRegistered: Boolean)
        fun displayCardNumberNotFound()
        fun handleUnknownHttpResponse(desc: String?)
        fun handleSessionTimeOut(stsParams: String)
        fun showABSAServiceGetUserCreditCardTokenProgressBar()
        fun hideABSAServiceGetUserCreditCardTokenProgressBar()
        fun navigateToRecentTransactionActivity(cardType: String)
        fun navigateToLoanWithdrawalActivity()
        fun navigateToPaymentOptionActivity()
        fun navigateToStatementActivity()
        fun setPushViewDownAnimation(view: View)
        fun onABSACreditCardFailureHandler(error: Throwable?)
        fun navigateToABSAStatementActivity()
    }

    interface AvailableFundsPresenter {
        fun setBundle(bundle: Bundle?)
        fun getBundle(): Pair<ApplyNowState, Account>?
        fun queryABSAServiceGetUserCreditCardToken()
        fun handleUserCreditCardToken(creditCardTokenResponse: CreditCardTokenResponse)
        fun getCreditCardNumber(cards: ArrayList<Card>?): String
        fun getAccount(): Account?
        fun onDestroy()
    }

    interface AvailableFundsModel {
        fun queryABSAServiceGetUserCreditCardToken(requestListener: IGenericAPILoaderView<Any>): Call<CreditCardTokenResponse>?
    }
}

interface AccountCardDetailsContract {

    interface AccountCardDetailView {
        fun handleUnknownHttpCode(description: String?)
        fun handleSessionTimeOut(stsParams: String?)
        fun showStoreCardProgress()
        fun hideAccountStoreCardProgress()
        fun navigateToGetTemporaryStoreCardPopupActivity(storeCardResponse: StoreCardsResponse)
        fun navigateToMyCardDetailActivity(storeCardResponse: StoreCardsResponse)
        fun navigateToDebitOrderActivity(debitOrder: DebitOrder)
        fun navigateToBalanceProtectionInsurance(accountInfo: String?)
        fun setBalanceProtectionInsuranceState(coveredText: Boolean)
        fun displayCardHolderName(name: String?)
        fun hideUserOfferActiveProgress()
        fun showUserOfferActiveProgress()
        fun disableContentStatusUI()
        fun enableContentStatusUI()
        fun handleCreditLimitIncreaseTagStatus(offerActive: OfferActive)
        fun hideProductNotInGoodStanding()
        fun onOfferActiveSuccessResult()
        fun navigateToLoanWithdrawalActivity()
    }

    interface AccountCardDetailPresenter {
        fun createCardHolderName(): String?
        fun displayCardHolderName()
        fun balanceProtectionInsuranceIsCovered(account: Account?): Boolean
        fun setBalanceProtectionInsuranceState()
        fun getAppCompatActivity(): AppCompatActivity?
        fun setAccountDetailBundle(arguments: Bundle?)
        fun getAccount(): Account?
        fun getDebitOrder(): DebitOrder?
        fun isDebitOrderActive(): Int?
        fun convertAccountObjectToJsonString(): String?
        fun handleUnknownHttpResponse(description: String?)
        fun getAccountStoreCardCards()
        fun getUserCLIOfferActive()
        fun getStoreCardResponse(): StoreCardsResponse?
        fun handleStoreCardSuccessResponse(storeCardResponse: StoreCardsResponse)
        fun navigateToGetTemporaryStoreCardPopupActivity()
        fun navigateToMyCardDetailActivity()
        fun getOfferActive(): OfferActive?
        fun getProductOfferingId(): Int?
        fun onDestroy()
        fun navigateToTemporaryStoreCardOnButtonTapped()
        fun navigateToDebitOrderActivityOnButtonTapped()
        fun navigateToBalanceProtectionInsuranceOnButtonTapped()
        fun cliProductOfferingGoodStanding(): Boolean
        fun creditLimitIncrease(): CreditLimitIncreaseStatus?
    }

    interface AccountCardDetailModel {
        fun queryServiceGetAccountStoreCardCards(storeCardsRequestBody: StoreCardsRequestBody?, requestListener: IGenericAPILoaderView<Any>): Call<StoreCardsResponse>?
        fun queryServiceGetUserCLIOfferActive(productOfferingId: String, requestListener: IGenericAPILoaderView<Any>): Call<OfferActive>?
    }
}