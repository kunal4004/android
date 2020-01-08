package za.co.woolworths.financial.services.android.contracts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.DebitOrder
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse

interface AccountPaymentOptionsContract {

    interface AccountCardDetailView {
        fun handleUnknownHttpCode(description: String?)
        fun handleSessionTimeOut(stsParams: String?)
        fun showStoreCardProgress()
        fun onStoreCardProgressCompleted()
        fun navigateToGetTemporaryStoreCardPopupActivity(storeCardResponse: StoreCardsResponse)
        fun navigateToMyCardDetailActivity(storeCardResponse: StoreCardsResponse)
        fun navigateToDebitOrderActivity(debitOrder: DebitOrder)
        fun navigateToBalanceProtectionInsurance(accountInfo: String?)
        fun setBalanceProtectionInsuranceState(coveredText: String?)
        fun displayCardHolderName(name: String?)
        fun displayViewCardText()
    }

    interface AccountCardDetailPresenter {
        fun createCardHolderName(): String?
        fun displayCardHolderName()
        fun balanceProtectionInsuranceIsCovered(account: Account?): String?
        fun setBalanceProtectionInsuranceState()
        fun getAppCompatActivity(): AppCompatActivity?
        fun setAccountDetailBundle(arguments: Bundle?)
        fun getAccount(): Account?
        fun getDebitOrder(): DebitOrder?
        fun isDebitOrderActive(): Int?
        fun getAccountInStringFormat(): String?
        fun handleUnknownHttpResponse(description: String?)
        fun requestGetAccountStoreCardCardsFromServer()
        fun requestGetUserCLIOfferActiveFromServer()
        fun getStoreCardResponse(): StoreCardsResponse?
        fun handleStoreCardSuccessResponse(storeCardResponse: StoreCardsResponse)
        fun navigateToGetTemporaryStoreCardPopupActivity()
        fun navigateToMyCardDetailActivity()
        fun onDestroy()
        fun navigateToTemporaryStoreCardOnButtonTapped()
        fun navigateToDebitOrderActivityOnButtonTapped()
        fun navigateToBalanceProtectionInsuranceOnButtonTapped()
    }

    interface AccountCardDetailModel {
        fun queryServiceGetAccountStoreCardCards(storeCardsRequestBody: StoreCardsRequestBody?, requestListener: ICommonView<Any>)
        fun queryServiceGetUserCLIOfferActive(productOfferingId: String, requestListener: ICommonView<Any>)
    }
}