package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.helper

import android.app.Activity

interface BeginPayMyAccountJourneyInterface {
    fun triggerFirebaseEvent(evenName: String?,activity:Activity?)
    fun isPayMyAccountFeatureEnabled(): Boolean
    fun isPaymentMethodOfTypeError(): Boolean
    fun openPayMyAccountOptionOrEnterPaymentAmountDialogFragment(activity: Activity?, openEnterPaymentAmountDialogFragment: () -> Unit)
}