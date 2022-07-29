package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.remove_dc_block

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.RemoveBlockDcMainFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.ui.extension.navigateSafelyWithNavController
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.InformationData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.DisplayInArrearsPopup
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds.AvailableFundsCommand
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds.AvailableFundsViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.AccountProductsMainFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState

@AndroidEntryPoint
class RemoveBlockOnCollectionFragment : Fragment(R.layout.remove_block_dc_main_fragment) {

    val availableFundViewModel : AvailableFundsViewModel by activityViewModels()
    val homeViewModel : AccountProductsHomeViewModel by activityViewModels()

    private lateinit var mDisplayInArrearsPopup: DisplayInArrearsPopup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = RemoveBlockDcMainFragmentBinding.bind(view)
        setupToolbar()
        mDisplayInArrearsPopup = DisplayInArrearsPopup(fragment = this@RemoveBlockOnCollectionFragment, homeViewModel = homeViewModel){ item ->
            navigateSafelyWithNavController(RemoveBlockOnCollectionFragmentDirections.actionRemoveBlockOnCollectionFragmentToAccountLandingDialogFragment(homeViewModel.product,item.first, item.second))
            binding.setHelpWithPaymentViewLabel(item.second)
            binding.setHelpWithPaymentViewVisibility(item.second)
        }
        binding.setupView()
        binding.subscribeObservers()
        binding.setListeners()
    }

    private fun RemoveBlockDcMainFragmentBinding.setHelpWithPaymentViewVisibility(eligibilityPlan: EligibilityPlan?) {
        helpWithPayment.visibility = if(homeViewModel.mViewTreatmentPlanImpl?.isViewElitePlanEnabled(eligibilityPlan)==true) VISIBLE else GONE
    }

    private fun RemoveBlockDcMainFragmentBinding.setupView() {
        incPayMyAccountButton.viewPaymentOptionTextShimmerLayout.loadingState(
            false)

        incPayMyAccountButton.viewPaymentOptionImageShimmerLayout.loadingState(
            false)

        with(availableFundViewModel){
            val callCenterContact = getCallCenterContact("0861502020")
            setUnderlineText(callCenterContact, contactCallCenterNowTextview)
        }
    }

    private fun RemoveBlockDcMainFragmentBinding.subscribeObservers() {
        availableFundViewModel.availableFunds.setUpView()
        availableFundViewModel.command.observe(viewLifecycleOwner){item ->
            when (item) {
                is AvailableFundsCommand.SetViewDetails -> setBalances(item)
                else -> return@observe
            }
        }

        with(mDisplayInArrearsPopup) {
            collectCheckEligibilityResult()
            setupInArrearsPopup()
        }
    }

    private fun setupToolbar() {
        (activity as? StoreCardActivity)?.getToolbarHelper()?.apply {
            setHomeLandingToolbar(homeViewModel) { view ->
                when (view.id) {
                    R.id.infoIconImageView -> navigateToInformation()
                    R.id.navigateBackImageButton -> activity?.finish()
                }
            }

            setOnAccountInArrearsTapListener {
                mDisplayInArrearsPopup.setupInArrearsPopup()
            }
        }
    }

    private fun navigateToInformation() {
        findNavController().navigate(
            AccountProductsMainFragmentDirections.actionAccountProductsMainFragmentToAccountInformationFragment(
                InformationData.Arrears()
            )
        )
    }

    private fun RemoveBlockDcMainFragmentBinding.setBalances(data: AvailableFundsCommand.SetViewDetails) {
        currentBalanceAmountTextview.text = data.currentBalance
        totalAmountDueAmountTextview.text = data.totalAmountDueAmount
    }

    private fun RemoveBlockDcMainFragmentBinding.setListeners() {
        with(homeViewModel) {
            incRecentTransactionButton.root.onClick {
                product?.let {
                    navigateToRecentTransactionActivity(activity = requireActivity(), product = product,
                        cardType = it.productGroupCode
                    )
                }
            }

            incViewStatementButton.root.onClick {
                navigateToStatementActivity(requireActivity(), product)
            }

            helpWithPayment.onClick { mDisplayInArrearsPopup.onTap(requireActivity()) }

        }
    }

    private fun RemoveBlockDcMainFragmentBinding.setHelpWithPaymentViewLabel(eligibilityPlan: EligibilityPlan?) {
        helpWithPayment.text = when(homeViewModel.mViewTreatmentPlanImpl?.isViewElitePlanEnabled(eligibilityPlan)==true){
            true ->  getString(R.string.view_your_payment_plan)
            false -> getString(R.string.get_help_repayment)
        }

        helpWithPayment.visibility = if (homeViewModel.mViewTreatmentPlanImpl?.isElitePlanEnabled(eligibilityPlan)==true) VISIBLE else GONE
    }
}


