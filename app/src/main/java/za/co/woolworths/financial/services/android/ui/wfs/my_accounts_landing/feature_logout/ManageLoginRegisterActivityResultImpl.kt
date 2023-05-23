package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_logout
import android.content.Intent
import androidx.activity.result.ActivityResult
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AccountLandingFirebaseManagerImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel

import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

interface ManageLoginRegisterActivityResult {
    fun UserAccountLandingViewModel.createSignInActivityResult(activityLauncher: BetterActivityResult<Intent, ActivityResult>?)
    fun UserAccountLandingViewModel.createRegisterActivityResult(activityLauncher: BetterActivityResult<Intent, ActivityResult>?)
    fun UserAccountLandingViewModel.createSignOutActivityResult(activityLauncher: BetterActivityResult<Intent, ActivityResult>?)
}

class ManageLoginRegisterActivityResultImpl @Inject constructor(
    private val intent: LogoutIntentImpl,
    private val analytics : AccountLandingFirebaseManagerImpl,
    ) : ManageLoginRegisterActivityResult, LogoutIntent by intent {

    override fun UserAccountLandingViewModel.createSignInActivityResult(activityLauncher: BetterActivityResult<Intent, ActivityResult>?) {
        analytics.onSignInButton()
        /* clear all cnc and dash browsing data when user login*/
        WoolworthsApplication.setCncBrowsingValidatePlaceDetails(null)
        WoolworthsApplication.setDashBrowsingValidatePlaceDetails(null)
        KotlinUtils.browsingCncStore = null

        val signInIntent = createSignInIntent()
        activityLauncher?.launch(signInIntent, onActivityResult = { result ->
            setUserAuthenticated(result.resultCode)
        })
    }

    override fun UserAccountLandingViewModel.createRegisterActivityResult(activityLauncher: BetterActivityResult<Intent, ActivityResult>?) {
        val registerIntent = createRegisterIntent()
        analytics.onRegisterButton()
        analytics.onRegisterSignUpButton()
        activityLauncher?.launch(registerIntent, onActivityResult = { result ->
            setUserAuthenticated(result.resultCode)
        })
    }

    override fun UserAccountLandingViewModel.createSignOutActivityResult(activityLauncher: BetterActivityResult<Intent, ActivityResult>?) {
        val signOutIntent = createSignOutIntent()
        activityLauncher?.launch(signOutIntent, onActivityResult = { result ->
            setUserUnAuthenticated(result.resultCode)
        })
    }
}