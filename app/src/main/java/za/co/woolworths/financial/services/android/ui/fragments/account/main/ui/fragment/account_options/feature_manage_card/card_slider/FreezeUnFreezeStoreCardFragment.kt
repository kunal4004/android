package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FreezeUnfreezeCardFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeUnfreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType

@AndroidEntryPoint
class FreezeUnFreezeStoreCardFragment : Fragment(R.layout.freeze_unfreeze_card_fragment) {

    val viewModel: TemporaryFreezeUnfreezeCardViewModel by activityViewModels()

    companion object {
        private const val STORE_CARD_FEATURE_TYPE = "STORE_CARD_FEATURE_TYPE"
        fun newInstance(storeCard: StoreCardFeatureType?) =
            FreezeUnFreezeStoreCardFragment().withArgs {
                putParcelable(STORE_CARD_FEATURE_TYPE, storeCard)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FreezeUnfreezeCardFragmentBinding.bind(view)
        val storeCard = arguments?.getParcelable<StoreCardFeatureType?>(STORE_CARD_FEATURE_TYPE) as? StoreCardFeatureType.StoreCardIsTemporaryFreeze
        binding.freezeUnfreezeShimmerLayout.setShimmer(null)

        viewModel.isLoading.observe(viewLifecycleOwner){ isLoading ->
                binding.freezeUnfreezeShimmerLayout.showShimmer(isLoading)
        }

        if (storeCard?.isAnimationEnabled == false) {
            binding.cardImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    when (storeCard.isStoreCardFrozen) {
                        true -> R.drawable.card_freeze
                        false -> R.drawable.w_store_card
                    }
                )
            )
        }
    }
}