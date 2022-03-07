package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOfferingState
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface IAccountProductLandingScreen {
    fun getStartDestinationIdScreen(): Int
    fun getPopupDialogStatus(status: (AccountOfferingState) -> Unit)
    fun isProductChargedOff(): Boolean
}

class AccountProductLandingScreenStatus @Inject constructor(treatmentPlanImpl: TreatmentPlanImpl) :
    IAccountProductLandingScreen, ITreatmentPlan by treatmentPlanImpl {

    private val isProductChargedOff = isProductChargedOff()

    override fun getStartDestinationIdScreen(): Int {
        return when (product?.productOfferingGoodStanding ?: false) {
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
                    !isProductChargedOff && isTakeUpTreatmentPlanJourneyEnabled() -> AccountOfferingState.MakeGetEligibilityCall
                    isViewTreatmentPlanSupported() -> if (isProductChargedOff) AccountOfferingState.ShowViewTreatmentPlanPopupFromConfigForChargedOff else AccountOfferingState.ShowViewTreatmentPlanPopupInArrearsFromConfig
                    else -> if (isProductChargedOff) AccountOfferingState.AccountIsChargedOff else AccountOfferingState.AccountIsInArrears
                }
            }
        )
    }

    override fun isProductChargedOff(): Boolean {
        return product?.productOfferingStatus.equals(Utils.ACCOUNT_CHARGED_OFF, ignoreCase = true)
    }
}