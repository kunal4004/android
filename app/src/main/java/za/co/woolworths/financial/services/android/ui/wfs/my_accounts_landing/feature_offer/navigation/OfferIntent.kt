package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.navigation

import android.app.Activity
import android.content.Intent
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.CreditReportTUActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountSection
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.activities.ApplyNowActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance.apply.PetInsuranceApplyNowActivity
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AccountLandingFirebaseManagerImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_view_application_status.ViewApplicationStatusImpl
import javax.inject.Inject

interface IOfferIntent {
    fun createBlackCreditCardApplyNowIntent()
    fun createPersonalLoanApplyNowIntent()
    fun createViewApplicationStatusIntent(applicationStatus: ViewApplicationStatusImpl)
    fun createViewFreeCreditReportIntent()
    fun createPetInsuranceIntent()
    fun createCreditCardApplyNowIntent()
    fun createPersonalLoanInArrearsIntent()
    fun createPersonalLoanSignedOutApplyNowIntent()
    fun createStoreCardApplyNowIntent()
    fun createStoreCardInArrearsIntent()
    fun createWoolworthStoreCardApplyNowIntent()
    fun launchApplyNowActivity(applyNowState: ApplyNowState)
    fun launchCreditReportActivity()
    fun launchViewApplicationStatusActivity(applicationStatus: ViewApplicationStatusImpl)
}

class OfferIntent @Inject constructor(private val activity : Activity?,
private val analytics : AccountLandingFirebaseManagerImpl) : IOfferIntent {

    override fun createBlackCreditCardApplyNowIntent() {
        analytics.onApplyNowCreditCardItem()
        launchApplyNowActivity(ApplyNowState.BLACK_CREDIT_CARD)
    }

    override fun createPersonalLoanApplyNowIntent() {
        analytics.onApplyNowPersonalLoanItem()
        launchApplyNowActivity(ApplyNowState.PERSONAL_LOAN)
    }

    override fun createViewApplicationStatusIntent(applicationStatus: ViewApplicationStatusImpl) {
        launchViewApplicationStatusActivity(applicationStatus = applicationStatus)
    }

    override fun createViewFreeCreditReportIntent() {
        analytics.onCreditReportItem()
        launchCreditReportActivity()
    }

    override fun createPetInsuranceIntent() {
        activity?.apply {
            startActivity(Intent(this, PetInsuranceApplyNowActivity::class.java))
        }
    }

    override fun createCreditCardApplyNowIntent() {
        analytics.onApplyNowCreditCardItem()
        launchApplyNowActivity(ApplyNowState.BLACK_CREDIT_CARD)
    }

    override fun createPersonalLoanInArrearsIntent() {}

    override fun createPersonalLoanSignedOutApplyNowIntent() {
        analytics.onApplyNowPersonalLoanItem()
        launchApplyNowActivity(ApplyNowState.PERSONAL_LOAN)
    }

    override fun createStoreCardApplyNowIntent() {
        analytics.onApplyNowStoreCardItem()
        launchApplyNowActivity(ApplyNowState.STORE_CARD)
    }

    override fun createStoreCardInArrearsIntent() {}

    override fun createWoolworthStoreCardApplyNowIntent() {
        analytics.onApplyNowStoreCardItem()
        launchApplyNowActivity(ApplyNowState.STORE_CARD)
    }

    override fun launchApplyNowActivity(applyNowState: ApplyNowState) {
        activity?.let { context ->
            val intent = Intent(context, ApplyNowActivity::class.java)
            intent.putExtra(OfferNavigationImpl.APPLY_NOW_STATE, applyNowState)
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    override fun launchCreditReportActivity() {
        activity?.let { context ->
            val intent = Intent(context, CreditReportTUActivity::class.java)
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    override fun launchViewApplicationStatusActivity(applicationStatus: ViewApplicationStatusImpl) {
        activity ?: return
        applicationStatus.onClick(MyAccountSection.AccountLanding, activity)
    }

}