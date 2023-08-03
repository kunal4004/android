package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.data

import android.content.Intent
import androidx.activity.result.ActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.General
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import javax.inject.Inject

interface GeneralNavigation {
    fun onGeneral(
        viewModel: UserAccountLandingViewModel,
        general: General,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?
    )
}

class GeneralNavigationImpl @Inject constructor(private val general: GeneralIntentImpl) :
    GeneralNavigation, GeneralIntent by general {

    override fun onGeneral(
        viewModel: UserAccountLandingViewModel,
        general: General,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?
    ) = when(general){
        General.ContactUs -> createContactUsIntent()
        General.NeedHelp -> createNeedHelpIntent(userAccountResponse = viewModel.getUserAccountResponse())
        General.Preferences -> viewModel.createMyPreferenceIntent(viewModel.isNowWfsUser(), activityLauncher)
        General.SignOut ->  createSignOutIntent()
        General.StoreLocator -> createStoreLocatorIntent()
        General.UpdatePassword -> createUpdatePasswordIntent()
    }
}