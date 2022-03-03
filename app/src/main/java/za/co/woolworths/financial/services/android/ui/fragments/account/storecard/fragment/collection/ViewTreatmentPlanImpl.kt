package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.fragment.collection

import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao

interface IViewTreatmentPlan {
    fun getEligibilityPlan()
}

class ViewTreatmentPlanImpl(val accountProductLandingDao: AccountProductLandingDao) : IViewTreatmentPlan {
    override fun getEligibilityPlan() {
        accountProductLandingDao.getAccountProduct()
    }

}