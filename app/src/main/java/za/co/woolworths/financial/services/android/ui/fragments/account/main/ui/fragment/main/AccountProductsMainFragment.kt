package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
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
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.NavigationGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOfferingState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.StoreCardAccountOptionsViewModel

import za.co.woolworths.financial.services.android.util.AppConstant.Companion.RED_HEX_COLOR
import za.co.woolworths.financial.services.android.util.KotlinUtils

@AndroidEntryPoint
class AccountProductsMainFragment : ViewBindingFragment<AccountProductLandingMainFragmentBinding>(
    AccountProductLandingMainFragmentBinding::inflate
) {

    private var childNavController: NavController? = null
    val viewModel by viewModels<AccountProductsHomeViewModel>()
    val optionsViewModel : StoreCardAccountOptionsViewModel by activityViewModels()

    private var navigationGraph: NavigationGraph = NavigationGraph()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
        setToolbar()
        setupLandingScreen()
    }

    private fun setToolbar() {
        with(binding) {
            when (viewModel.isProductInGoodStanding()) {
                true -> {
                    toolbarTitleTextView.visibility = VISIBLE
                    toolbarTitleTextView.text = getString(viewModel.getTitleId())
                    accountInArrearsTextView.visibility = GONE
                }
                false -> {
                    toolbarTitleTextView.visibility = GONE
                    KotlinUtils.roundCornerDrawable(accountInArrearsTextView, RED_HEX_COLOR)
                    accountInArrearsTextView.visibility = VISIBLE
                }
            }
        }
    }

    private fun setupLandingScreen() {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.productNavigationView) as NavHostFragment
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
                        when(isCreditCard(product)){
                            false -> displayPopUp(DialogData.ChargedOff())
                        }
                    }

                    is AccountOfferingState.ShowViewTreatmentPlanPopupFromConfigForChargedOff -> {
                        when(isCreditCard(product)){
                            false -> displayPopUp(DialogData.ViewPlanDialog())
                            true-> displayPopUp(DialogData.ChargedOff(
                                firstButtonTitle = R.string.view_your_payment_plan,
                                secondButtonVisibility = GONE
                            ))
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
                        response.output.eligibilityPlan.let {
                            displayPopUp(viewModel.getPopUpData(it), it)
                        }
                    }
                    is ViewState.RenderFailure -> {
                        displayPopUp(DialogData.AccountInArrDialog())
                    }
                    is ViewState.Loading -> {
                    }
                    ViewState.RenderEmpty -> {}
                }
            }
        }
    }


    fun displayPopUp(dialogData: DialogData, eligibilityPlan: EligibilityPlan? = null) {
        viewModel.apply {
            findNavController().navigate(
                AccountProductsMainFragmentDirections.actionAccountProductsMainFragmentToAccountLandingDialogFragment(
                    product,
                    dialogData, eligibilityPlan
                )
            )
        }

    }
}