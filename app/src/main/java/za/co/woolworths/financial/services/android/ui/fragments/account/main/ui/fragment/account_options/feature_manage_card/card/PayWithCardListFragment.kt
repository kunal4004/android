package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card

import android.os.Bundle
import android.view.View
import android.view.View.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.PayWithCardListFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.device_security.linkMyDeviceIfNecessary
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.PAY_WITH_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.SHOW_PAY_WITH_CARD_SCREEN
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@AndroidEntryPoint
class PayWithCardListFragment : Fragment(R.layout.pay_with_card_list_fragment) {
    companion object {
        const val PAY_WITH_CARD_ON_DISMISS_RESULT_LISTENER = "PAY_WITH_CARD_ON_DISMISS_RESULT_LISTENER"
        const val PAY_WITH_CARD_REQUEST_LISTENER  = "PAY_WITH_CARD_REQUEST_LISTENER"
    }

    private var binding: PayWithCardListFragmentBinding? = null
    @Inject lateinit var router: ProductLandingRouterImpl

    val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PayWithCardListFragmentBinding.bind(view)
        binding?.apply {
            setOnClickEvent()
            setResultListener()
            subscribeObserver()
        }
    }

    private fun PayWithCardListFragmentBinding.subscribeObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.payWithCardTap.collectLatest { wasTapped ->
                if (wasTapped && isAdded) {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_PAY,
                        requireActivity()
                    )
                    initPayWithCard()
                    viewModel.setPayWithCard(false)
                }
            }
        }
    }

    private fun setResultListener() {
        setFragmentResultListener(PAY_WITH_CARD_REQUEST_LISTENER) { _, bundle ->
            when (bundle.getString(PAY_WITH_CARD_REQUEST_LISTENER, "")) {
                PAY_WITH_CARD_ON_DISMISS_RESULT_LISTENER -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.queryServiceBlockPayWithCardStoreCard()
                    }
                }
            }
        }
    }

    private fun PayWithCardListFragmentBinding.setOnClickEvent(isDeviceLinked: Boolean = true) {
        viewLifecycleOwner.lifecycleScope.launch {
            payWithCardRelativeLayout.onClick {
                linkMyDeviceIfNecessary(requireActivity(), isDeviceLinked = isDeviceLinked,ApplyNowState.STORE_CARD, {
                    PAY_WITH_CARD_DETAIL = true
                }, {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_PAY,
                        requireActivity()
                    )
                    initPayWithCard()
                })
            }
        }

    }

    private fun PayWithCardListFragmentBinding.initPayWithCard() {
        when (viewModel.dataSource.isOneTimePinUnblockStoreCardEnabled()) {
            true -> router.routeToOTPActivity(requireActivity())
            false -> lifecycleScope.launch {
                viewModel.queryServiceUnBlockPayWithCardStoreCard()
                viewModel.payWithCardUnBlockCardResponse.collect { state ->
                    with(state) {
                        renderNoConnection {
                            router.showNoConnectionToast(requireActivity())
                            showLoading(ViewState.Loading(false), this@initPayWithCard)
                        }

                        renderLoading {
                            showLoading(this@renderLoading, this@initPayWithCard)
                        }

                        renderSuccess {
                            router.routeToScanToPayBarcode(
                                findNavController(),
                                viewModel.dataSource.getStoreCardsResponse()
                            )
                        }

                        renderHttpFailureFromServer { router.routeToServerErrorDialog(requireActivity(), output.response) }

                        renderFailure { router.routeToDefaultErrorMessageDialog(requireActivity()) }
                        
                    }
                }
            }
        }
    }

    private fun showLoading(
        loading: ViewState.Loading,
        payWithCardListFragmentBinding: PayWithCardListFragmentBinding
    ) {
        when (loading.isLoading) {
            true -> {
                payWithCardListFragmentBinding.payWithCardTokenProgressBar.visibility = VISIBLE
                payWithCardListFragmentBinding.payWithCardNext.visibility = GONE
            }
            false -> {
                payWithCardListFragmentBinding.payWithCardTokenProgressBar.visibility = INVISIBLE
                payWithCardListFragmentBinding.payWithCardNext.visibility = VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (SHOW_PAY_WITH_CARD_SCREEN){
            SHOW_PAY_WITH_CARD_SCREEN = false
            viewModel.setPayWithCard(true)
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}