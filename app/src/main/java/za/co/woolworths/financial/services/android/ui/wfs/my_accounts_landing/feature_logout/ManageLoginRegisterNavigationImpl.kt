package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_logout

import android.content.Intent
import androidx.activity.result.ActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.ManageLoginRegister
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import javax.inject.Inject

interface ManageLoginRegisterNavigation {
     fun UserAccountLandingViewModel.onManageLoginRegister(event: ManageLoginRegister,
                                                           activityLauncher: BetterActivityResult<Intent, ActivityResult>?)
}

class ManageLoginRegisterNavigationImpl @Inject constructor(private val logout: ManageLoginRegisterActivityResultImpl)
    : ManageLoginRegisterNavigation, ManageLoginRegisterActivityResult by logout  {
    override fun UserAccountLandingViewModel.onManageLoginRegister(event: ManageLoginRegister,
                                                                   activityLauncher: BetterActivityResult<Intent, ActivityResult>?) {
        when (event) {
            ManageLoginRegister.Register -> createRegisterActivityResult(activityLauncher)
            ManageLoginRegister.SignIn -> createSignInActivityResult(activityLauncher)
            ManageLoginRegister.SignOut -> createSignOutActivityResult(activityLauncher)
        }
    }
}