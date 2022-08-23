package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOfferingState
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface IAccountProductLandingScreen {
    fun getStartDestinationIdScreen(): Int
    fun showInArrearsDialogByStatus(status: (AccountOfferingState) -> Unit)
    fun isChargedOff(): Boolean
}

class AccountProductLandingScreenStatus @Inject constructor(private val treatmentPlanImpl: TreatmentPlanImpl) :
    IAccountProductLandingScreen, ITreatmentPlan by treatmentPlanImpl {

    override fun getStartDestinationIdScreen(): Int {
        return when (treatmentPlanImpl.isProductChargedOff()) {
            true -> R.id.removeBlockOnCollectionFragment
            false -> R.id.accountProductsHomeFragment
        }
    }

    override fun showInArrearsDialogByStatus(status: (AccountOfferingState) -> Unit) {
        status(
            when (product?.productOfferingGoodStanding ?: false) {
                true -> AccountOfferingState.AccountInGoodStanding
                false -> when {
                    isTakeUpTreatmentPlanJourneyEnabled() -> AccountOfferingState.MakeGetEligibilityCall
                    isViewTreatmentPlanSupported() -> if (isChargedOff()) AccountOfferingState.ShowViewTreatmentPlanPopupFromConfigForChargedOff else AccountOfferingState.ShowViewTreatmentPlanPopupInArrearsFromConfig
                    else -> if (isChargedOff()) AccountOfferingState.AccountIsChargedOff else AccountOfferingState.AccountIsInArrears
                }
            })
    }

    override fun isChargedOff(): Boolean {
        return product?.productOfferingStatus.equals(
            Utils.ACCOUNT_CHARGED_OFF,
            ignoreCase = true
        )
    }

}