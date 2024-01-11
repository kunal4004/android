package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_profile.navigation

import android.content.Intent
import androidx.activity.result.ActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.MyProfile
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_profile.ProfileIntent
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import javax.inject.Inject

interface ProfileNavigation {
    fun onMyProfile(clicked: MyProfile, viewModel: UserAccountLandingViewModel, activityLauncher: BetterActivityResult<Intent, ActivityResult>?)
}

class ProfileNavigationImpl @Inject constructor(private val profile: ProfileIntent) :
    ProfileNavigation {

    override fun onMyProfile(clicked: MyProfile, viewModel: UserAccountLandingViewModel, activityLauncher: BetterActivityResult<Intent, ActivityResult>?) = when (clicked) {
        MyProfile.Detail -> profile.createDetailIntent()
        MyProfile.Message -> profile.createMessageIntent(activityLauncher, viewModel)
        MyProfile.Order -> profile.createOrderIntent()
        MyProfile.OrderAgain -> profile.createOrderAgainIntent()
        MyProfile.ShoppingList -> profile.createShoppingListIntent()
    }

}