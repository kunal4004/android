package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.helper

import android.app.Activity
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigPayMyAccount
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.ActivityIntentNavigationManager
import za.co.woolworths.financial.services.android.util.Utils

class BeginPayMyAccountJourneyActionImpl(
    val payMyAccountViewModel: PayMyAccountViewModel
) : BeginPayMyAccountJourneyInterface {

    override fun triggerFirebaseEvent(evenName: String?, activity: Activity?) {
        return Utils.triggerFireBaseEvents(evenName, activity)
    }

    override fun isPayMyAccountFeatureEnabled(): Boolean {
        val payMyAccountOption: ConfigPayMyAccount? = AppConfigSingleton.mPayMyAccount
        return payMyAccountOption?.isFeatureEnabled() ?: false
    }

    override fun isPaymentMethodOfTypeError(): Boolean {
        return payMyAccountViewModel.getPaymentMethodType() == PayMyAccountViewModel.PAYUMethodType.ERROR
    }

    override fun openPayMyAccountOptionOrEnterPaymentAmountDialogFragment(
        activity: Activity?, openEnterPaymentAmountDialogFragment: () -> Unit) {
        val payUMethodType = payMyAccountViewModel.getCardDetail()?.payuMethodType
        when (payUMethodType == PayMyAccountViewModel.PAYUMethodType.CARD_UPDATE
                && isPayMyAccountFeatureEnabled()) {
            true -> openEnterPaymentAmountDialogFragment()
            false -> activity?.let { act ->
                ActivityIntentNavigationManager.presentPayMyAccountActivity(
                    act, payMyAccountViewModel.getCardDetail())
            }
        }
    }
}