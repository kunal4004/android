package za.co.woolworths.financial.services.android.ui.activities.account.sign_in

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.IAccountSignedInContract
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.models.dto.account.AccountHelpInformation
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class AccountSignedInPresenterImpl(private var mainView: IAccountSignedInContract.MyAccountView?, private var model: IAccountSignedInContract.MyAccountModel) : IAccountSignedInContract.MyAccountPresenter {

    private var mApplyNowState: ApplyNowState = ApplyNowState.STORE_CARD
    private var mAccountResponse: AccountsResponse? = null
    private var mProductGroupCode: String? = null

    companion object {
        private const val STORE_CARD: String = "SC"
        private const val CREDIT_CARD: String = "CC"
        private const val PERSONAL_LOAN: String = "PL"
        const val MY_ACCOUNT_RESPONSE = "MY_ACCOUNT_RESPONSE"
        const val APPLY_NOW_STATE = "APPLY_NOW_STATE"
    }

    override fun getAccountBundle(bundle: Bundle?): Pair<ApplyNowState?, AccountsResponse?>? {
        mApplyNowState = bundle?.getSerializable(APPLY_NOW_STATE) as? ApplyNowState
                ?: ApplyNowState.STORE_CARD
        val accountResponseString = bundle?.getString(MY_ACCOUNT_RESPONSE, "")
        mAccountResponse = Gson().fromJson(accountResponseString, AccountsResponse::class.java)
        return Pair(mApplyNowState, mAccountResponse)
    }

    private fun getProductCode(applyNowState: ApplyNowState): String {
        return when (applyNowState) {
            ApplyNowState.STORE_CARD -> STORE_CARD
            ApplyNowState.SILVER_CREDIT_CARD -> CREDIT_CARD
            ApplyNowState.PERSONAL_LOAN -> PERSONAL_LOAN
            else -> throw RuntimeException("ApplyNowState value not supported $applyNowState")
        }
    }

    override fun setAvailableFundBundleInfo(navDetailController: NavController?) {
        this.mProductGroupCode = getProductCode(mApplyNowState)

        val bundle = Bundle()
        val accountInfo = getMyAccountCardInfo()
        bundle.putString(MY_ACCOUNT_RESPONSE, Gson().toJson(accountInfo))
        val graph = navDetailController?.graph
        graph?.startDestination = when (accountInfo?.first) {
            ApplyNowState.STORE_CARD -> R.id.storeCardFragment
            ApplyNowState.SILVER_CREDIT_CARD -> R.id.silverCreditCardFragment
            ApplyNowState.PERSONAL_LOAN -> R.id.personalLoanFragment
            ApplyNowState.BLACK_CREDIT_CARD -> R.id.blackCreditCardFragment
            ApplyNowState.GOLD_CREDIT_CARD -> R.id.goldCreditCardFragment
            else -> throw (java.lang.RuntimeException(" setAvailableFundBundleInfo() :: Invalid account State found $accountInfo"))
        }

        navDetailController?.setGraph(navDetailController.graph, bundle)
        showProductOfferOutstanding()
    }

    private fun getAccount(accountsResponse: AccountsResponse): Account? {
        return accountsResponse.accountList?.filter { account -> account.productGroupCode == getProductCode(mApplyNowState) }?.get(0)
    }

    override fun getMyAccountCardInfo(): Pair<ApplyNowState, Account>? {
        val account: Account? = getAccount()
        account?.productOfferingId?.let { WoolworthsApplication.getInstance().setProductOfferingId(it) }
        val productGroupInfo = when (account?.productGroupCode) {
            STORE_CARD -> Pair(ApplyNowState.STORE_CARD, account)
            CREDIT_CARD -> when (account.accountNumberBin) {
                Utils.SILVER_CARD -> Pair(ApplyNowState.SILVER_CREDIT_CARD, account)
                Utils.BLACK_CARD -> Pair(ApplyNowState.BLACK_CREDIT_CARD, account)
                Utils.GOLD_CARD -> Pair(ApplyNowState.GOLD_CREDIT_CARD, account)
                else -> throw RuntimeException("Invalid  accountNumberBin ${account.accountNumberBin}")
            }
            PERSONAL_LOAN -> Pair(ApplyNowState.PERSONAL_LOAN, account)
            else -> throw RuntimeException("Invalid  productGroupCode ${account?.productGroupCode}")
        }

        getToolbarTitle(productGroupInfo.first)?.let { toolbarTitle -> mainView?.toolbarTitle(toolbarTitle) }

        return productGroupInfo
    }

    override fun getToolbarTitle(state: ApplyNowState): String? {
        val resources = getAppCompatActivity()?.resources
        return when (state) {
            ApplyNowState.STORE_CARD -> resources?.getString(R.string.store_card_title)
            ApplyNowState.SILVER_CREDIT_CARD -> resources?.getString(R.string.silver_credit_card)
            ApplyNowState.BLACK_CREDIT_CARD -> resources?.getString(R.string.blackCreditCard_title)
            ApplyNowState.GOLD_CREDIT_CARD -> resources?.getString(R.string.goldCreditCard_title)
            ApplyNowState.PERSONAL_LOAN -> resources?.getString(R.string.personal_loan)
        }
    }

    override fun showProductOfferOutstanding() {
        val account = getAccount()
        account?.apply {
            return when {
                (!productOfferingGoodStanding && productOfferingStatus.equals(Utils.ACCOUNT_CHARGED_OFF, ignoreCase = true)) -> {
                    // account is in arrears for more than 6 months
                    mainView?.showAccountChargeOffForMoreThan6Months()!!
                }
                !productOfferingGoodStanding -> { // account is in arrears
                    mainView?.showAccountInArrears(account)
                    val informationModel = getCardProductInformation(true)
                    mainView?.showAccountHelp(informationModel)!!
                }
                else -> {
                    mainView?.hideAccountInArrears(account)
                    val informationInArrearsModel = getCardProductInformation(false)
                    mainView?.showAccountHelp(informationInArrearsModel)!!
                }
            }
        }
    }

   override fun bottomSheetBehaviourPeekHeight(appCompatActivity: AppCompatActivity?): Int {
        appCompatActivity?.apply {
            val height = resources?.displayMetrics?.heightPixels ?: 0
            return (height.div(100)).times(23)
        }
        return 0
    }

    private fun getAccount(): Account? {
        return mAccountResponse?.let { account -> getAccount(account) }
    }

    override fun getAppCompatActivity(): AppCompatActivity? = WoolworthsApplication.getInstance()?.currentActivity as? AppCompatActivity

    override fun onBackPressed(activity: Activity?) = KotlinUtils.onBackPressed(activity)

    override fun onDestroy() {
        mainView = null
    }

    private fun getCardProductInformation(accountIsInArrearsState: Boolean): MutableList<AccountHelpInformation> {
        return model.getCardProductInformation(accountIsInArrearsState)
    }

    override fun setAccountCardDetailInfo(navDetailController: NavController?) {
        val bundle = Bundle()
        val accountInfo = getMyAccountCardInfo()
        bundle.putString(MY_ACCOUNT_RESPONSE, Gson().toJson(accountInfo))
        val graph = navDetailController?.graph
        graph?.startDestination = when (accountInfo?.first) {
            ApplyNowState.STORE_CARD -> R.id.storeCardDetail
            ApplyNowState.SILVER_CREDIT_CARD -> R.id.silverCreditCardDetail
            ApplyNowState.PERSONAL_LOAN -> R.id.personalLoanDetail
            ApplyNowState.BLACK_CREDIT_CARD -> R.id.blackCreditCardDetail
            ApplyNowState.GOLD_CREDIT_CARD -> R.id.goldCreditCardDetail
            else -> throw (java.lang.RuntimeException(" setAccountCardDetailInfo() :: Invalid account State found $accountInfo"))
        }
        navDetailController?.setGraph(navDetailController.graph, bundle)
    }

    override fun setAccountSixMonthInArrears(navDetailController: NavController?) {
        val bundle = Bundle()
        val resources = getSixMonthOutstandingTitleAndCardResource()
        bundle.putString(MY_ACCOUNT_RESPONSE, Gson().toJson(resources))
        navDetailController?.setGraph(navDetailController.graph, bundle)
    }

    override fun getSixMonthOutstandingTitleAndCardResource(): Pair<Int, Int> {
        val accountInfo = getMyAccountCardInfo()
        return when (accountInfo?.first) {
            ApplyNowState.STORE_CARD -> Pair(R.drawable.w_store_card, R.string.store_card_title)
            ApplyNowState.SILVER_CREDIT_CARD -> Pair(R.drawable.w_silver_credit_card, R.string.silver_credit_card)
            ApplyNowState.BLACK_CREDIT_CARD -> Pair(R.drawable.w_black_credit_card, R.string.blackCreditCard_title)
            ApplyNowState.GOLD_CREDIT_CARD -> Pair(R.drawable.w_gold_credit_card, R.string.goldCreditCard_title)
            ApplyNowState.PERSONAL_LOAN -> Pair(R.drawable.w_personal_loan_card, R.string.personalLoanCard_title)
            else -> throw RuntimeException("SixMonthOutstanding Invalid  ApplyNowState ${accountInfo?.first}")
        }
    }

    override fun bottomSheetBehaviourHeight(appCompatActivity: AppCompatActivity?): Int {
        appCompatActivity?.apply {
            val displayMetrics: DisplayMetrics = resources.displayMetrics
            val height = displayMetrics.heightPixels
            val toolbarHeight = KotlinUtils.getToolbarHeight(appCompatActivity)
            return height.minus(toolbarHeight)
        }
        return 0
    }
}