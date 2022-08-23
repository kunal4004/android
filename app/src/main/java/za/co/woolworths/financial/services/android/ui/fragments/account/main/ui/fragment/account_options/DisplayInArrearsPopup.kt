package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.GetAPaymentPlanActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderEmpty
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderFailure
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderHttpFailureFromServer
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOfferingState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.eliteplan.TakeUpPlanUtil

interface IDisplayInArrearsPopup {
    fun setupInArrearsPopup()
    fun collectCheckEligibilityResult()
    fun onTap(activity: Activity?)
}

class DisplayInArrearsPopup(
    val fragment: Fragment,
    val homeViewModel: AccountProductsHomeViewModel,
    val navigationTo: (Pair<DialogData?, EligibilityPlan?>) -> Unit
) : IDisplayInArrearsPopup {

    private var mEligibilityPlan: EligibilityPlan? = null

    override fun setupInArrearsPopup() {
        with(homeViewModel) {
            showInArrearsDialogByStatus { status ->
                when (status) {
                    is AccountOfferingState.AccountIsInArrears ->  Log.e("showInArrearsDias", "F")
                    AccountOfferingState.AccountInGoodStanding -> Log.e("showInArrearsDias", "A")
                    AccountOfferingState.AccountIsChargedOff ->  Log.e("showInArrearsDias", "B")
                    AccountOfferingState.MakeGetEligibilityCall ->  homeViewModel.requestAccountsCollectionsCheckEligibility()
                    AccountOfferingState.ShowViewTreatmentPlanPopupFromConfigForChargedOff ->  Log.e("showInArrearsDias", "D")
                    AccountOfferingState.ShowViewTreatmentPlanPopupInArrearsFromConfig ->  Log.e("showInArrearsDias", "E")
                }
            }
        }
    }

    override fun collectCheckEligibilityResult() {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            with(homeViewModel) {
                accountsCollectionsCheckEligibility.collect { item ->
                    with(item) {
                        renderSuccess {
                            mEligibilityPlan = output.eligibilityPlan
                            emitEligibilityPlan(mEligibilityPlan)
                            homeViewModel.setUpViewTreatmentPlan(mEligibilityPlan)
                             navigationTo(
                                    Pair(
                                        homeViewModel.mViewTreatmentPlanImpl?.getPopUpData(mEligibilityPlan),
                                        mEligibilityPlan
                                    )
                                )
                        }
                        renderEmpty { showAccountInArrears() }
                        renderHttpFailureFromServer { showAccountInArrears() }
                        renderFailure { showAccountInArrears() }
                    }
                }
            }
        }
    }

    override fun onTap(activity: Activity?) {
        activity ?: return
        when (mEligibilityPlan?.actionText) {

            ActionText.START_NEW_ELITE_PLAN.value -> {
                TakeUpPlanUtil.takeUpPlanEventLog(ApplyNowState.STORE_CARD, activity)
                val intent = Intent(activity, GetAPaymentPlanActivity::class.java)
                intent.putExtra(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN, mEligibilityPlan)
                activity.startActivityForResult(intent, AccountsOptionFragment.REQUEST_ELITEPLAN)
                activity.overridePendingTransition(R.anim.slide_from_right, R.anim.stay)
            }

            ActionText.VIEW_ELITE_PLAN.value -> KotlinUtils.openTreatmentPlanUrl(activity,
                mEligibilityPlan)
        }
    }

    private fun showAccountInArrears() = navigationTo(Pair(DialogData.AccountInArrDialog(), null))

}