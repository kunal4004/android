package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.StoreCardAccountOptionsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOptionsScreenUI
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants

import za.co.woolworths.financial.services.android.util.KotlinUtils

@AndroidEntryPoint
class StoreCardAccountOptionsFragment : ViewBindingFragment<StoreCardAccountOptionsFragmentBinding>(StoreCardAccountOptionsFragmentBinding::inflate) {

    val viewModel by viewModels<StoreCardAccountOptionsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        with(binding) {
            viewModel.viewState.observe(viewLifecycleOwner) { items ->
                items?.forEach { item ->
                    when (item) {
                        is AccountOptionsScreenUI.ViewTreatmentPlan -> {}
                        is AccountOptionsScreenUI.SetUpAPaymentPlan -> {}
                        is AccountOptionsScreenUI.PaymentOptionsScreenUI -> {}
                        is AccountOptionsScreenUI.BalanceProtectionInsurance -> showBalanceProtectionInsuranceTag(item)
                        is AccountOptionsScreenUI.WithdrawCashNow -> hideLoanWithdrawal()
                        is AccountOptionsScreenUI.DebitOrder -> showDebitOrder(item.isActive)
                    }
                }
            }
        }
    }

    private fun StoreCardAccountOptionsFragmentBinding.hideLoanWithdrawal() {
        withdrawCashView.visibility = GONE
    }

    private fun StoreCardAccountOptionsFragmentBinding.showBalanceProtectionInsuranceTag(
        bpi: AccountOptionsScreenUI.BalanceProtectionInsurance
    ) {
        when (bpi.status) {
            BpiInsuranceApplicationStatusType.INSURANCE_COVERED -> {
                balanceProtectionInsuranceTag.bpiTagTextView.visibility = VISIBLE
                balanceProtectionInsuranceTag.bpiNotCoveredTextView.visibility = GONE
                balanceProtectionInsuranceTag.balanceProtectionInsuranceArrowImageView.visibility =
                    GONE
                balanceProtectionInsuranceTag.bpiTagTextView.text = getString(R.string.bpi_covered)
                KotlinUtils.roundCornerDrawable(
                    balanceProtectionInsuranceTag.bpiTagTextView,
                    Constants.GreenColorCode
                )
            }
            BpiInsuranceApplicationStatusType.NOT_COVERED -> {
                balanceProtectionInsuranceTag.bpiTagTextView.visibility = GONE
                balanceProtectionInsuranceTag.bpiNotCoveredTextView.visibility = VISIBLE
                balanceProtectionInsuranceTag.balanceProtectionInsuranceArrowImageView.visibility =
                    VISIBLE
            }
            else -> {
                val data = bpi.leadGen
                val displayLabel = data?.displayLabel
                balanceProtectionInsuranceTag.bpiTagTextView.visibility = VISIBLE
                balanceProtectionInsuranceTag.bpiNotCoveredTextView.visibility = GONE
                balanceProtectionInsuranceTag.balanceProtectionInsuranceArrowImageView.visibility =
                    GONE
                balanceProtectionInsuranceTag.bpiTagTextView.text = displayLabel
                KotlinUtils.roundCornerDrawable(
                    balanceProtectionInsuranceTag.bpiTagTextView,
                    data?.displayLabelColor
                )
            }
        }
    }

    private fun StoreCardAccountOptionsFragmentBinding.showDebitOrder(isActive: Boolean) {
        if (isActive) {
            KotlinUtils.roundCornerDrawable(debitOrderIsActiveTextView, Constants.GreenColorCode)
            debitOrderView.visibility = VISIBLE
        } else {
            debitOrderView.visibility = GONE
        }
    }
}