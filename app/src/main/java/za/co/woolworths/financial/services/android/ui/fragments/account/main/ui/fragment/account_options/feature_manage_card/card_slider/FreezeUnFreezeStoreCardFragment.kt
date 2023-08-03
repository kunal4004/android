package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FreezeUnfreezeCardFragmentBinding
import com.facebook.shimmer.Shimmer
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardEnhancementConstant
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.util.KotlinUtils

@AndroidEntryPoint
class FreezeUnFreezeStoreCardFragment : Fragment(R.layout.freeze_unfreeze_card_fragment) {

    private var binding: FreezeUnfreezeCardFragmentBinding? = null
    val viewModel: TemporaryFreezeCardViewModel by activityViewModels()
    val accountViewModel: MyAccountsRemoteApiViewModel by activityViewModels()

    companion object {
        private const val STORE_CARD_FEATURE_TYPE = "STORE_CARD_FEATURE_TYPE"
        fun newInstance(storeCard: StoreCardFeatureType?) =
            FreezeUnFreezeStoreCardFragment().withArgs {
                putParcelable(STORE_CARD_FEATURE_TYPE, storeCard)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreezeUnfreezeCardFragmentBinding.bind(view)
        val card = arguments?.getParcelable<StoreCardFeatureType?>(STORE_CARD_FEATURE_TYPE) as? StoreCardFeatureType.StoreCardIsTemporaryFreeze
        binding?.freezeUnfreezeShimmerLayout?.setShimmer(null)
        val isBlockTypeNewCard = card?.storeCard?.blockType?.equals(StoreCardEnhancementConstant.NewCard, ignoreCase = true) == true
        if(isBlockTypeNewCard){
            binding?.accountHolderNameTextView?.visibility = View.INVISIBLE
            binding?.storeCardImageView?.setImageDrawable( ContextCompat.getDrawable(requireContext(), R.drawable.store_card_new_card_image))
            binding?.storeCardImageView?.contentDescription = context?.getString(R.string.active_store_card_image_on_overlay_new_card)
        }else {
            binding?.accountHolderNameTextView?.text = KotlinUtils.getCardHolderNameSurname()
            viewModel.isTempFreezeUnFreezeLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    // show shimmer
                    binding?.freezeUnfreezeShimmerLayout?.setShimmer(
                        Shimmer.AlphaHighlightBuilder().build()
                    )
                    binding?.freezeUnfreezeShimmerLayout?.showShimmer(true)
                    binding?.freezeUnfreezeShimmerLayout?.startShimmer()
                } else {
                    // hide shimmer
                    binding?.freezeUnfreezeShimmerLayout?.setShimmer(null)
                    binding?.freezeUnfreezeShimmerLayout?.showShimmer(false)
                    binding?.freezeUnfreezeShimmerLayout?.stopShimmer()
                }

            }
            setCardImage(card, binding)
            binding?.storeCardImageView?.onClick {
                (requireActivity() as? StoreCardActivity)?.apply {
                    accountViewModel.emitEventOnCardTap(card)
                }
            }
        }
    }

    private fun setCardImage(
        storeCard: StoreCardFeatureType.StoreCardIsTemporaryFreeze?,
        binding: FreezeUnfreezeCardFragmentBinding?
    ) {
        binding?.accountHolderNameTextView?.contentDescription = context?.getString(R.string.active_store_card_image_embossed_user_name_on_overlay)
        binding?.accountHolderNameTextView?.setTextColor(Color.WHITE)
        when (storeCard?.isStoreCardFrozen){
            true -> {
                binding?.accountHolderNameTextView?.visibility =View.GONE
                binding?.storeCardImageView?.setImageDrawable(ContextCompat.getDrawable(
                    requireContext(),R.drawable.store_card_frozen))
                binding?.storeCardImageView?.contentDescription = context?.getString(R.string.frozen_store_card_image_on_overlay)
            }
            else -> {
                binding?.accountHolderNameTextView?.visibility = View.VISIBLE
                binding?.storeCardImageView?.setImageDrawable( ContextCompat.getDrawable(
                    requireContext(),R.drawable.store_card_active))
                binding?.accountHolderNameTextView?.text = KotlinUtils.getCardHolderNameSurname()
                binding?.storeCardImageView?.contentDescription = context?.getString(R.string.active_store_card_image_on_overlay)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.root?.requestLayout()
    }
}