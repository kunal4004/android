package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsListFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.ui.base.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOptionsScreenUI
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.StoreCardCallBack
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BetterActivityResult
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

@AndroidEntryPoint
class AccountOptionsListFragment : Fragment(R.layout.account_options_list_fragment) {

    @Inject
    lateinit var landingRouter: ProductLandingRouterImpl

    val viewModel: AccountProductsHomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(AccountOptionsListFragmentBinding.bind(view)) {
            subscribeObservers()
            setListeners()
        }
    }

    private fun AccountOptionsListFragmentBinding.subscribeObservers() {
        accountOptionsSkeleton.loadingState(false, targetedShimmerLayout = accountOptionsLayout)
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
            viewModel.init()
        }
    }

    private fun AccountOptionsListFragmentBinding.hideLoanWithdrawal() {
        loanWithdrawalDivider.visibility = GONE
        withdrawCashView.visibility = GONE
    }

    private fun AccountOptionsListFragmentBinding.showBalanceProtectionInsuranceTag(
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

    private fun AccountOptionsListFragmentBinding.showDebitOrder(isActive: Boolean) {
        if (isActive) {
            KotlinUtils.roundCornerDrawable(debitOrderIsActiveTextView, Constants.GreenColorCode)
            debitOrderDivider.visibility = VISIBLE
            debitOrderRelativeLayout.visibility = VISIBLE
        } else {
            debitOrderDivider.visibility = GONE
            debitOrderRelativeLayout.visibility = GONE
        }
    }

    private fun AccountOptionsListFragmentBinding.showViewYourPaymentPlan(isVisible: Boolean) {
        viewTreatmentPlanLinearLayout.visibility = if (isVisible) VISIBLE else GONE
    }

    private fun AccountOptionsListFragmentBinding.showSetupPaymentPlan(isVisible: Boolean) {
        setupTreatmentPlanView.visibility = if (isVisible) VISIBLE else GONE
    }

    private fun AccountOptionsListFragmentBinding.setListeners() {

        debitOrderRelativeLayout.onClick { landingRouter.routeToDebitOrderActivity(requireActivity()) }

        balanceProtectionInsuranceRelativeLayout.onClick {
            val bpi = viewModel.accountOptions.bpi
            if (bpi.isBpiStatusInProgress(getString(R.string.status_in_progress)))
                return@onClick
            launchStoreCard(bpi.setupIntent(activity))
        }

        payMyAccountRelativeLayout.onClick {

        }
    }
    private val activityLauncher = BetterActivityResult.registerActivityForResult(this)

    private fun launchStoreCard(intent: Intent) {
        activityLauncher.launch(intent, onActivityResult = { result ->
            StoreCardCallBack().bpiCallBack(result).apply {
                this?.let { viewModel.updateBPI(it) }

            }
        })
        activity?.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
    }
}