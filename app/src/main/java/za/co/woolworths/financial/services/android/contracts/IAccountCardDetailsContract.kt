package za.co.woolworths.financial.services.android.contracts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardActivationState
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.DeliveryStatus
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.CreditLimitIncreaseStatus

interface IAccountCardDetailsContract {

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
        fun navigateToPaymentOptionActivity()
        fun onGetCreditCArdTokenSuccess(creditCardTokenResponse: CreditCardTokenResponse)
        fun onGetCreditCardTokenFailure()
        fun showGetCreditCardActivationStatus(status: CreditCardActivationState)
        fun executeCreditCardTokenService()
        fun stopCardActivationShimmer()
        fun executeCreditCardDeliveryStatusService()
        fun onGetCreditCardDeliveryStatusSuccess(creditCardDeliveryStatusResponse: CreditCardDeliveryStatusResponse)
        fun onGetCreditCardDeliveryStatusFailure()
        fun showGetCreditCardDeliveryStatus(deliveryStatus: DeliveryStatus)
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
        fun navigateToPaymentOptionActivity()
        fun getCreditCardToken()
        fun getCardWithPLCState(cards: ArrayList<Card>?): Card?
        fun getCreditCardDeliveryStatus(envelopeNumber: String?)
    }

    interface AccountCardDetailModel {
        fun queryServiceGetAccountStoreCardCards(storeCardsRequestBody: StoreCardsRequestBody?, requestListener: IGenericAPILoaderView<Any>): Call<StoreCardsResponse>?
        fun queryServiceGetUserCLIOfferActive(productOfferingId: String, requestListener: IGenericAPILoaderView<Any>): Call<OfferActive>?
        fun queryServiceGetCreditCartToken(requestListener: IGenericAPILoaderView<Any>):Call<CreditCardTokenResponse>?
        fun queryServiceGetCreditCardDeliveryStatus(productOfferingId: String, envelopeReference: String, requestListener: IGenericAPILoaderView<Any>): Call<CreditCardDeliveryStatusResponse>?
    }
}