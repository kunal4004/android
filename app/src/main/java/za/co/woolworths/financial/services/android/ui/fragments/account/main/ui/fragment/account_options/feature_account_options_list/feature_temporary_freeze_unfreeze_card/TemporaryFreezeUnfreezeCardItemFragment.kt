package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.feature_temporary_freeze_unfreeze_card

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.TemporaryFreezeUnfreezeCardItemFragmentBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.AccountOptionsManageCardFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.TemporaryFreezeCardFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.TemporaryUnFreezeCardFragment

class TemporaryFreezeUnfreezeCardItemFragment : Fragment(R.layout.temporary_freeze_unfreeze_card_item_fragment) {

    val viewModel: TemporaryFreezeUnfreezeCardViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(TemporaryFreezeUnfreezeCardItemFragmentBinding.bind(view)){
            setupTemporaryFreezeCardSwipe()
            setResultListeners()
            subscribeObservers()
        }
    }

    private fun TemporaryFreezeUnfreezeCardItemFragmentBinding.subscribeObservers() {
        viewModel.isSwitcherEnabled.observe(viewLifecycleOwner){ isSwitcherEnabled ->
            switchTemporaryFreezeCard.isChecked = isSwitcherEnabled
        }
    }

    private fun TemporaryFreezeUnfreezeCardItemFragmentBinding.setResultListeners() {
        setFragmentResultListener(AccountOptionsManageCardFragment.MANAGE_CARD_ACCOUNT_OPTIONS) { _, bundle ->
            when (bundle.getString(AccountOptionsManageCardFragment.MANAGE_CARD_ACCOUNT_OPTIONS, "")) {
                TemporaryFreezeCardFragment.TEMPORARY_FREEZE_CARD_FRAGMENT_CONFIRM_RESULT -> {
                    queryServiceBlockUnblockStoreCard()
                }

                TemporaryFreezeCardFragment.TEMPORARY_FREEZE_CARD_FRAGMENT_CANCEL_RESULT -> {
                    isTemporaryCardSwitchChecked(false)
                }

                TemporaryUnFreezeCardFragment.UN_FREEZE_TEMPORARY_CARD_CONFIRM_RESULT -> {
                    queryServiceBlockUnblockStoreCard()
                   // val lists = adapter.getListOfStoreCards()
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
                    true -> findNavController().navigate(TemporaryFreezeUnfreezeCardItemFragmentDirections.actionTemporaryFreezeUnfreezeCardItemFragmentToTemporaryFreezeCardFragment())
                    false -> findNavController().navigate(TemporaryFreezeUnfreezeCardItemFragmentDirections.actionTemporaryFreezeUnfreezeCardItemFragmentToTemporaryUnFreezeCardFragment())
                }
            }
        }
    }

    private fun TemporaryFreezeUnfreezeCardItemFragmentBinding.queryServiceBlockUnblockStoreCard() {
        lifecycleScope.launch {
            viewModel.queryServiceBlockUnblockStoreCard().collect { state ->
                with(state){
                    renderLoading {
                        viewModel.isLoading.value = isLoading
                        // TODO:: isFreezeStoreCardLoading(isLoading)
                    }
                }
            }
        }
    }
}