package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.TemporaryFreezeUnfreezeCardItemFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.LoaderType
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.device_security.linkMyDeviceIfNecessary
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.FREEZE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.SHOW_TEMPORARY_FREEZE_DIALOG
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.AccountOptionsManageCardFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.ProductLandingRouterImpl
import za.co.woolworths.financial.services.android.ui.views.actionsheet.TemporaryFreezeCardFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.TemporaryUnFreezeCardFragment
import javax.inject.Inject

@AndroidEntryPoint
class TemporaryFreezeUnfreezeCardItemFragment : Fragment(R.layout.temporary_freeze_unfreeze_card_item_fragment) {

    private var isTemporaryButtonChecked: Boolean = false
    val viewModel: TemporaryFreezeCardViewModel by activityViewModels()
    val accountViewModel: MyAccountsRemoteApiViewModel by activityViewModels()

    @Inject lateinit var router : ProductLandingRouterImpl

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(TemporaryFreezeUnfreezeCardItemFragmentBinding.bind(view)) {
            // required to prevent automatic device security popup on landing
            viewModel.mStoreCardUpsellMessageFlagState.disableFreezeStoreCardFlag()
            setupTemporaryFreezeCardSwipe()
            setResultListeners()
            subscribeObservers()
        }
    }

    private fun TemporaryFreezeUnfreezeCardItemFragmentBinding.subscribeObservers() {
        // Turns SwitchCompat observer on/off
        viewModel.isSwitcherEnabled.observe(viewLifecycleOwner) { isSwitcherEnabled ->
            switchTemporaryFreezeCard.isChecked = isSwitcherEnabled
        }

        //Todo :: check if revertSwitcherStateOnSkippedButtonTapped can be merged with mStoreCardUpsellMessageFlagState
        viewModel.mDeviceSecurityFlagState.revertSwitcherStateOnSkippedButtonTapped.observe(viewLifecycleOwner){
            if(it.isNotEmpty()) {
                switchTemporaryFreezeCard.isChecked = !switchTemporaryFreezeCard.isChecked
            }
        }

        viewModel.mStoreCardUpsellMessageFlagState.observeResult(viewLifecycleOwner){
                switchTemporaryFreezeCard.isChecked = !switchTemporaryFreezeCard.isChecked
        }

        // Collects api/view results from freeze/unfreeze store card
        lifecycleScope.launch {
            viewModel.blockMyCardResponse.collectLatest { state ->
                with(state) {

                    renderNoConnection {
                        router.showNoConnectionToast(requireActivity())
                        showLoading(ViewState.Loading(false), this@subscribeObservers)
                    }

                    renderLoading {
                        showLoading(this, this@subscribeObservers)
                    }

                    renderSuccess { accountViewModel.requestGetStoreCardCards() }

                    renderHttpFailureFromServer {  router.routeToServerErrorDialog(requireActivity(), output.response)}

                    renderFailure {  router.routeToDefaultErrorMessageDialog(requireActivity()) }
                }
            }
        }
    }

    private fun showLoading(
        loading: ViewState.Loading,
        temporaryFreezeUnfreezeCardItemFragmentBinding: TemporaryFreezeUnfreezeCardItemFragmentBinding
    ) {
        val isLanding = (accountViewModel.loaderType == LoaderType.LANDING)
        if (loading.isLoading) { // show progress bar
            accountViewModel.loaderType = LoaderType.FREEZE_CARD
            temporaryFreezeUnfreezeCardItemFragmentBinding.temporaryFreezeCardRelativeLayout.isEnabled =
                false
            temporaryFreezeUnfreezeCardItemFragmentBinding.freezeProgressBar.visibility = VISIBLE
            temporaryFreezeUnfreezeCardItemFragmentBinding.switchTemporaryFreezeCard.visibility =
                GONE
            viewModel.isTempFreezeUnFreezeLoading.value = true
        } else { // hide progressbar
            if (isLanding) {
                temporaryFreezeUnfreezeCardItemFragmentBinding.temporaryFreezeCardRelativeLayout.isEnabled =
                    true
                temporaryFreezeUnfreezeCardItemFragmentBinding.freezeProgressBar.visibility = GONE
                temporaryFreezeUnfreezeCardItemFragmentBinding.switchTemporaryFreezeCard.visibility =
                    VISIBLE
                viewModel.isTempFreezeUnFreezeLoading.value = false
            }
        }
    }

    // Callbacks for Freeze/Unfreeze dialog
    private fun TemporaryFreezeUnfreezeCardItemFragmentBinding.setResultListeners() {
        setFragmentResultListener(AccountOptionsManageCardFragment.AccountOptionsLandingKey) { _, bundle ->
            when (bundle.getString(AccountOptionsManageCardFragment.AccountOptionsLandingKey, "")) {

                TemporaryFreezeCardFragment.TEMPORARY_FREEZE_CARD_FRAGMENT_CONFIRM_RESULT -> {
                    viewModel.queryServiceBlockCardTypeFreeze()
                }

                TemporaryFreezeCardFragment.TEMPORARY_FREEZE_CARD_FRAGMENT_CANCEL_RESULT -> {
                    isTemporaryCardSwitchChecked(false)
                }

                TemporaryUnFreezeCardFragment.UN_FREEZE_TEMPORARY_CARD_CONFIRM_RESULT -> {
                    viewModel.queryServiceUnBlockCardTypeFreeze()
                }

                TemporaryUnFreezeCardFragment.UN_FREEZE_TEMPORARY_CARD_CANCEL_RESULT -> {
                    isTemporaryCardSwitchChecked(true)
                }

            }
        }
    }

    private fun TemporaryFreezeUnfreezeCardItemFragmentBinding.isTemporaryCardSwitchChecked(isChecked: Boolean) {
        switchTemporaryFreezeCard.isChecked = isChecked
    }

    private fun TemporaryFreezeUnfreezeCardItemFragmentBinding.setupTemporaryFreezeCardSwipe() {
        switchTemporaryFreezeCard.setOnCheckedChangeListener { buttonView, isChecked ->

            if (viewModel.mDeviceSecurityFlagState.disableDeviceSecurityPopupWhenRevertSwitcherNotEmpty()) return@setOnCheckedChangeListener

            if (buttonView.isPressed
                || viewModel.mStoreCardUpsellMessageFlagState.getFreezeStoreCardFlagValue() == true) {
                isTemporaryButtonChecked = isChecked
                linkMyDeviceIfNecessary(activity = requireActivity(), true , state = ApplyNowState.STORE_CARD, {
                    FREEZE_CARD_DETAIL = true
                }, {
                    if (viewModel.isStoreCardUpShellFragmentVisible ){
                        viewModel.mStoreCardUpsellMessageFlagState.activateFreezeStoreCardFlag()
                    }

                    viewModel.mStoreCardUpsellMessageFlagState.disableFreezeStoreCardFlag()// lock is active for user interaction only
                    freezeUnfreezeCardPopup(isChecked)
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (SHOW_TEMPORARY_FREEZE_DIALOG) {
            SHOW_TEMPORARY_FREEZE_DIALOG = false
            viewLifecycleOwner.lifecycleScope.launch {
                freezeUnfreezeCardPopup(isTemporaryButtonChecked)
            }
        }
    }

    private fun freezeUnfreezeCardPopup(isChecked: Boolean) {
        when (isChecked) {
            true -> findNavController().navigate(
                TemporaryFreezeUnfreezeCardItemFragmentDirections.actionTemporaryFreezeUnfreezeCardItemFragmentToTemporaryFreezeCardFragment()
            )
            false -> findNavController().navigate(
                TemporaryFreezeUnfreezeCardItemFragmentDirections.actionTemporaryFreezeUnfreezeCardItemFragmentToTemporaryUnFreezeCardFragment()
            )
        }
    }

    companion object {
        const val DEVICE_SECURITY_REQUEST_CODE : Int = 12443
    }
}