package za.co.woolworths.financial.services.android.contracts

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.google.gson.JsonObject
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.AccountHelpInformation
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import java.io.Serializable

interface IAccountSignedInContract {

    interface MyAccountView {
        fun toolbarTitle(title: String)
        fun showAccountInArrears(account: Account)
        fun hideAccountInArrears(account: Account)
        fun showAccountHelp(informationModelAccount: MutableList<AccountHelpInformation>)
        fun removeBlocksWhenChargedOff(isViewTreatmentPlanActive: Boolean)
        fun removeBlocksOnCollectionCustomer()
        fun showViewTreatmentPlan(dialogButtonType: ViewTreatmentPlanDialogFragment.Companion.ViewTreatmentPlanDialogButtonType)
        fun bottomSheetIsExpanded(): Boolean
        fun chatToCollectionAgent(applyNowState: ApplyNowState, accountList: List<Account>? = null)
        fun showSetUpPaymentPlanButton(state: ApplyNowState)
    }

    interface MyAccountPresenter {
        fun onBackPressed(activity: Activity?)
        fun getAccountBundle(bundle: Bundle?): Serializable?
        fun onDestroy()
        fun getAppCompatActivity(): AppCompatActivity?
        fun setAvailableFundBundleInfo(navDetailController: NavController?)
        fun getMyAccountCardInfo(): Pair<ApplyNowState, Account>?
        fun getToolbarTitle(state: ApplyNowState): String?
        fun showProductOfferOutstanding(state: ApplyNowState)
        fun setAccountCardDetailInfo(navDetailController: NavController?)
        fun setAccountSixMonthInArrears(navDetailController: NavController?)
        fun getSixMonthOutstandingTitleAndCardResource(): Pair<Int, Int>
        fun bottomSheetBehaviourHeight(): Int
        fun bottomSheetBehaviourPeekHeight(): Int
        fun isAccountInArrearsState(): Boolean?
        fun isAccountInDelinquencyMoreThan6Months(): Boolean
        fun chatWithCollectionAgent()
        fun getDeepLinkData(): JsonObject?
        fun deleteDeepLinkData()
        fun isProductInGoodStanding():Boolean
    }

    interface MyAccountModel {
        fun getCardProductInformation(accountIsInArrearsState: Boolean): MutableList<AccountHelpInformation>
    }
}
