package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentAvailableFundBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.WBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.InformationData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.SystemBarCompat
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.feature_pay_my_account.PaymentsPayuMethodViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.AccountProductsMainFragmentDirections
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

@AndroidEntryPoint
class MyStoreCardFragment @Inject constructor() :
    ViewBindingFragment<FragmentAvailableFundBinding>(FragmentAvailableFundBinding::inflate),
    View.OnClickListener {

    val viewModel : AvailableFundsViewModel by activityViewModels()
    val homeViewModel : AccountProductsHomeViewModel by activityViewModels()
    val pmaViewModel: PaymentsPayuMethodViewModel by activityViewModels()

    @Inject
    lateinit var bottomSheet: WBottomSheetBehaviour

    @Inject
    lateinit var statusBarCompat: SystemBarCompat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.availableFunds.setUpView()
        statusBarCompat.setLightStatusAndNavigationBar()
        KotlinUtils.setTransparentStatusBar(requireActivity() as? StoreCardActivity)
        setupToolbar()
        subscribeObserver()
        setGuideline()
        setAccountInArrearsUI()
        setBackground()
        clickListeners()
    }

    private fun showProgress(isLoading: Boolean) {
        binding.incPayMyAccountButton.viewPaymentOptionTextShimmerLayout.loadingState(isLoading)
        binding.incPayMyAccountButton.viewPaymentOptionImageShimmerLayout.loadingState(isLoading)
    }

    private fun setupToolbar() {
        (activity as? StoreCardActivity)?.apply {
            getToolbarHelper()?.setHomeLandingToolbar(homeViewModel) { view ->
                when (view.id) {
                    R.id.infoIconImageView -> navigateToInformation()
                    R.id.navigateBackImageButton -> activity?.finish()
                }
            }
        }
    }

    private fun clickListeners() {
        binding.incViewStatementButton.root.setOnClickListener(this)
        binding.incRecentTransactionButton.root.setOnClickListener(this)
        binding.incPayMyAccountButton.root.setOnClickListener(this)
    }

    private fun setBackground() {
        binding.availableFundBackground.setBackgroundResource(R.drawable.store_card_background)
    }

    private fun setAccountInArrearsUI() {
        binding.paymentOverdueGroup.visibility = homeViewModel.isUiVisible()
    }

    private fun subscribeObserver() {
        with(viewModel) {
            command.observe(viewLifecycleOwner) { item ->
                when (item) {
                    is AvailableFundsCommand.SetViewDetails -> binding.setBalances(item)
                    else -> return@observe
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            pmaViewModel.paymentMethodsResponseResult.collect { result ->
                with(result) {
                    renderSuccess { showLandingScreen(output) }

                    renderLoading { showProgress(isLoading) }

                    renderFailure {}

                    renderNoConnection {}

                    renderHttpFailureFromServer { showLandingScreen(output) }

                    renderEmpty {}

                }
            }
        }

        pmaViewModel.requestPaymentPayuMethod()

    }

    private fun showLandingScreen(response : PaymentMethodsResponse?) {
        when (pmaViewModel.getLandingScreen(response)) {
            PayMyAccountViewModel.PAYUMethodType.CREATE_USER -> {}
            PayMyAccountViewModel.PAYUMethodType.CARD_UPDATE -> {}
            PayMyAccountViewModel.PAYUMethodType.ERROR -> {}
        }
    }

    /**
     * Set dynamic guideline when overdue amount, payable now is visible
     */
    private fun setGuideline() {
        with(binding) {
            bottomStartGuide.setGuidelinePercent(bottomSheet.buttonsTopGuideline)
            bottomSliderGuideline.setGuidelinePercent(bottomSheet.buttonsBottomGuideline)
        }
    }

    private fun navigateToInformation() {
        with(homeViewModel) {
            findNavController().navigate(
                AccountProductsMainFragmentDirections.actionAccountProductsMainFragmentToAccountInformationFragment(
                    if (isProductInGoodStanding()) InformationData.GoodStanding() else InformationData.Arrears()
                )
            )
        }
    }

    private fun FragmentAvailableFundBinding.setBalances(setViewDetails: AvailableFundsCommand.SetViewDetails) {
        with(setViewDetails) {
            availableFundAmountTextView.text = availableFund
            currentBalanceAmountTextView.text = currentBalance
            creditLimitAmountTextView.text = creditLimit
            totalAmountDueAmountTextView.text = totalAmountDueAmount
            nextPaymentDueDateTextView.text = paymentDueDate
            amountPayableNowAmountTextView.text = amountOverdue
        }
    }

    override fun onClick(view: View?) {
        with(homeViewModel) {
            when (view) {
                binding.incViewStatementButton.root -> {
                    navigateToStatementActivity(activity, product)
                }
                binding.incRecentTransactionButton.root -> {
                    product?.let { navigateToRecentTransactionActivity(activity, product,cardType =  it.productGroupCode) }
                }

                else -> Unit
            }
        }
    }
}

