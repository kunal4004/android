package za.co.woolworths.financial.services.android.ui.activities.account

import android.app.Activity
import android.os.Bundle
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.AccountSalesContract
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.AccountSales
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.util.Utils

class AccountSalesPresenterImpl(private var mainView: AccountSalesContract.AccountSalesView?, private var model: AccountSalesContract.AccountSalesModel) : AccountSalesContract.AccountSalesPresenter, AccountSalesContract.AccountSalesModel {

    companion object {
        const val ACCOUNT_SALES_CREDIT_CARD = "ACCOUNT_SALES_CREDIT_CARD"
    }

    override fun switchAccountSalesProduct(applyNowState: ApplyNowState) {
        mainView?.apply {
            when (applyNowState) {
                ApplyNowState.STORE_CARD -> {
                    val storeCard = getStoreCard()
                    displayHeaderItems(storeCard.cardHeader)
                    displayAccountSalesBlackInfo(storeCard)
                }

                ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD -> {
                    // Gold vs Black credit card
                    val creditCard = getCreditCard()
                    displayCreditCard(creditCard[0], creditCard[1], getGoldOrBlackCardPosition(applyNowState))
                }

                ApplyNowState.PERSONAL_LOAN -> {
                    val personalLoan = getPersonalLoan()
                    displayHeaderItems(personalLoan.cardHeader)
                    displayAccountSalesBlackInfo(personalLoan)
                }
            }
        }
    }

    private fun getGoldOrBlackCardPosition(applyNowState: ApplyNowState) = if (applyNowState == ApplyNowState.GOLD_CREDIT_CARD) 0 else 1

    override fun getCreditCard(): MutableList<AccountSales> = model.getCreditCard()

    override fun getStoreCard(): AccountSales = model.getStoreCard()

    override fun getPersonalLoan(): AccountSales = model.getPersonalLoan()

    fun getOverlayAnchoredHeight(): Int? {
        val activity = WoolworthsApplication.getInstance()?.currentActivity
        val height: Int? = activity?.resources?.displayMetrics?.heightPixels ?: 0
        return height?.div(3)?.plus(Utils.dp2px(activity, 18f)) ?: 0
    }

    fun onApplyNowButtonTapped(activity: Activity?) = Utils.openExternalLink(activity, WoolworthsApplication.getApplyNowLink())

    fun setAccountSalesDetailPage(storeCard: AccountSales, navDetailController: NavController) {
        val bundle = Bundle()
        bundle.putString(ACCOUNT_SALES_CREDIT_CARD, Gson().toJson(storeCard))
        navDetailController.setGraph(navDetailController.graph, bundle)
    }

    fun onDestroy() {
        mainView = null
    }

    fun onBackPressed(activity: Activity?) {
        activity?.apply {
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        }
    }

    fun getStatusBarHeight(actionBarHeight: Int): Int {
        val activity = WoolworthsApplication.getInstance()?.currentActivity
        val resId: Int = activity?.resources?.getIdentifier("status_bar_height", "dimen", "android") ?: -1
        var statusBarHeight = 0
        if (resId > 0) {
            statusBarHeight = activity?.resources?.getDimensionPixelSize(resId) ?: 0
        }
        return statusBarHeight + actionBarHeight
    }
}