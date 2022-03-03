package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.databinding.AccountProductLandingMainFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.AccountOfferingState
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.AccountApiResult

@AndroidEntryPoint
class AccountProductsMainFragment :
    ViewBindingFragment<AccountProductLandingMainFragmentBinding>() {

    val viewModel: AccountProductsHomeViewModel by viewModels()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AccountProductLandingMainFragmentBinding {
        return AccountProductLandingMainFragmentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLandingScreen()
        setupObservers()
    }

    private fun setupLandingScreen() {
        with(viewModel) {
            landingScreen { state ->
                when (state) {
                    /* when productOfferingGoodStanding == true
                   hideAccountInArrears(account)
                   showAccountHelp(getCardProductInformation(false))
                    */
                    is AccountOfferingState.AccountInGoodStanding -> {
                        findNavController().navigate(AccountProductsMainFragmentDirections.actionAccountProductsMainFragmentToAccountProductsHomeFragment())
                    }
                    is AccountOfferingState.AccountIsInArrears -> {
                        // showAccountInArrears(account)
                    }

                    is AccountOfferingState.AccountIsChargedOff -> {
                        // account is in arrears for more than 6 months
                        // removeBlocksOnCollectionCustomer()
                    }

                    is AccountOfferingState.ShowViewTreatmentPlanPopupFromConfigForChargedOff -> {

                    }

                    is AccountOfferingState.ShowViewTreatmentPlanPopupInArrearsFromConfig -> {

                    }

                    is AccountOfferingState.MakeGetEligibilityCall -> {

                    }
                }
            }
        }
    }

    private fun setupObservers() {
        with(viewModel) {
            eligibilityPlanResponse.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is AccountApiResult.Success -> {}
                    is AccountApiResult.Error -> {}
                    is AccountApiResult.Loading -> {}
                }
            }
        }
    }
}