package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list

import android.app.Activity
import android.util.Log
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.ActivityIntentNavigationManager
import za.co.woolworths.financial.services.android.util.FirebaseManager

sealed class  PayMyAccountScreen {
    object RetryOnErrorScreen : PayMyAccountScreen()
    object OpenAccountOptionsOrEnterPaymentAmountDialog : PayMyAccountScreen()
}

class PayMyAccountButtonTap (
    val payMyAccountViewModel: PayMyAccountViewModel,
    val isShimmerEnabled: Boolean
)  {

    fun onTap(activity: Activity?, eventName: String?, result : (PayMyAccountScreen) -> Unit) {
        if (isShimmerEnabled) return
        with(payMyAccountViewModel) {
            //Redirect to payment options when  ABSA cards array is empty for credit card products
            if (getProductGroupCode().equals(
                    AccountsProductGroupCode.CREDIT_CARD.groupCode,
                    ignoreCase = true
                )
            ) {
                if (getAccount()?.cards?.isEmpty() == true) {
                    ActivityIntentNavigationManager.presentPayMyAccountActivity(
                        activity,
                        payMyAccountViewModel.getCardDetail()
                    )
                    return
                }
            }

            with(payMyAccountPresenter) {
                triggerFirebaseEvent(eventName, activity)
                resetAmountEnteredToDefault()
                when (isPaymentMethodOfTypeError()) {
                    true -> {
                        try {
                            result(PayMyAccountScreen.RetryOnErrorScreen)
                        } catch (ex: IllegalStateException) {
                            FirebaseManager.logException(ex)
                        }
                    }
                    false -> {
                        openPayMyAccountOptionOrEnterPaymentAmountDialogFragment(activity)
                        {
                            try {
                                result(PayMyAccountScreen.OpenAccountOptionsOrEnterPaymentAmountDialog)
                              //  directions?.let { navigateSafelyWithNavController(it) }
                            } catch (ex: IllegalStateException) {
                                FirebaseManager.logException(ex)
                            }
                        }
                    }
                }
            }
        }
    }
}