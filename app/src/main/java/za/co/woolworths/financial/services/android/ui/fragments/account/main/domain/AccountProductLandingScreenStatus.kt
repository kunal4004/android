package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import com.awfs.coordination.R
import javax.inject.Inject

interface IAccountProductLandingScreen {
    fun getStartDestinationIdScreen(): Int
}

class AccountProductLandingScreenStatus @Inject constructor(private val treatmentPlanImpl: TreatmentPlanImpl) :
    IAccountProductLandingScreen, ITreatmentPlan by treatmentPlanImpl {

    override fun getStartDestinationIdScreen(): Int {
        return when (treatmentPlanImpl.isProductChargedOff()) {
            true -> R.id.removeBlockOnCollectionFragment
            false -> R.id.accountProductsHomeFragment
        }
    }

}