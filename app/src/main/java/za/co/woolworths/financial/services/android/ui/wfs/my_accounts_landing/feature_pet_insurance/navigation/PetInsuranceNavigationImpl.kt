package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import za.co.woolworths.financial.services.android.models.dto.account.CoveredStatus
import za.co.woolworths.financial.services.android.ui.activities.webview.activities.WInternalWebPageActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants
import za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance.apply.PetInsuranceApplyNowActivity
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AccountLandingFirebaseManagerImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.PetInsurance
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.PetInsuranceImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.PetInsuranceUrlIsWebviewExitUrl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.AccountProductCardsGroup
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel.UserAccountLandingViewModel
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

interface PetInsuranceNavigation {
    fun showDialogFragmentWhenStatusPending()
    fun navigateToPetInsuranceAwarenessModel()
    fun navigateToPetInsuranceApplyNow(viewModel: UserAccountLandingViewModel, activityLauncher: BetterActivityResult<Intent, ActivityResult>?)
    fun navigateToPetInsurance(viewModel : UserAccountLandingViewModel, activityLauncher: BetterActivityResult<Intent, ActivityResult>?, productGroup: AccountProductCardsGroup.PetInsurance)
    fun breakoutToWebUrl(params : PetInsuranceUrlIsWebviewExitUrl)
    fun UserAccountLandingViewModel.registerPetInsuranceForActivityResult(activityLauncher: BetterActivityResult<Intent, ActivityResult>?)
}

class PetInsuranceNavigationImpl @Inject constructor(private val activity: Activity?,
                                                     private val petInsurance : PetInsuranceImpl,
                                                     private val analytics : AccountLandingFirebaseManagerImpl
) : PetInsuranceNavigation,
    PetInsurance by petInsurance {

    override fun showDialogFragmentWhenStatusPending() {
        (activity as? AppCompatActivity)?.let { KotlinUtils.showPetInsurancePendingDialog(it.supportFragmentManager) }
    }

    override fun navigateToPetInsuranceAwarenessModel() {
        activity?.let {
            analytics.petInsuranceLearnMore()
            it.startActivity(Intent(it, PetInsuranceApplyNowActivity::class.java)) }
    }

    override fun navigateToPetInsuranceApplyNow(viewModel: UserAccountLandingViewModel,activityLauncher: BetterActivityResult<Intent, ActivityResult>?) {
        viewModel.registerPetInsuranceForActivityResult(activityLauncher)
    }

    override fun navigateToPetInsurance(
        viewModel: UserAccountLandingViewModel,
        activityLauncher: BetterActivityResult<Intent, ActivityResult>?,
        productGroup: AccountProductCardsGroup.PetInsurance
    ) {
        analytics.petInsuranceGetInsuranceProduct()
        when (productGroup.insuranceProducts?.statusType()) {
            CoveredStatus.NOT_COVERED,
            CoveredStatus.PENDING,
            CoveredStatus.COVERED -> viewModel.registerPetInsuranceForActivityResult(
                activityLauncher
            )

            null -> Unit
        }
    }

    override fun breakoutToWebUrl(params: PetInsuranceUrlIsWebviewExitUrl) {
        val petInsuranceUrl = params.first
        val isRenderModeWebView = params.second
        val exitUrl = params.third

        KotlinUtils.petInsuranceRedirect(
            activity, petInsuranceUrl,
            isRenderModeWebView, exitUrl
        )
    }

    override fun UserAccountLandingViewModel.registerPetInsuranceForActivityResult(activityLauncher: BetterActivityResult<Intent, ActivityResult>?){
        activity ?: return

        val config = getPetInsuranceConfigFromMobileConfig()
        val petInsuranceUrl = config?.petInsuranceUrl
        val intent =  Intent(activity, WInternalWebPageActivity::class.java).apply {
            putExtra(Constants.IS_PET_INSURANCE, true)
        }

        when (isRenderModeWebView(config)) {
            true -> {
                intent.putExtra(KotlinUtils.COLLECTIONS_EXIT_URL, "")
                activityLauncher?.launch(intent) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        queryAccountLandingService()
                    }
                }
            }
            false -> KotlinUtils.openUrlInPhoneBrowser(petInsuranceUrl, activity)
        }
    }

}