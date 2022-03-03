package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.AccountOfferingState
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface IAccountProductLandingScreen {
    fun landingScreen(status: (AccountOfferingState) -> Unit)
}

class AccountProductLandingScreenStatus @Inject constructor(treatmentPlanImpl: TreatmentPlanImpl) :
    IAccountProductLandingScreen, ITreatmentPlan by treatmentPlanImpl {
    override fun landingScreen(status: (AccountOfferingState) -> Unit) {
        status(
            when (product?.productOfferingGoodStanding ?: false) {
                true -> AccountOfferingState.AccountInGoodStanding
                false -> {
                    val isProductChargedOff = product?.productOfferingStatus.equals(
                        Utils.ACCOUNT_CHARGED_OFF,
                        ignoreCase = true
                    )
                    when {
                        !isProductChargedOff && isTakeUpTreatmentPlanJourneyEnabled() -> AccountOfferingState.MakeGetEligibilityCall
                        isViewTreatmentPlanSupported() -> if (isProductChargedOff) AccountOfferingState.ShowViewTreatmentPlanPopupFromConfigForChargedOff else AccountOfferingState.ShowViewTreatmentPlanPopupInArrearsFromConfig
                        else -> if (isProductChargedOff) AccountOfferingState.AccountIsChargedOff else AccountOfferingState.AccountIsInArrears
                    }
                }
            }
        )
    }
}