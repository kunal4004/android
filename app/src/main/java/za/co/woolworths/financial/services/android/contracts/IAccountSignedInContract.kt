package za.co.woolworths.financial.services.android.contracts

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
    }

    interface MyAccountPresenter {
        fun onBackPressed(activity: Activity?)
        fun getOverlayAnchoredHeight(): Int?
        fun getAccountBundle(bundle: Bundle?): Serializable?
        fun onDestroy()
        fun getStatusBarHeight(actionBarHeight: Int): Int
        fun getAppCompatActivity(): AppCompatActivity?
        fun setAvailableFundBundleInfo(navDetailController: NavController?)
        fun getMyAccountCardInfo(): Pair<ApplyNowState, Account>?
        fun getToolbarTitle(state: ApplyNowState): String?
        fun showProductOfferOutstanding()
        fun getStatusBarHeight(slideOffset: Float, toolbar: Toolbar?): Int?
        fun getStatusBarHeight(): Int?
        fun setAccountCardDetailInfo(navDetailController: NavController?)
        fun setAccountSixMonthInArrears(navDetailController: NavController?)
        fun getSixMonthOutstandingTitleAndCardResource() : Pair<Int, Int>
    }

    interface MyAccountModel {
        fun getCardProductInformation(accountIsInArrearsState: Boolean): MutableList<AccountHelpInformation>
    }
}
