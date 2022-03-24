package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOfferingState
import javax.inject.Inject

interface IAccountProductLandingScreen {
    fun getStartDestinationIdScreen(): Int
    fun getPopupDialogStatus(status: (AccountOfferingState) -> Unit)
}

class AccountProductLandingScreenStatus @Inject constructor(private val treatmentPlanImpl: TreatmentPlanImpl) :
    IAccountProductLandingScreen, ITreatmentPlan by treatmentPlanImpl {

    private val isProductChargedOff = treatmentPlanImpl.isProductChargedOff()
    private val isAccountInArrears = treatmentPlanImpl.isProductInGoodStanding()

    override fun getStartDestinationIdScreen(): Int {
        return when (isAccountInArrears) {
            true -> R.id.accountProductsHomeFragment
            false -> when (isProductChargedOff) {
                true -> R.id.removeBlockOnCollectionFragment2
                false -> R.id.accountProductsHomeFragment
            }
        }
    }

    override fun getPopupDialogStatus(status: (AccountOfferingState) -> Unit) {
        status(
            when (product?.productOfferingGoodStanding ?: false) {
                true -> AccountOfferingState.AccountInGoodStanding
                false -> when {
                    isViewTreatmentPlanSupported() || isTakeUpTreatmentPlanJourneyEnabled() ->  if (isProductChargedOff) AccountOfferingState.MakeGetEligibilityCall else AccountOfferingState.ShowViewTreatmentPlanPopupInArrearsFromConfig
                    else -> if (isProductChargedOff) AccountOfferingState.AccountIsChargedOff else AccountOfferingState.AccountIsInArrears
                }
            }
        )
    }

}