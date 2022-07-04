package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountOptionsCreditLimitIncreaseFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.ui.base.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.MyAccountsUtils
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.loadingState
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.ConnectivityLiveData
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

@AndroidEntryPoint
class AccountOptionsCreditLimitIncreaseFragment : Fragment(R.layout.account_options_credit_limit_increase_fragment), View.OnClickListener {

    @Inject lateinit var myAccountsUtils: MyAccountsUtils

    @Inject lateinit var router: ProductLandingRouterImpl

    @Inject lateinit var connectivityLiveData: ConnectivityLiveData

    private val viewModel: CreditLimitIncreaseViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = AccountOptionsCreditLimitIncreaseFragmentBinding.bind(view)
        subscribeObservers(binding)
        binding.setListener()
        requestOfferActive()
    }

    private fun requestOfferActive() {
        lifecycleScope.launch {
            viewModel.queryRemoteServiceCLIOfferActive()
        }
    }

    private fun AccountOptionsCreditLimitIncreaseFragmentBinding.setListener() {
        creditLimitIncreaseContainerRelativeLayout.onClick(this@AccountOptionsCreditLimitIncreaseFragment)
        relIncreaseMyLimit.onClick(this@AccountOptionsCreditLimitIncreaseFragment)
    }

    private fun subscribeObservers(binding: AccountOptionsCreditLimitIncreaseFragmentBinding) {
        queryCLIOfferActiveRemoteService(binding)
    }

    private fun queryCLIOfferActiveRemoteService(binding: AccountOptionsCreditLimitIncreaseFragmentBinding) {

        if (viewModel.isCliFlowHiddenForProductNotInGoodStanding()) {
            binding.hideCliComponent()
            return
        }

        lifecycleScope.launch {
            connectivityLiveData.observe(viewLifecycleOwner){ isConnectionAvailable ->
                if (isConnectionAvailable && viewModel.retryNetworkRequest.isConnectionAvailableForOfferActive()){
                    lifecycleScope.launch { viewModel.queryRemoteServiceCLIOfferActive() }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.offerActive.collect { result ->
                viewModel.retryNetworkRequest.popOfferActiveRequest()
                with(result) {
                    renderSuccess {
                        val response =  output as? OfferActive
                        viewModel.mOfferActive = response
                        when (val data = viewModel.getStatus(response)) {
                            is CLILandingUIState.Consent -> binding.setCommonStateUI(
                                offerActive = data.offerActive,
                                isIncreaseMyLimitLayoutVisible = true
                            )
                            is CLILandingUIState.CommonStatus -> binding.setCommonStateUI(data.offerActive)
                            is CLILandingUIState.Unavailable -> binding.setCommonStateUI(data.offerActive)
                        }
                    }

                    renderFailure { router.routeToDefaultErrorMessageDialog(requireActivity()) }

                    renderNoConnection {
                        viewModel.retryNetworkRequest.putOfferActiveRequest()
                        router.showNoConnectionToast(requireActivity())
                    }

                    renderLoading { binding.showProgress(isLoading) }
                   
                    renderNoConnection { router.showNoConnectionToast(requireActivity()) }
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

    override fun onClick(view: View?) {
        when(view?.id){
             R.id.relIncreaseMyLimit, R.id.creditLimitIncreaseContainerRelativeLayout -> {
                if (!myAccountsUtils.isAppInstanceIdLinked()) {
                    with(viewModel) {
                        if (mOfferActive == null ||
                            isOfferDisabled(viewModel.mOfferActive)
                        ) return@onClick
                        triggerFirebaseEventForCliStartDestination(requireActivity())
                        router.routeToCreditLimitIncrease(requireActivity(),getCreditLimitIncreaseLanding())
                    }
                }
            }
        }
    }
}