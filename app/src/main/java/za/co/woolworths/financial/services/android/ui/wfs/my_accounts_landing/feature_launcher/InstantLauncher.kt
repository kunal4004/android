package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_launcher

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_biometrics.BiometricActivityResult
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_biometrics.BiometricActivityResultImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.navigation.PetInsuranceNavigationImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.AccountLandingInstantLauncher
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.data.navigation.CreditCardDeliveryNavigation
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.data.navigation.CreditCardDeliveryNavigationImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import javax.inject.Inject

interface InstantLauncher {
    fun UserAccountLandingViewModel.onLaunch(
        activity: Activity?,
        event: AccountLandingInstantLauncher,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?
    )
}

class InstantLauncherImpl @Inject constructor(
    private val creditCardDelivery: CreditCardDeliveryNavigationImpl,
    private val petInsuranceNavigationImpl: PetInsuranceNavigationImpl,
    private val biometric: BiometricActivityResultImpl
    ) : InstantLauncher,
    CreditCardDeliveryNavigation by creditCardDelivery,
    BiometricActivityResult by biometric {

    override fun UserAccountLandingViewModel.onLaunch(
        activity: Activity?,
        event: AccountLandingInstantLauncher,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?
    ) = when (event) {
        is AccountLandingInstantLauncher.ScheduleCreditCardDelivery -> show(
            applyNowStateToAccountBinNumber = event.applyNowStateToAccountBinNumber,
            response = event.response
        )
        is AccountLandingInstantLauncher.FicaResultListener -> handleResult(
            fica = event.ficaModel, activity)

        is AccountLandingInstantLauncher.PetInsuranceNotCoveredAwarenessModel ->
            petInsuranceNavigationImpl.navigateToPetInsuranceAwarenessModel()

        is AccountLandingInstantLauncher.BiometricIsRequired ->
            registerBiometricForResult(activityLauncher = activityLauncher)
    }

}