package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.navigation

import android.content.Intent
import androidx.activity.result.ActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_device_security.data.DeviceSecurityImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.navigation.PetInsuranceNavigation
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.navigation.PetInsuranceNavigationImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_view_application_status.ViewApplicationStatusImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface ProductNavigation {
    fun UserAccountLandingViewModel.accountCardsAction(
        productGroup: AccountProductCardsGroup,
        activityResultLauncher: BetterActivityResult<Intent, ActivityResult>? = null,
        deepLinkParams : String? = null
    )

}

class ProductNavigationImpl @Inject constructor(private val viewApplicationStatus: ViewApplicationStatusImpl,
                                                private val productIntent: ProductIntentImpl,
                                                private val deviceSecurity: DeviceSecurityImpl,
                                                private val petNavigation : PetInsuranceNavigationImpl
) : ProductNavigation,
    PetInsuranceNavigation by petNavigation {

    override fun UserAccountLandingViewModel.accountCardsAction(
        productGroup: AccountProductCardsGroup,
        activityResultLauncher: BetterActivityResult<Intent, ActivityResult>?,
        deepLinkParams: String?) {

        if (productGroup.retryOptions?.isRetryInProgress == true) {
            fetchUserAccountByProductOfferingId(accountProductCardsGroup = productGroup)
            return
        }

        productGroup.applyNowState?.let { applyNowState ->
            with(deviceSecurity) {
                if (isLinkDeviceScreenNavigationNeeded()) {
                    createDeviceSecurityActivityResult(
                        viewModel = this@accountCardsAction,
                        activityLauncher = activityResultLauncher,
                        deepLinkParams = deepLinkParams,
                        applyNowState = applyNowState
                    )
                    return
                }
            }
        }
        val response : String? = getUserAccountResponse()?.let {response -> Utils.objectToJson(response)}
        with(productIntent) {
            when (productGroup) {
                is AccountProductCardsGroup.StoreCard -> createStoreCardIntent(deepLinkParams = deepLinkParams, productGroup = productGroup)
                is AccountProductCardsGroup.PersonalLoan -> createPersonalLoanIntent(deepLinkParams = deepLinkParams, userAccountResponse = response)
                is AccountProductCardsGroup.BlackCreditCard -> createBlackCreditCardIntent(deepLinkParams = deepLinkParams, userAccountResponse = response)
                is AccountProductCardsGroup.GoldCreditCard -> createBlackCreditCardIntent(deepLinkParams = deepLinkParams, userAccountResponse = response)
                is AccountProductCardsGroup.SilverCreditCard -> createBlackCreditCardIntent(deepLinkParams = deepLinkParams, userAccountResponse = response)
                is AccountProductCardsGroup.ApplicationStatus -> createViewApplicationStatusIntent(viewApplicationStatus)
                is AccountProductCardsGroup.PetInsurance -> petNavigation.navigateToPetInsurance(activityLauncher = activityResultLauncher, viewModel = this@accountCardsAction, productGroup = productGroup)
                is AccountProductCardsGroup.LinkYourWooliesCard -> createLinkYourWooliesCardIntent(activityLauncher = activityResultLauncher, viewModel = this@accountCardsAction)
            }
        }
    }
}