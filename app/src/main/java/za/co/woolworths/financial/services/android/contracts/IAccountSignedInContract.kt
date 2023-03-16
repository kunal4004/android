package za.co.woolworths.financial.services.android.contracts

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.google.gson.JsonObject
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.account.AccountHelpInformation
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import java.io.Serializable

interface IAccountSignedInContract {

    interface MyAccountView {
        fun toolbarTitle(title: String)
        fun showAccountInArrears(account: Account?, showDialog: Boolean)
        fun showAboveSixMonthsAccountInDelinquencyPopup(eligibilityPlan: EligibilityPlan?)
        fun hideAccountInArrears(account: Account)
        fun showAccountHelp(informationModelAccount: MutableList<AccountHelpInformation>)
        fun removeBlocksWhenChargedOff()
        fun removeBlocksOnCollectionCustomer()
        fun showViewTreatmentPlan(state: ApplyNowState, eligibilityPlan: EligibilityPlan?)
        fun bottomSheetIsExpanded(): Boolean
        fun chatToCollectionAgent(applyNowState: ApplyNowState, accountList: List<Account>? = null)
        fun showPlanButton(state: ApplyNowState, eligibilityPlan: EligibilityPlan?)
        fun removeBlocksWhenChargedOff(isViewTreatmentPlanActive: Boolean)
        fun showViewTreatmentPlan(viewPaymentOptions: Boolean)
    }

    interface MyAccountPresenter {
        fun onBackPressed(activity: Activity?)
        fun getAccountBundle(bundle: Bundle?): Serializable?
        fun onDestroy()
        fun getAppCompatActivity(): AppCompatActivity?
        fun getMyAccountCardInfo(): Pair<ApplyNowState, Account>?
        fun getToolbarTitle(state: ApplyNowState): String?
        fun showProductOfferOutstanding(
            state: ApplyNowState,
            myAccountsViewModel: MyAccountsRemoteApiViewModel,
            showPopupIfNeeded: Boolean
        )
        fun setAccountCardDetailInfo(navDetailController: NavController?)
        fun setAccountSixMonthInArrears(navDetailController: NavController?)
        fun getSixMonthOutstandingTitleAndCardResource(): Pair<Int, Int>
        fun bottomSheetBehaviourHeight(): Int
        fun bottomSheetBehaviourPeekHeight(): Int
        fun isAccountInArrearsState(): Boolean?
        fun isAccountInDelinquencyMoreThan6Months(): Boolean
        fun chatWithCollectionAgent()
        fun getDeepLinkData(): JsonObject?
        fun getEligibilityPlan(): EligibilityPlan?
        fun deleteDeepLinkData()
        fun isProductInGoodStanding():Boolean
        fun setAvailableFundBundleInfo(
            navDetailController: NavController?,
            myAccountsViewModel: MyAccountsRemoteApiViewModel
        )
    }

    interface MyAccountModel {
        fun getCardProductInformation(accountIsInArrearsState: Boolean): MutableList<AccountHelpInformation>
    }
}
