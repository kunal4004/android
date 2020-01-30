package za.co.woolworths.financial.services.android.contracts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PaymentMethod
import za.co.woolworths.financial.services.android.models.dto.account.*
import java.io.Serializable
import java.util.*

interface AccountSignedInContract {

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
        fun showProductOfferOutStanding()
        fun maximumExpandableHeight(slideOffset: Float, toolbar: Toolbar?): Int?
        fun setAccountCardDetailInfo(navDetailController: NavController?)
        fun setAccountSixMonthInArrears(navDetailController: NavController?)
        fun getSixMonthOutstandingTitleAndCardResource() : Pair<Int, Int>
    }

    interface MyAccountModel {
        fun getCardProductInformation(accountIsInArrearsState: Boolean): MutableList<AccountHelpInformation>
    }
}

interface AccountSalesContract {

    interface AccountSalesView {
        fun displayHeaderItems(cardHeader: CardHeader?)
        fun displayAccountSalesBlackInfo(storeCard: AccountSales)
        fun displayCreditCard(fragmentList: Map<String, Fragment>?, position: Int)
        fun displayCreditCardFrontUI(position: Int)
    }

    interface AccountSalesPresenter {
        fun switchAccountSalesProduct()
        fun maximumExpandableHeight(slideOffset: Float, toolbar: Toolbar?): Int?
        fun setAccountSalesIntent(intent: Intent?)
        fun getApplyNowState(): ApplyNowState?
    }

    interface AccountSalesModel {
        fun getCreditCard(): MutableList<AccountSales>
        fun getFragment(): Map<String, Fragment>?
        fun getStoreCard(): AccountSales
        fun getPersonalLoan(): AccountSales
    }
}

interface PaymentOptionContract {

    interface PaymentOptionView {
        fun showPaymentDetail(paymentDetail: Map<String, String>?)
        fun setHowToPayLogo(headerDrawable: HeaderDrawable?)
        fun showABSAInfo()
        fun hideABSAInfo()
        fun setPaymentOption(paymentMethods: MutableList<PaymentMethod>?)
    }

    interface PaymentOptionPresenter {
        fun retrieveAccountBundle(intent: Intent?)
        fun getAccount(): Account?
        fun getPaymentDetail(): Map<String, String>
        fun displayPaymentDetail()
        fun setHowToPayLogo()
        fun loadABSACreditCardInfoIfNeeded()
        fun getPaymentMethod(): MutableList<PaymentMethod>?
        fun displayPaymentMethod()
        fun initView()
    }

    interface PaymentOptionModel {
        fun getAccountDetailValues(): HashMap<String, String?>
        fun getDrawableHeader(): List<HeaderDrawable>
    }
}