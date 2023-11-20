package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card


import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.awfs.coordination.R
import com.awfs.coordination.databinding.StoreCardUpshellMessageFragmentBinding
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardEnhancementConstant
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType

class StoreCardFreezeCardUpshellMessage : Fragment(R.layout.store_card_upshell_message_fragment) {

    val viewModel: TemporaryFreezeCardViewModel by activityViewModels()
    val accountViewModel: MyAccountsRemoteApiViewModel by activityViewModels()

    companion object {
        private const val STORE_CARD_FEATURE_TYPE = "STORE_CARD_FEATURE_TYPE"
        fun newInstance(storeCard: StoreCardFeatureType?) =
            StoreCardFreezeCardUpshellMessage().withArgs {
                putParcelable(STORE_CARD_FEATURE_TYPE, storeCard)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeFrageLifeCycle()
        val binding = StoreCardUpshellMessageFragmentBinding.bind(view)
        val card =
            arguments?.getParcelable<StoreCardFeatureType?>(STORE_CARD_FEATURE_TYPE) as? StoreCardFeatureType.StoreCardIsTemporaryFreeze
        val isBlockTypeNewCard = card?.storeCard?.blockType?.equals(
            StoreCardEnhancementConstant.NewCard,
            ignoreCase = true
        ) == true
        if (isBlockTypeNewCard) {
            binding.accountHolderNameTextView.visibility = View.INVISIBLE
            binding.storeCardImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.store_card_new_card_image
                )
            )
            binding.storeCardImageView.contentDescription =
                context?.getString(R.string.active_store_card_image_on_overlay_new_card)

        } else {
            binding.storeCardImageView.setImageResource(R.drawable.virtual_temp_freeze)
            binding.storeCardImageView.onClick {
                viewModel.mStoreCardUpsellMessageFlagState.activateFreezeStoreCardFlag()
                (requireActivity() as? StoreCardActivity)?.apply {
                    accountViewModel.emitEventOnCardTap(card)
                }
            }
        }
    }
    private fun observeFrageLifeCycle(){
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                viewModel.isStoreCardUpShellFragmentVisible = true
            }

            override fun onPause(owner: LifecycleOwner) {
                viewModel.isStoreCardUpShellFragmentVisible = false
            }
        })
    }
}