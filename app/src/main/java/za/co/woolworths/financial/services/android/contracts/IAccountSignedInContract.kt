package za.co.woolworths.financial.services.android.contracts

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.AccountHelpInformation
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import java.io.Serializable

interface IAccountSignedInContract {

    interface MyAccountView {
        fun toolbarTitle(title: String)
        fun showAccountInArrears(account: Account)
        fun hideAccountInArrears(account: Account)
        fun showAccountHelp(informationModelAccount: MutableList<AccountHelpInformation>)
        fun showAccountChargeOffForMoreThan6Months()
        fun bottomSheetIsExpanded():Boolean
    }

    interface MyAccountPresenter {
        fun onBackPressed(activity: Activity?)
        fun getAccountBundle(bundle: Bundle?): Serializable?
        fun onDestroy()
        fun getAppCompatActivity(): AppCompatActivity?
        fun setAvailableFundBundleInfo(navDetailController: NavController?)
        fun getMyAccountCardInfo(): Pair<ApplyNowState, Account>?
        fun getToolbarTitle(state: ApplyNowState): String?
        fun showProductOfferOutstanding()
        fun setAccountCardDetailInfo(navDetailController: NavController?)
        fun setAccountSixMonthInArrears(navDetailController: NavController?)
        fun getSixMonthOutstandingTitleAndCardResource() : Pair<Int, Int>
        fun bottomSheetBehaviourHeight(): Int
        fun bottomSheetBehaviourPeekHeight() : Int
    }

    interface MyAccountModel {
        fun getCardProductInformation(accountIsInArrearsState: Boolean): MutableList<AccountHelpInformation>
    }
}
