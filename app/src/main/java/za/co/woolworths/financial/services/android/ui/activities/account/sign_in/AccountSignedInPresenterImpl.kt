package za.co.woolworths.financial.services.android.ui.activities.account.sign_in

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.IAccountSignedInContract
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.models.dto.account.AccountHelpInformation
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.extension.deviceHeight
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class AccountSignedInPresenterImpl(private var mainView: IAccountSignedInContract.MyAccountView?, private var model: IAccountSignedInContract.MyAccountModel) : IAccountSignedInContract.MyAccountPresenter {

    private var mApplyNowState: ApplyNowState = ApplyNowState.STORE_CARD
    private var mAccountResponse: AccountsResponse? = null
    private var mProductGroupCode: String? = null
    var isAccountInArrearsState : Boolean = false

    companion object {
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

    @Throws(RuntimeException::class)
    private fun getProductCode(applyNowState: ApplyNowState): String {
        return when (applyNowState) {
            ApplyNowState.STORE_CARD -> AccountsProductGroupCode.STORE_CARD.groupCode
            ApplyNowState.SILVER_CREDIT_CARD -> AccountsProductGroupCode.CREDIT_CARD.groupCode
            ApplyNowState.PERSONAL_LOAN -> AccountsProductGroupCode.PERSONAL_LOAN.groupCode
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
        return accountsResponse.accountList?.filter { account -> account?.productGroupCode == getProductCode(mApplyNowState) }?.get(0)
    }

    @Throws(RuntimeException::class)
    override fun getMyAccountCardInfo(): Pair<ApplyNowState, Account>? {
        val account: Account? = getAccount()
        account?.productOfferingId?.let { WoolworthsApplication.getInstance().setProductOfferingId(it) }

        val productGroupInfo = when (account?.productGroupCode?.let { AccountsProductGroupCode.getEnum(it) }) {
            AccountsProductGroupCode.STORE_CARD -> Pair(ApplyNowState.STORE_CARD, account)
            AccountsProductGroupCode.CREDIT_CARD -> when (account.accountNumberBin) {
                Utils.SILVER_CARD -> Pair(ApplyNowState.SILVER_CREDIT_CARD, account)
                Utils.BLACK_CARD -> Pair(ApplyNowState.BLACK_CREDIT_CARD, account)
                Utils.GOLD_CARD -> Pair(ApplyNowState.GOLD_CREDIT_CARD, account)
                else -> throw RuntimeException("Invalid  accountNumberBin ${account.accountNumberBin}")
            }
            AccountsProductGroupCode.PERSONAL_LOAN -> Pair(ApplyNowState.PERSONAL_LOAN, account)
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
            else ->  ""
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

    override fun bottomSheetBehaviourPeekHeight(): Int {
        val height = deviceHeight()
        return (height.div(100)).times(if (isAccountInArrearsState() == true) 14 else 23)
    }

    @SuppressLint("DefaultLocale")
    override fun isAccountInArrearsState(): Boolean? {
        val account = getAccount()
        val productOfferingGoodStanding = account?.productOfferingGoodStanding ?: false
        //  account?.productGroupCode?.toUpperCase() != CREDIT_CARD will hide payable now row for credit card options
        return !productOfferingGoodStanding && account?.productGroupCode?.toUpperCase() != AccountsProductGroupCode.CREDIT_CARD.groupCode.toUpperCase()
    }

    override fun chatWithCollectionAgent() {
        mainView?.chatToCollectionAgent(mApplyNowState, mAccountResponse?.accountList)
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

    @Throws(RuntimeException::class)
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

    override fun bottomSheetBehaviourHeight(): Int {
        val height = deviceHeight()
        val toolbarHeight = KotlinUtils.getToolbarHeight()
        return height.minus(toolbarHeight).minus(KotlinUtils.getStatusBarHeight().div(2))
    }
}