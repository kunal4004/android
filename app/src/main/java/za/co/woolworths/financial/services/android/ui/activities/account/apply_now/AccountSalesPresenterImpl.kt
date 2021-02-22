package za.co.woolworths.financial.services.android.ui.activities.account.apply_now

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.IAccountSalesContract
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.AccountSales
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.extension.deviceHeight
import za.co.woolworths.financial.services.android.util.KotlinUtils

class AccountSalesPresenterImpl(private var mainView: IAccountSalesContract.AccountSalesView?, private var model: IAccountSalesContract.AccountSalesModel) : IAccountSalesContract.AccountSalesPresenter, IAccountSalesContract.AccountSalesModel {

    private var mApplyNowState: ApplyNowState? = null

    companion object {
        const val ACCOUNT_SALES_CREDIT_CARD = "ACCOUNT_SALES_CREDIT_CARD"
    }

    private fun getGoldOrBlackCardPosition(applyNowState: ApplyNowState) = if (applyNowState == ApplyNowState.GOLD_CREDIT_CARD) 0 else 1

    override fun getCreditCard(): MutableList<AccountSales> = model.getCreditCard()

    override fun getFragment(): Map<String, Fragment>? = model.getFragment()

    override fun getStoreCard(): AccountSales = model.getStoreCard()

    override fun getPersonalLoan(): AccountSales = model.getPersonalLoan()

    @Throws(RuntimeException::class)
    fun onApplyNowButtonTapped(): String? {
        val applyNowLinks = WoolworthsApplication.getApplyNowLink()
       return when (getApplyNowState()) {
            ApplyNowState.STORE_CARD -> applyNowLinks?.storeCard
            ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> applyNowLinks?.creditCard
            ApplyNowState.PERSONAL_LOAN -> applyNowLinks?.personalLoan
            else -> throw RuntimeException("OnApplyNowButtonTapped:: Invalid ApplyNowState ## : ${getApplyNowState()}")
        }
    }

    fun onBackPressed(activity: Activity?) = KotlinUtils.onBackPressed(activity)

    fun onDestroy() {
        mainView = null
    }

    fun setAccountSalesDetailPage(storeCard: AccountSales, navDetailController: NavController) {
        val bundle = Bundle()
        bundle.putString(ACCOUNT_SALES_CREDIT_CARD, Gson().toJson(storeCard))
        val graph = navDetailController.navInflater.inflate(R.navigation.account_sales_detail_nav_graph)
        navDetailController.graph = graph
        navDetailController.setGraph(navDetailController.graph, bundle)
    }

    override fun switchAccountSalesProduct() {
        val applyNowState = getApplyNowState()
        mainView?.apply {
            when (applyNowState) {
                ApplyNowState.STORE_CARD -> {
                    val storeCard = getStoreCard()
                    displayHeaderItems(storeCard.cardHeader)
                    displayAccountSalesBlackInfo(storeCard)
                }

                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD -> {
                    // Gold vs Black credit card
                    displayCreditCard(getFragment(), getGoldOrBlackCardPosition(applyNowState))
                }

                ApplyNowState.PERSONAL_LOAN -> {
                    val personalLoan = getPersonalLoan()
                    displayHeaderItems(personalLoan.cardHeader)
                    displayAccountSalesBlackInfo(personalLoan)
                }
                else -> throw RuntimeException("Invalid applyNowState state $applyNowState")
            }
        }
    }

    override fun setAccountSalesIntent(intent: Intent?) {
        mApplyNowState = intent?.extras?.getSerializable("APPLY_NOW_STATE") as? ApplyNowState
    }

    override fun getApplyNowState(): ApplyNowState? = mApplyNowState

    override fun isCreditCardProduct(): Boolean {
         return  when (getApplyNowState()) {
            ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD -> false
            else -> true
        }
    }

    override fun bottomSheetPeekHeight(): Int {
        return (deviceHeight() / 3) +  KotlinUtils.getStatusBarHeight()
    }

    override fun bottomSheetBehaviourHeight(): Int {
        val height = deviceHeight()
        val toolbarHeight = KotlinUtils.getToolbarHeight()
        return height.minus(toolbarHeight).minus(KotlinUtils.getStatusBarHeight().div(2))
    }
}