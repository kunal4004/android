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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.LoaderType
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.AccountOptionsManageCardFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.TemporaryFreezeCardFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.TemporaryUnFreezeCardFragment

class TemporaryFreezeUnfreezeCardItemFragment : Fragment(R.layout.temporary_freeze_unfreeze_card_item_fragment) {

    val viewModel: TemporaryFreezeCardViewModel by activityViewModels()
    val accountViewModel: MyAccountsRemoteApiViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(TemporaryFreezeUnfreezeCardItemFragmentBinding.bind(view)) {
            setupTemporaryFreezeCardSwipe()
            setResultListeners()
            setonClickListeners()
            subscribeObservers()
        }
    }

    private fun TemporaryFreezeUnfreezeCardItemFragmentBinding.setonClickListeners() {
        temporaryFreezeCardRelativeLayout.onClick {

        }
    }

    private fun TemporaryFreezeUnfreezeCardItemFragmentBinding.subscribeObservers() {
        // Turns SwitchCompat observer on/off
        viewModel.isSwitcherEnabled.observe(viewLifecycleOwner) { isSwitcherEnabled ->
            switchTemporaryFreezeCard.isChecked = isSwitcherEnabled
        }

        // Collects api/view results from freeze/unfreeze store card
        lifecycleScope.launch {
            viewModel.blockMyCardResponse.collectLatest { state ->
                with(state) {
                    renderLoading {
                        val isLanding = (accountViewModel.loaderType == LoaderType.LANDING)
                        if (isLoading) { // show progress bar
                            accountViewModel.loaderType = LoaderType.FREEZE_CARD
                            temporaryFreezeCardRelativeLayout.isEnabled  = false
                            freezeProgressBar.visibility = VISIBLE
                            switchTemporaryFreezeCard.visibility = GONE
                            viewModel.isTempFreezeUnFreezeLoading.value = true
                        } else { // hide progressbar
                            if (isLanding) {
                                temporaryFreezeCardRelativeLayout.isEnabled = true
                                freezeProgressBar.visibility = GONE
                                switchTemporaryFreezeCard.visibility = VISIBLE
                                viewModel.isTempFreezeUnFreezeLoading.value = false
                            }
                        }
                    }
                    renderSuccess { accountViewModel.requestGetStoreCardCards() }
                }
            }
        }
    }

    // Callbacks for Freeze/Unfreeze dialog
    private fun TemporaryFreezeUnfreezeCardItemFragmentBinding.setResultListeners() {
        setFragmentResultListener(AccountOptionsManageCardFragment.MANAGE_CARD_ACCOUNT_OPTIONS) { _, bundle ->
            when (bundle.getString(AccountOptionsManageCardFragment.MANAGE_CARD_ACCOUNT_OPTIONS, "")) {

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
            if (buttonView.isPressed) { // block is active for user interaction only
                when (isChecked) {
                    true -> findNavController().navigate(
                        TemporaryFreezeUnfreezeCardItemFragmentDirections.actionTemporaryFreezeUnfreezeCardItemFragmentToTemporaryFreezeCardFragment()
                    )
                    false -> findNavController().navigate(
                        TemporaryFreezeUnfreezeCardItemFragmentDirections.actionTemporaryFreezeUnfreezeCardItemFragmentToTemporaryUnFreezeCardFragment()
                    )
                }
            }
        }
    }
}