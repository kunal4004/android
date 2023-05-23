package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.data

import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.General
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import javax.inject.Inject

interface GeneralNavigation {
    fun onGeneral(viewModel: UserAccountLandingViewModel, general: General)
}

class GeneralNavigationImpl @Inject constructor(private val general: GeneralIntentImpl) :
    GeneralNavigation, GeneralIntent by general {

    override fun onGeneral(viewModel: UserAccountLandingViewModel ,general: General) = when(general){
        General.ContactUs -> createContactUsIntent()
        General.NeedHelp -> createNeedHelpIntent()
        General.Preferences -> createMyPreferenceIntent(viewModel.isNowWfsUser())
        General.SignOut ->  createSignOutIntent()
        General.StoreLocator -> createStoreLocatorIntent()
        General.UpdatePassword -> createUpdatePasswordIntent()
    }
}