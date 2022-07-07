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
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@AndroidEntryPoint
class PayWithCardListFragment : Fragment(R.layout.pay_with_card_list_fragment) {

    companion object {
        var PAY_WITH_CARD_DETAIL = false
        const val PAY_WITH_CARD_ON_DISMISS_RESULT_LISTENER = "PAY_WITH_CARD_ON_DISMISS_RESULT_LISTENER"
        const val PAY_WITH_CARD_REQUEST_LISTENER  = "PAY_WITH_CARD_REQUEST_LISTENER"
    }

    @Inject lateinit var router: ProductLandingRouterImpl

    private val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()
    private val freezeViewModel: TemporaryFreezeCardViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(PayWithCardListFragmentBinding.bind(view)) {
            setOnClickEvent()
            setResultListener()
        }
    }

    private fun setResultListener() {
        setFragmentResultListener(PAY_WITH_CARD_REQUEST_LISTENER) { _, bundle ->
            when (bundle.getString(PAY_WITH_CARD_REQUEST_LISTENER, "")) {

                PAY_WITH_CARD_ON_DISMISS_RESULT_LISTENER -> {
                    lifecycleScope.launch {
                        viewModel.queryServiceBlockStoreCard(storeCardType = StoreCardType.VIRTUAL_TEMP_CARD).collect {}
                    }
                }
            }
        }
    }

    private fun PayWithCardListFragmentBinding.setOnClickEvent() {
        lifecycleScope.launchWhenStarted {
            payWithCardRelativeLayout.onClick {
                KotlinUtils.linkDeviceIfNecessary(requireActivity(), ApplyNowState.STORE_CARD, {
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

        howItWorksRelativeLayout.onClick {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_HOW_TO,
                requireActivity()
            )
            router.routeToHowItWorks(
                requireActivity(),
                viewModel.dataSource.isStaffMemberAndHasTemporaryCard(),
                viewModel.dataSource.getVirtualCardStaffMemberMessage()
            )
        }
    }

    private fun PayWithCardListFragmentBinding.initPayWithCard() {
        when (viewModel.dataSource.isOneTimePinUnblockStoreCardEnabled()) {
            true -> router.routeToOTPActivity(requireActivity())
            false -> lifecycleScope.launch {

                freezeViewModel.queryServiceUnBlockStoreCard(StoreCardType.VIRTUAL_TEMP_CARD).collect { state ->
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

}