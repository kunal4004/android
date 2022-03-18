package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductLandingMainFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bpi_covered_tag_layout.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.NavigationGraph
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.RED_HEX_COLOR
import za.co.woolworths.financial.services.android.util.KotlinUtils

@AndroidEntryPoint
class AccountProductsMainFragment : ViewBindingFragment<AccountProductLandingMainFragmentBinding>() {

    private var childNavController: NavController? = null
    val viewModel by viewModels<AccountProductsHomeViewModel>()
    var navigationGraph : NavigationGraph = NavigationGraph()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AccountProductLandingMainFragmentBinding {
        return AccountProductLandingMainFragmentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
        setToolbar()
        setupLandingScreen()
        setupObservers()
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

            val account = getAccountProduct()
            //findNavController().navigate(AccountProductsMainFragmentDirections.actionAccountProductsMainFragmentToAccountInArrearsLandingDialogFragment(account))

//            val popupId = getPopupDialogStatus{ state ->
//                when (state) {
//                    /* when productOfferingGoodStanding == true
//                   hideAccountInArrears(account)
//                   showAccountHelp(getCardProductInformation(false))
//                    */
//                    is AccountOfferingState.AccountInGoodStanding -> null
//
//                    is AccountOfferingState.AccountIsInArrears -> {
//                        // showAccountInArrears(account)
//                    }
//
//                    is AccountOfferingState.AccountIsChargedOff -> {
//
//                        // account is in arrears for more than 6 months
//                        // removeBlocksOnCollectionCustomer()
//                    }
//
//                    is AccountOfferingState.ShowViewTreatmentPlanPopupFromConfigForChargedOff -> {
//
//                    }
//
//                    is AccountOfferingState.ShowViewTreatmentPlanPopupInArrearsFromConfig -> {
//
//                    }
//
//                    is AccountOfferingState.MakeGetEligibilityCall -> {
//                        viewModel.queryServiceCheckCustomerEligibilityPlan()
//                    }
//                }
//            }
        }
    }

    private fun setupObservers() {
//        with(viewModel) {
//            eligibilityPlanResponse.observe(viewLifecycleOwner) { response ->
//                when (response) {
//                    is AccountApiResult.Success -> {}
//                    is AccountApiResult.Error -> {}
//                    is AccountApiResult.Loading -> {}
//                }
//            }
//        }
    }
}