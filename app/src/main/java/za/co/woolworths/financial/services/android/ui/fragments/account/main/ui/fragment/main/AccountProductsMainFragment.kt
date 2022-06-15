package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import androidx.core.os.bundleOf
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
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.NavigationGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOfferingState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.InformationData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.StoreCardAccountOptionsViewModel

@AndroidEntryPoint
class AccountProductsMainFragment : Fragment(R.layout.account_product_landing_main_fragment) {

    private var childNavController: NavController? = null
    val viewModel by viewModels<AccountProductsHomeViewModel>()
    val optionsViewModel: StoreCardAccountOptionsViewModel by activityViewModels()

    private var navigationGraph: NavigationGraph = NavigationGraph()

    lateinit var mToolbarContainer : AccountProductsToolbarHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = AccountProductLandingMainFragmentBinding.bind(view)
        activity?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
        mToolbarContainer = AccountProductsToolbarHelper(binding,this@AccountProductsMainFragment)
        setupLandingScreen()
        setToolbar()
    }

    private fun setToolbar() {
        mToolbarContainer.setHomeLandingToolbar(viewModel) { view ->
            when(view.id){
                R.id.infoIconImageView -> navigateToInformation()
                R.id.navigateBackImageButton -> activity?.finish()
            }
        }
    }

    private fun setupLandingScreen() {
        val navHostFragment = childFragmentManager.findFragmentById(R.id.productNavigationView) as NavHostFragment
        childNavController = navHostFragment.navController

        with(viewModel) {
            val startDestinationId = getStartDestinationIdScreen()
            navigationGraph.setupNavigationGraph(
                childNavController,
                graphResId = R.navigation.nav_account_product_landing,
                startDestinationId = startDestinationId,
                bundleOf()
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
                        when (isCreditCard(product)) {
                            false -> displayPopUp(DialogData.ChargedOff())
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
                        eligibilityPlanResponse?.eligibilityPlan.let {
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
                }
            }
        }
    }


    fun displayPopUp(dialogData: DialogData, eligibilityPlan: EligibilityPlan? = null) {
        viewModel.apply {
            findNavController().navigate(AccountProductsMainFragmentDirections.actionAccountProductsMainFragmentToAccountLandingDialogFragment(product, dialogData, eligibilityPlan)) }
    }

    private fun navigateToInformation() {
        viewModel.apply {
            findNavController().navigate(
                AccountProductsMainFragmentDirections.actionAccountProductsMainFragmentToAccountInformationFragment(
                    if (isProductInGoodStanding()) InformationData.GoodStanding() else InformationData.Arrears()
                )
            )
        }
    }


    fun getChildNavHost(): NavHostFragment? {
        return childFragmentManager.findFragmentById(R.id.productNavigationView) as? NavHostFragment

    }
}