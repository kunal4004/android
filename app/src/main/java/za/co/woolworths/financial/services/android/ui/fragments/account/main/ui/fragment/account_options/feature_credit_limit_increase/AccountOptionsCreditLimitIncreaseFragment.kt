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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.models.dto.linkdevice.LinkedDeviceResponse
import za.co.woolworths.financial.services.android.ui.base.doOnLayoutReady
import za.co.woolworths.financial.services.android.ui.base.isOverlappingWith
import za.co.woolworths.financial.services.android.ui.base.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderFailure
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderNoConnection
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
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
    private val disposable: CompositeDisposable? = CompositeDisposable()

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

        disposable?.add(
            WoolworthsApplication
                .getInstance()
                .bus()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .ofType(LinkedDeviceResponse::class.java)
                .subscribe {
                    lifecycleScope.launch { viewModel.queryRemoteServiceCLIOfferActive() }
                }
        )
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

        lifecycleScope.launch {
            viewModel.offerActive.collect { result ->
                viewModel.retryNetworkRequest.popOfferActiveRequest()
                with(result) {

                    renderLoading { binding.showProgress(isLoading) }

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
            with(badgeLabelBottomTextView) {
                KotlinUtils.roundCornerDrawable(
                    badgeLabelBottomTextView,
                    nextStepColour ?: AppConstant.DEFAULT_TAG_HEX_COLOR
                )
                text = messageSummary
            }
            // If the title overlaps with the badge on the right,
            // then hide the badge and show the bottom one instead
            badgeContainerBottom.visibility = GONE
            titleTextView.doOnLayoutReady {
                if (titleTextView.isOverlappingWith(badgeContainerRight)) {
                    badgeContainerRight.visibility = GONE
                    badgeContainerBottom.visibility = VISIBLE
                } else {
                    badgeContainerRight.visibility = VISIBLE
                    badgeContainerBottom.visibility = GONE
                }
            }
        }
    }

    private fun AccountOptionsCreditLimitIncreaseFragmentBinding.showProgress(isLoading: Boolean = false) {
        cliSkeleton.loadingState(isLoading, targetedShimmerLayout = contentLinearLayout, shimmerContainer = cliSkeleton)
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

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }
}