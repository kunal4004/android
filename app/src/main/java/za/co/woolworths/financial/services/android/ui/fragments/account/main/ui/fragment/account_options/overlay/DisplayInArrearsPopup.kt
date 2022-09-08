package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.overlay

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.GetAPaymentPlanActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.ITreatmentPlan
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.TreatmentPlanImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountInArrears
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOfferingState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.eliteplan.TakeUpPlanUtil

interface IDisplayInArrearsPopup {
    fun showInArrearsDialogByStatus(status: (AccountOfferingState) -> Unit)
    fun setupInArrearsPopup()
    fun collectCheckEligibilityResult()
    fun onTap(activity: Activity?)
    fun isChargedOff(): Boolean
}

class DisplayInArrearsPopup(
    val viewLifecycleOwner : LifecycleOwner,
    val homeViewModel: AccountProductsHomeViewModel,
    private val treatmentPlanImpl: TreatmentPlanImpl,
    val navigationTo: (DialogData?, EligibilityPlan?) -> Unit
) : IDisplayInArrearsPopup, ITreatmentPlan by treatmentPlanImpl {

    private var mEligibilityPlan: EligibilityPlan? = null

    override fun showInArrearsDialogByStatus(status: (AccountOfferingState) -> Unit) {
        status(
            when (product?.productOfferingGoodStanding ?: false) {
                true -> AccountOfferingState.AccountInGoodStanding
                false -> when {
                    isTakeUpTreatmentPlanJourneyEnabled() -> AccountOfferingState.MakeGetEligibilityCall
                    isViewTreatmentPlanSupported() -> if (isChargedOff()) AccountOfferingState.ShowViewTreatmentPlanPopupFromConfigForChargedOff else AccountOfferingState.ShowViewTreatmentPlanPopupInArrearsFromConfig
                    else -> if (isChargedOff()) AccountOfferingState.AccountIsChargedOff else AccountOfferingState.AccountIsInArrears
                }
            }
        )
    }

    override fun setupInArrearsPopup() {
        showInArrearsDialogByStatus { status ->
            when (status) {
                AccountOfferingState.AccountInGoodStanding -> Unit
                AccountOfferingState.AccountIsInArrears ->  homeViewModel.requestAccountsCollectionsCheckEligibility()
                AccountOfferingState.MakeGetEligibilityCall -> homeViewModel.requestAccountsCollectionsCheckEligibility()
                AccountOfferingState.AccountIsChargedOff -> homeViewModel.emitAccountIsChargedOff()
                AccountOfferingState.ShowViewTreatmentPlanPopupFromConfigForChargedOff -> homeViewModel.emitShowViewTreatmentPlanPopupFromConfigForChargedOff()
                AccountOfferingState.ShowViewTreatmentPlanPopupInArrearsFromConfig -> homeViewModel.emitShowViewTreatmentPlanPopupInArrearsFromConfig()
            }
        }
    }

    override fun collectCheckEligibilityResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            with(homeViewModel) {
                accountsCollectionsCheckEligibility.collectLatest { item ->
                    with(item) {
                        renderSuccess {
                            setEligibilityPlan(output.eligibilityPlan)
                            homeViewModel.setTreatmentPlan(output.eligibilityPlan)
                            navigationTo(homeViewModel.viewTreatmentPlan?.getPopupData(mEligibilityPlan),mEligibilityPlan)
                        }
                        renderEmpty { showAccountInArrears() }
                        renderHttpFailureFromServer { showAccountInArrears() }
                        renderFailure { showAccountInArrears() }
                    }
                }
            }
        }
    }

    private fun setEligibilityPlan(eligibilityPlan : EligibilityPlan?) {
        mEligibilityPlan = eligibilityPlan
        homeViewModel.eligibilityPlan = eligibilityPlan
        homeViewModel.emitEligibilityPlanWhenNotEmpty(eligibilityPlan)

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

            ActionText.VIEW_ELITE_PLAN.value -> KotlinUtils.openTreatmentPlanUrl(
                activity,
                mEligibilityPlan
            )
        }
    }

    private fun showAccountInArrears() = navigationTo(AccountInArrears.InArrears(), mEligibilityPlan)

    override fun isChargedOff(): Boolean {
        return product?.productOfferingStatus.equals(
            Utils.ACCOUNT_CHARGED_OFF,
            ignoreCase = true
        )
    }
}