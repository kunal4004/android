package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.navigation

import android.content.Intent
import androidx.activity.result.ActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.navigation.PetInsuranceNavigationImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OfferClickEvent
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_view_application_status.ViewApplicationStatusImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel

import javax.inject.Inject

interface OfferNavigation {
    fun onOffer(viewModel: UserAccountLandingViewModel, activityLauncher: BetterActivityResult<Intent, ActivityResult>?, clicked: OfferClickEvent)
}
class OfferNavigationImpl @Inject constructor(
    private val applicationStatus: ViewApplicationStatusImpl,
    private val petNavigation: PetInsuranceNavigationImpl,
    private val offerIntent: OfferIntent
) : OfferNavigation
{
    override fun onOffer(viewModel: UserAccountLandingViewModel,activityLauncher: BetterActivityResult<Intent, ActivityResult>?, clicked: OfferClickEvent) = when (clicked) {
        is OfferClickEvent.BlackCreditCardApplyNow -> offerIntent.createBlackCreditCardApplyNowIntent()
        is OfferClickEvent.CreditCardApplyNow -> offerIntent.createBlackCreditCardApplyNowIntent()
        is OfferClickEvent.CreditCardInArrears -> offerIntent.createCreditCardApplyNowIntent()
        is OfferClickEvent.PersonalLoanApplyNow -> offerIntent.createPersonalLoanApplyNowIntent()
        is OfferClickEvent.PersonalLoanInArrears -> offerIntent.createPersonalLoanInArrearsIntent()
        is OfferClickEvent.PersonalLoanSignedOutApplyNow -> offerIntent.createPersonalLoanSignedOutApplyNowIntent()
        is OfferClickEvent.PetInsurance -> petNavigation.navigateToPetInsuranceApplyNow(viewModel, activityLauncher)
        is OfferClickEvent.StoreCardApplyNow -> offerIntent.createStoreCardApplyNowIntent()
        is OfferClickEvent.StoreCardInArrears -> offerIntent.createStoreCardInArrearsIntent()
        is OfferClickEvent.ViewApplicationStatus -> offerIntent.createViewApplicationStatusIntent(applicationStatus)
        is OfferClickEvent.ViewFreeCreditReport -> offerIntent.createViewFreeCreditReportIntent()
        is OfferClickEvent.WoolworthStoreCardApplyNow -> offerIntent.createWoolworthStoreCardApplyNowIntent()
    }
    companion object {
        const val APPLY_NOW_STATE = "APPLY_NOW_STATE"
    }
}