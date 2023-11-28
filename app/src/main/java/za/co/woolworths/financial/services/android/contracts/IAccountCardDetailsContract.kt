package za.co.woolworths.financial.services.android.contracts

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplication
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardActivationState
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.DeliveryStatus
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.CreditLimitIncreaseStatus
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent

interface IAccountCardDetailsContract {

    interface AccountCardDetailView {
        fun handleUnknownHttpCode(description: String?)
        fun handleSessionTimeOut(stsParams: String?)
        fun showStoreCardProgress()
        fun hideStoreCardProgress()
        fun navigateToGetTemporaryStoreCardPopupActivity(storeCardResponse: StoreCardsResponse)
        fun navigateToDebitOrderActivity(debitOrder: DebitOrder)
        fun showBalanceProtectionInsuranceLead(bpiInsuranceApplication: BpiInsuranceApplication?){}
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
        fun navigateToPayMyAccountActivity()
        fun onGetCreditCArdTokenSuccess(creditCardTokenResponse: CreditCardTokenResponse) {}
        fun onGetCreditCardTokenFailure()
        fun showGetCreditCardActivationStatus(status: CreditCardActivationState)
        fun executeCreditCardTokenService()
        fun stopCardActivationShimmer()
        fun executeCreditCardDeliveryStatusService()
        fun onGetCreditCardDeliveryStatusSuccess(creditCardDeliveryStatusResponse: CreditCardDeliveryStatusResponse)
        fun onGetCreditCardDeliveryStatusFailure()
        fun showGetCreditCardDeliveryStatus(deliveryStatus: DeliveryStatus)
        fun showOnStoreCardFailure(error: Throwable?) {}
        fun handleStoreCardCardsSuccess(storeCardResponse: StoreCardsResponse) {}
        fun showUnBlockStoreCardCardDialog() {}
        fun navigateToMyCardDetailActivity(storeCardResponse: StoreCardsResponse, requestUnblockStoreCardCall: Boolean = false)
        fun hideBalanceProtectionInsurance()
        fun navigateToBalanceProtectionInsuranceApplication(accountInfo: String?, bpiInsuranceStatus: BpiInsuranceApplicationStatusType?)
    }

    interface AccountCardDetailPresenter {
        fun createCardHolderName(): String?
        fun displayCardHolderName()
        fun balanceProtectionInsuranceIsCovered(account: Account?): Boolean
        fun getBpiInsuranceApplication(): BpiInsuranceApplication?
        fun showBalanceProtectionInsuranceLead()
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
        fun navigateToMyCardDetailActivity(shouldStartWithUnblockStoreCardCall: Boolean = false)
        fun getOfferActive(): OfferActive?
        fun getProductOfferingId(): Int?
        fun onDestroy()
        fun navigateToTemporaryStoreCard()
        fun navigateToDebitOrderActivity()
        fun navigateToBalanceProtectionInsurance()
        fun cliProductOfferingGoodStanding(): Boolean
        fun creditLimitIncrease(): CreditLimitIncreaseStatus?
        fun navigateToPaymentOptionActivity()
        fun getCreditCardToken()
        fun getCardWithPLCState(cards: ArrayList<Card>?): Card?
        fun getCreditCardDeliveryStatus(envelopeNumber: String?)
        fun isCreditCardSection(): Boolean
        fun navigateToPayMyAccountActivity()
        fun getStoreCardBlockType(): Boolean
        fun isProductCodeStoreCard(): Boolean
        fun onStartCreditLimitIncreaseFirebaseEvent(activity: Activity)
        fun isVirtualCardEnabled(): Boolean
        fun isVirtualCardObjectNotNull(): Boolean
        fun isTemporaryCardEnabled(): Boolean
        fun isInstantCardReplacementEnabled(): Boolean
        fun isVirtualCardObjectBlockTypeNull(): Boolean
        fun isGeneterateVTC(): Boolean
        fun getPrimaryStoreCardBlockType(): String
        fun isReplacementCardAndVirtualCardViewEnabled(): Boolean
        fun isActivateVirtualTempCard(): Boolean
    }

    interface AccountCardDetailModel {
        fun queryServiceGetAccountStoreCardCards(storeCardsRequestBody: StoreCardsRequestBody?, requestListener: IGenericAPILoaderView<Any>): Call<StoreCardsResponse>?
        fun queryServiceGetUserCLIOfferActive(productOfferingId: String, requestListener: IGenericAPILoaderView<Any>): Call<OfferActive>?
        fun queryServiceGetCreditCartToken(requestListener: IGenericAPILoaderView<Any>): Call<CreditCardTokenResponse>?
        fun queryServiceGetCreditCardDeliveryStatus(productOfferingId: String, envelopeReference: String, requestListener: IGenericAPILoaderView<Any>): Call<CreditCardDeliveryStatusResponse>?
    }
}