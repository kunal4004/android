package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsListFragmentBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOptionsScreenUI
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.StoreCardAccountOptionsViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants
import za.co.woolworths.financial.services.android.util.KotlinUtils

class AccountOptionsListFragment : ViewBindingFragment<AccountOptionsListFragmentBinding>(AccountOptionsListFragmentBinding::inflate) {

    val viewModel: StoreCardAccountOptionsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
    }

    private fun subscribeObservers() {
        with(binding) {
            lifecycleScope.launch {
                viewModel.viewState.collect { items ->
                    items.forEach { item ->
                        with(item) {
                            when (this) {
                                is AccountOptionsScreenUI.ViewTreatmentPlan -> showViewYourPaymentPlan(
                                    isVisible
                                )
                                is AccountOptionsScreenUI.SetUpAPaymentPlan -> showSetupPaymentPlan(
                                    isVisible
                                )
                                is AccountOptionsScreenUI.PaymentOptionsScreenUI -> Unit
                                is AccountOptionsScreenUI.BalanceProtectionInsurance -> showBalanceProtectionInsuranceTag(
                                    this
                                )
                                is AccountOptionsScreenUI.WithdrawCashNow -> hideLoanWithdrawal()
                                is AccountOptionsScreenUI.DebitOrder -> showDebitOrder(isActive)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun AccountOptionsListFragmentBinding.hideLoanWithdrawal() {
        loanWithdrawalDivider.visibility = View.GONE
        withdrawCashView.visibility = View.GONE
    }

    private fun AccountOptionsListFragmentBinding.showBalanceProtectionInsuranceTag(
        bpi: AccountOptionsScreenUI.BalanceProtectionInsurance
    ) {
        when (bpi.status) {
            BpiInsuranceApplicationStatusType.INSURANCE_COVERED -> {
                balanceProtectionInsuranceTag.bpiTagTextView.visibility = View.VISIBLE
                balanceProtectionInsuranceTag.bpiNotCoveredTextView.visibility = View.GONE
                balanceProtectionInsuranceTag.balanceProtectionInsuranceArrowImageView.visibility =
                    View.GONE
                balanceProtectionInsuranceTag.bpiTagTextView.text = getString(R.string.bpi_covered)
                KotlinUtils.roundCornerDrawable(
                    balanceProtectionInsuranceTag.bpiTagTextView,
                    Constants.GreenColorCode
                )
            }
            BpiInsuranceApplicationStatusType.NOT_COVERED -> {
                balanceProtectionInsuranceTag.bpiTagTextView.visibility = View.GONE
                balanceProtectionInsuranceTag.bpiNotCoveredTextView.visibility = View.VISIBLE
                balanceProtectionInsuranceTag.balanceProtectionInsuranceArrowImageView.visibility =
                    View.VISIBLE
            }
            else -> {
                val data = bpi.leadGen
                val displayLabel = data?.displayLabel
                balanceProtectionInsuranceTag.bpiTagTextView.visibility = View.VISIBLE
                balanceProtectionInsuranceTag.bpiNotCoveredTextView.visibility = View.GONE
                balanceProtectionInsuranceTag.balanceProtectionInsuranceArrowImageView.visibility =
                    View.GONE
                balanceProtectionInsuranceTag.bpiTagTextView.text = displayLabel
                KotlinUtils.roundCornerDrawable(
                    balanceProtectionInsuranceTag.bpiTagTextView,
                    data?.displayLabelColor
                )
            }
        }
    }

    private fun AccountOptionsListFragmentBinding.showDebitOrder(isActive: Boolean) {
        if (isActive) {
            KotlinUtils.roundCornerDrawable(debitOrderIsActiveTextView, Constants.GreenColorCode)
            debitOrderDivider.visibility = View.VISIBLE
            debitOrderView.visibility = View.VISIBLE
        } else {
            debitOrderDivider.visibility = View.GONE
            debitOrderView.visibility = View.GONE
        }
    }

    private fun AccountOptionsListFragmentBinding.showViewYourPaymentPlan(isVisible: Boolean) {
        viewTreatmentPlanLinearLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun AccountOptionsListFragmentBinding.showSetupPaymentPlan(isVisible: Boolean) {
        setupTreatmentPlanView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}