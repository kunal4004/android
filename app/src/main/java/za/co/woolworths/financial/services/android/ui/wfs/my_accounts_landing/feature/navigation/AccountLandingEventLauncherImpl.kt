package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.google.gson.JsonObject
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AccountLandingFirebaseManager
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AccountLandingFirebaseManagerImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.data.GeneralNavigation
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_general.data.GeneralNavigationImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_launcher.InstantLauncher
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_launcher.InstantLauncherImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_logout.ManageLoginRegisterNavigation
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_logout.ManageLoginRegisterNavigationImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.navigation.OfferNavigation
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.navigation.OfferNavigationImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.navigation.PetInsuranceNavigation
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.navigation.PetInsuranceNavigationImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.AccountLandingInstantLauncher
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.General
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.ManageLoginRegister
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.MyProfile
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OfferClickEvent
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OnAccountItemClickListener
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.navigation.ProductNavigation
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.navigation.ProductNavigationImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_profile.ProfileNavigation
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_profile.ProfileNavigationImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.data.navigation.CreditCardDeliveryNavigation
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.data.navigation.CreditCardDeliveryNavigationImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface AccountLandingEventLauncher {
    fun hideToolbar()
    fun onItemSelectedListener(
        event: OnAccountItemClickListener,
        deepLinkParams: String? = null,
        viewModel: UserAccountLandingViewModel,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?
    )

    fun onProductClicked(
        productGroup: AccountProductCardsGroup,
        viewModel: UserAccountLandingViewModel,
        deepLinkParams: String? = null,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?
    )

    fun navigateToDeepLinkData(
        deepLinkParams: JsonObject?,
        viewModel: UserAccountLandingViewModel,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?
    )
}

class AccountLandingEventLauncherImpl @Inject constructor(
    private val activity: Activity,
    private val productNavigation: ProductNavigationImpl,
    private val offer: OfferNavigationImpl,
    private val profile: ProfileNavigationImpl,
    private val general: GeneralNavigationImpl,
    private val loggedOut: ManageLoginRegisterNavigationImpl,
    private val analytics: AccountLandingFirebaseManagerImpl,
    private val creditCardDelivery: CreditCardDeliveryNavigationImpl,
    private val pet : PetInsuranceNavigationImpl,
    private val instantLauncher : InstantLauncherImpl
) : AccountLandingEventLauncher,
    ManageLoginRegisterNavigation by loggedOut,
    GeneralNavigation by general,
    ProfileNavigation by profile,
    OfferNavigation by offer,
    ProductNavigation by productNavigation,
    AccountLandingFirebaseManager by analytics,
    CreditCardDeliveryNavigation by creditCardDelivery,
    PetInsuranceNavigation by pet,
    InstantLauncher by instantLauncher {

    override fun hideToolbar() {
        if (activity is BottomNavigationActivity) {
            activity.hideToolbar()
        }
    }

    override fun onItemSelectedListener(
        event: OnAccountItemClickListener,
        deepLinkParams: String?,
        viewModel: UserAccountLandingViewModel,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?) {
        viewModel.disableBiometricBlur()
        with(viewModel) {
            when (event) {
                is OfferClickEvent -> onOffer(this, activityLauncher, event)
                is MyProfile -> onMyProfile(event, this, activityLauncher)
                is General -> onGeneral(this, event, activityLauncher)
                is ManageLoginRegister -> onManageLoginRegister(event, activityLauncher)
                is AccountLandingInstantLauncher -> onLaunch(activity  = activity, event = event,activityLauncher = activityLauncher)
                else -> Unit
            }
        }
    }

    override fun onProductClicked(
        productGroup: AccountProductCardsGroup,
        viewModel: UserAccountLandingViewModel,
        deepLinkParams: String?,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?) {
        with(viewModel) {
            disableBiometricBlur()
            when (productGroup) {
                is AccountProductCardsGroup.ApplicationStatus -> accountCardsAction(
                    productGroup = productGroup,
                    activityResultLauncher = activityLauncher,
                    deepLinkParams = deepLinkParams
                )

                is AccountProductCardsGroup.BlackCreditCard -> accountCardsAction(
                    productGroup = productGroup,
                    activityResultLauncher = activityLauncher,
                    deepLinkParams = deepLinkParams
                )

                is AccountProductCardsGroup.GoldCreditCard -> accountCardsAction(
                    productGroup = productGroup,
                    activityResultLauncher = activityLauncher,
                    deepLinkParams = deepLinkParams
                )

                is AccountProductCardsGroup.SilverCreditCard -> accountCardsAction(
                    productGroup = productGroup,
                    activityResultLauncher = activityLauncher,
                    deepLinkParams = deepLinkParams
                )

                is AccountProductCardsGroup.PersonalLoan -> accountCardsAction(
                    productGroup = productGroup,
                    activityResultLauncher = activityLauncher,
                    deepLinkParams = deepLinkParams
                )

                is AccountProductCardsGroup.PetInsurance -> accountCardsAction(
                    productGroup = productGroup,
                    activityResultLauncher = activityLauncher,
                    deepLinkParams = deepLinkParams
                )

                is AccountProductCardsGroup.StoreCard -> accountCardsAction(
                    productGroup = productGroup,
                    activityResultLauncher = activityLauncher,
                    deepLinkParams = deepLinkParams
                )

                is AccountProductCardsGroup.LinkYourWooliesCard ->  accountCardsAction(
                    productGroup = productGroup,
                    activityResultLauncher = activityLauncher,
                    deepLinkParams = deepLinkParams
                )
            }
        }
    }

    override fun navigateToDeepLinkData(
        deepLinkParams: JsonObject?,
        viewModel: UserAccountLandingViewModel,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?) {
        val productGroupCode = deepLinkParams?.get("productGroupCode")?.asString?.uppercase()
        productGroupCode ?: return
        val productGroup = viewModel.getProductByProductGroupCode(productGroupCode)
        val deepLinkParam  : String? = Utils.objectToJson(deepLinkParams)

        productGroup?.let { group -> onProductClicked(productGroup = group,viewModel = viewModel, activityLauncher = activityLauncher, deepLinkParams = deepLinkParam) }

        viewModel.resetDeepLinkParams()

    }

}