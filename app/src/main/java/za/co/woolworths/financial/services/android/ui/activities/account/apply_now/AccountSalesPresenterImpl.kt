package za.co.woolworths.financial.services.android.ui.activities.account.apply_now

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.AccountSalesContract
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.AccountSales
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class AccountSalesPresenterImpl(private var mainView: AccountSalesContract.AccountSalesView?, private var model: AccountSalesContract.AccountSalesModel) : AccountSalesContract.AccountSalesPresenter, AccountSalesContract.AccountSalesModel {

    companion object {
        const val ACCOUNT_SALES_CREDIT_CARD = "ACCOUNT_SALES_CREDIT_CARD"
    }

    private fun getGoldOrBlackCardPosition(applyNowState: ApplyNowState) = if (applyNowState == ApplyNowState.GOLD_CREDIT_CARD) 0 else 1

    override fun getCreditCard(): MutableList<AccountSales> = model.getCreditCard()

    override fun getFragment(): Map<String, Fragment>? = model.getFragment()

    override fun getStoreCard(): AccountSales = model.getStoreCard()

    override fun getPersonalLoan(): AccountSales = model.getPersonalLoan()

    fun getOverlayAnchoredHeight(): Int? = KotlinUtils.getBottomSheetBehaviorDefaultAnchoredHeight()

    fun onApplyNowButtonTapped(activity: Activity?) = Utils.openExternalLink(activity, WoolworthsApplication.getApplyNowLink())

    fun onBackPressed(activity: Activity?) = KotlinUtils.onBackPressed(activity)

    fun getStatusBarHeight(actionBarHeight: Int): Int = KotlinUtils.getStatusBarHeight(actionBarHeight)

    fun onDestroy() {
        mainView = null
    }

    fun setAccountSalesDetailPage(storeCard: AccountSales, navDetailController: NavController) {
        val bundle = Bundle()
        bundle.putString(ACCOUNT_SALES_CREDIT_CARD, Gson().toJson(storeCard))
        navDetailController.setGraph(navDetailController.graph, bundle)
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

    override fun maximumExpandableHeight(slideOffset: Float, toolbar: Toolbar?): Int? {
        return toolbar?.layoutParams?.height?.let { toolBarHeight -> getStatusBarHeight(toolBarHeight) }
    }
}