package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductLandingMainFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.toolbar.AccountProductsToolbarHelper
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOfferingState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.StoreCardAccountOptionsViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.setupGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel

@AndroidEntryPoint
class AccountProductsMainFragment : Fragment(R.layout.account_product_landing_main_fragment) {

    private var childNavController: NavController? = null
    val viewModel by viewModels<AccountProductsHomeViewModel>()
    private val accountOptionsViewModel: StoreCardAccountOptionsViewModel by activityViewModels()

    lateinit var mToolbarContainer: AccountProductsToolbarHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = AccountProductLandingMainFragmentBinding.bind(view)
        activity?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
        mToolbarContainer = AccountProductsToolbarHelper(binding, this@AccountProductsMainFragment)
        setupLandingScreen()
    }

    private fun setupLandingScreen() {
        val navHostFragment = getChildNavHost()
        childNavController = navHostFragment?.navController

        with(viewModel) {

            val startDestinationId = getStartDestinationIdScreen()
            setupGraph(
                graphResId = R.navigation.nav_account_product_landing,
                startDestination = startDestinationId,
                containerId = R.id.productNavigationContainerView,
                startDestinationArgs = arguments
            )

            getPopupDialogStatus { state ->
                when (state) {
                    /* when productOfferingGoodStanding == true
                   hideAccountInArrears(account)
                   showAccountHelp(getCardProductInformation(false))
                    */
                    is AccountOfferingState.AccountInGoodStanding -> Unit

                    is AccountOfferingState.AccountIsInArrears -> {
                        // showAccountInArrears(account)
                    }

                    is AccountOfferingState.AccountIsChargedOff -> {
                        if (!isCreditCard(product)) {
                            displayPopUp(DialogData.ChargedOff())
                        }
                    }

                    is AccountOfferingState.ShowViewTreatmentPlanPopupFromConfigForChargedOff -> {
                        when (isCreditCard(product)) {
                            false -> displayPopUp(DialogData.ViewPlanDialog())
                            true -> displayPopUp(
                                DialogData.ChargedOff(
                                    firstButtonTitle = R.string.view_your_payment_plan,
                                    secondButtonVisibility = GONE
                                )
                            )
                        }

                    }

                    is AccountOfferingState.ShowViewTreatmentPlanPopupInArrearsFromConfig -> {
                        displayPopUp(DialogData.ViewPlanDialog())

                    }

                    is AccountOfferingState.MakeGetEligibilityCall -> {
                        callEligibility()
                    }
                }
            }
        }
    }

    private fun callEligibility() {
        lifecycleScope.launchWhenStarted {
            viewModel.eligibilityPlanResponse().collect { response ->
                when (response) {
                    is ViewState.RenderSuccess -> {
                        val eligibilityPlanResponse = response.output as? EligibilityPlanResponse
                        eligibilityPlanResponse?.eligibilityPlan?.let {
                            accountOptionsViewModel.eligibilityPlanState.tryEmit(it)
                            displayPopUp(viewModel.getPopUpData(it), it)
                        }
                    }
                    is ViewState.RenderFailure -> {
                        displayPopUp(DialogData.AccountInArrDialog())
                    }
                    is ViewState.Loading -> {
                    }
                    ViewState.RenderEmpty -> {
                    }
                    else -> Unit
                }
            }
        }
    }


    private fun displayPopUp(dialogData: DialogData, eligibilityPlan: EligibilityPlan? = null) {
        viewModel.apply {
            findNavController().navigate(
                AccountProductsMainFragmentDirections.actionAccountProductsMainFragmentToAccountLandingDialogFragment(
                    product,
                    dialogData,
                    eligibilityPlan
                )
            )
        }
    }


    fun getChildNavHost(): NavHostFragment? =
        childFragmentManager.findFragmentById(R.id.productNavigationContainerView) as? NavHostFragment

}