package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsCreditLimitIncreaseFragmentBinding
import kotlinx.coroutines.flow.collect
import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderFailure
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils

class AccountOptionsCreditLimitIncreaseFragment :
    ViewBindingFragment<AccountOptionsCreditLimitIncreaseFragmentBinding>(
        AccountOptionsCreditLimitIncreaseFragmentBinding::inflate
    ) {

    private val viewModel: CreditLimitIncreaseViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
    }

    private fun subscribeObservers() {
        queryCLIOfferActiveRemoteService()
    }

    private fun queryCLIOfferActiveRemoteService() {
        if (viewModel.isCliFlowHiddenForProductNotInGoodStanding()) {
            binding.hideCliComponent()
            return
        }
        lifecycleScope.launchWhenStarted {
            viewModel.queryRemoteServiceCLIOfferActive().collect { result ->
                with(result) {
                    renderSuccess {
                        when (val data = viewModel.getStatus(output)) {
                            is CLILandingUIState.Consent -> binding.setCommonStateUI(
                                offerActive = data.offerActive,
                                isIncreaseMyLimitLayoutVisible = true
                            )
                            is CLILandingUIState.CommonStatus -> binding.setCommonStateUI(data.offerActive)
                            is CLILandingUIState.Unavailable -> binding.setCommonStateUI(data.offerActive)
                        }
                    }
                    renderFailure {  // TODO :: AutoConnect on internet failure
                    }
                    renderLoading { binding.showProgress(this.isLoading) }
                }
            }
        }
    }

    private fun AccountOptionsCreditLimitIncreaseFragmentBinding.hideCliComponent() {
        rootContainerLinearLayout.visibility = GONE
        topGrayDividerSpacer.visibility = GONE
    }

    private fun AccountOptionsCreditLimitIncreaseFragmentBinding.setCommonStateUI(
        offerActive: OfferActive?,
        isTopDividerVisible: Boolean = true,
        isIncreaseMyLimitLayoutVisible: Boolean = false
    ) {
        titleTextView.text = getString(R.string.cli_credit_limit_increase)
        offerActive?.apply {
            topGrayDividerSpacer.visibility = if (isTopDividerVisible) VISIBLE else GONE
            increaseMyLimitItemLinearLayout.visibility =
                if (isIncreaseMyLimitLayoutVisible) VISIBLE else GONE
            with(descriptionTextView) {
                visibility = if (messageDetail.isNullOrEmpty()) GONE else VISIBLE
                text = messageDetail
            }
            with(badgeLabelTextView) {
                visibility = VISIBLE
                KotlinUtils.roundCornerDrawable(
                    badgeLabelTextView,
                    nextStepColour ?: AppConstant.DEFAULT_TAG_HEX_COLOR
                )
                text = messageSummary
            }
        }
    }

    private fun AccountOptionsCreditLimitIncreaseFragmentBinding.showProgress(isLoading: Boolean = false) {
        cliSkeleton.loadingState(isLoading)
    }
}