package za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.store_card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentAvailableFundBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.WBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds.AvailableFundsCommand
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds.AvailableFundsViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MyStoreCardFragment @Inject constructor() : ViewBindingFragment<FragmentAvailableFundBinding>() {

    private val viewModel by viewModels<AvailableFundsViewModel>()
    private val homeViewModel by viewModels<AccountProductsHomeViewModel>()

    @Inject
    lateinit var  bottomSheet: WBottomSheetBehaviour

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAvailableFundBinding {
        return FragmentAvailableFundBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObserver()
        setGuideline()
        setAccountInArrearsUI()
        background()
    }

    private fun background() {
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
}