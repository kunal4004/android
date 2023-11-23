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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.ui.base.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOptionsScreenUI
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.StoreCardActivityResultCallback
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

    @Inject
    lateinit var pmaButton : PayMyAccountButtonTap

    val viewModel: AccountProductsHomeViewModel by activityViewModels()
    val payMyAccountViewModel : PayMyAccountViewModel by activityViewModels()
    val homeViewModel : AccountProductsHomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(AccountOptionsListFragmentBinding.bind(view)) {
            subscribeObservers()
            setListeners()
        }
    }

    private fun AccountOptionsListFragmentBinding.subscribeObservers() {
        accountOptionsSkeleton.loadingState(false, targetedShimmerLayout = accountOptionsLayout)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewState.collectLatest { items ->
                items.forEach { item ->
                    with(item) {
                        when (this) {

                            is AccountOptionsScreenUI.PaymentOptionsScreenUI -> Unit
                            is AccountOptionsScreenUI.BalanceProtectionInsurance -> showBalanceProtectionInsuranceTag(this)
                            is AccountOptionsScreenUI.WithdrawCashNow -> hideLoanWithdrawal()
                            is AccountOptionsScreenUI.DebitOrder -> showDebitOrder(isActive)
                            else -> Unit
                        }
                    }
                }
            }
        }
        viewModel.init()
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.accountsCollectionsCheckEligibility.collectLatest { checkEligibilityResponse ->
                with(checkEligibilityResponse) {

                    renderLoading {
                        if (isLoading) {
                            showSetupPaymentPlan(false)
                            showViewYourPaymentPlan(false)
                        }
                    }

                    renderSuccess {
                        val eligibilityPlan = output.eligibilityPlan
                        when (eligibilityPlan?.actionText) {
                            ActionText.TAKE_UP_TREATMENT_PLAN.value -> {
                                showSetupPaymentPlan(
                                    isVisible
                                )
                                showViewYourPaymentPlan(false)
                            }
                            ActionText.VIEW_TREATMENT_PLAN.value -> {
                                showViewYourPaymentPlan(
                                    isVisible
                                )
                                showSetupPaymentPlan(
                                    false
                                )
                            }
                        }
                    }
                }
            }
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
            BpiInsuranceApplicationStatusType.DISABLED -> {
                balanceProtectionInsuranceRelativeLayout.visibility = GONE
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

    private fun AccountOptionsListFragmentBinding.showViewYourPaymentPlan(isVisible: Boolean) =
        when (isVisible) {
            true -> {
                viewTreatmentPlanLinearLayout.visibility = VISIBLE
                viewTreatmentPlanDivider.visibility = VISIBLE
            }
            false -> {
                viewTreatmentPlanLinearLayout.visibility = GONE
                viewTreatmentPlanDivider.visibility = GONE
            }
        }


    private fun AccountOptionsListFragmentBinding.showSetupPaymentPlan(isVisible: Boolean) =
        when (isVisible) {
            true -> {
                setupTreatmentPlanView.visibility = VISIBLE
                showPaymentPlanDivider.visibility = VISIBLE
            }
            false -> {
                setupTreatmentPlanView.visibility = GONE
                showPaymentPlanDivider.visibility = GONE
            }
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
            pmaButton.payMyAccountViewModel = payMyAccountViewModel
            pmaButton.isShimmerEnabled = false
            pmaButton.navigateToPayMyAccountActivity()
        }

        setupTreatmentPlanView.onClick { landingRouter.routeToSetupPaymentPlan(activity, viewModel) }

        viewTreatmentPlanLinearLayout.onClick { landingRouter.routeToViewTreatmentPlan(activity, viewModel) }
    }

    private val activityLauncher = BetterActivityResult.registerActivityForResult(this)

    private fun launchStoreCard(intent: Intent) {
        activityLauncher.launch(intent, onActivityResult = { result ->
            StoreCardActivityResultCallback().balanceProtectionInsuranceCallback(result).apply {
                this?.let { viewModel.updateBPI(it) }

            }
        })
        activity?.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
    }
}