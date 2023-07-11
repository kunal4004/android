package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.InstantStoreCardReplacementCardFragmentBinding
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardEnhancementConstant
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.util.KotlinUtils

class ActivateVirtualTempCardFragment :
    Fragment(R.layout.instant_store_card_replacement_card_fragment) {

    val accountViewModel: MyAccountsRemoteApiViewModel by activityViewModels()

    companion object {
        private const val STORE_CARD_FEATURE_TYPE = "STORE_CARD_FEATURE_TYPE"
        fun newInstance(storeCard: StoreCardFeatureType?) =
            ActivateVirtualTempCardFragment().withArgs {
                putParcelable(STORE_CARD_FEATURE_TYPE, storeCard)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = InstantStoreCardReplacementCardFragmentBinding.bind(view)
        val card = arguments?.getParcelable<StoreCardFeatureType?>(STORE_CARD_FEATURE_TYPE) as? StoreCardFeatureType.ActivateVirtualTempCard
       with(binding) {
           val isBlockTypeNewCard = card?.storeCard?.blockType?.equals(StoreCardEnhancementConstant.NewCard, ignoreCase = true) == true
           if (isBlockTypeNewCard) {
               binding.accountHolderNameTextView.visibility = View.INVISIBLE
               binding.storeCardImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.store_card_new_card_image))
               binding.storeCardImageView.contentDescription = context?.getString(R.string.active_store_card_image_on_overlay_new_card)
           }else {
               setupView(binding, card)
               storeCardImageView.onClick {
                   (requireActivity() as? StoreCardActivity)?.apply {
                       accountViewModel.emitEventOnCardTap(card)
                   }
               }
           }
       }
    }


    private fun setupView(
        binding: InstantStoreCardReplacementCardFragmentBinding,
        activateVTC: StoreCardFeatureType.ActivateVirtualTempCard?
    ) {
        val isBlockTypeNewCard = activateVTC?.storeCard?.blockType?.equals(
            StoreCardEnhancementConstant.NewCard, ignoreCase = true) == true
        if(isBlockTypeNewCard){
            binding.accountHolderNameTextView.visibility = View.INVISIBLE
            binding.storeCardImageView.setImageDrawable( ContextCompat.getDrawable(requireContext(), R.drawable.store_card_new_card_image))
            binding.storeCardImageView.contentDescription = context?.getString(R.string.active_store_card_image_on_overlay_new_card)

        }else {
            if (activateVTC?.isTemporaryCardEnabled == true) {
                binding.storeCardImageView.setImageResource(R.drawable.store_card_virtual_temp)
                binding.storeCardImageView.contentDescription =
                    context?.getString(R.string.active_vtsc_card_image_on_overlay)
                binding.tempCardLabel.visibility = View.GONE
                binding.cardLabel.visibility = View.GONE
                binding.accountHolderNameTextView.text = KotlinUtils.getCardHolderNameSurname()
                binding.accountHolderNameTextView.visibility = View.VISIBLE
                binding.accountHolderNameTextView.contentDescription =
                    context?.getString(R.string.active_vtsc_card_image_embossed_name_label_text_on_overlay)
            } else {
                binding.storeCardImageView.setImageResource(R.drawable.ic_sc_inactive)
                binding.storeCardImageView.contentDescription =
                    context?.getString(R.string.inactive_store_card_image_on_overlay)
                binding.tempCardLabel.visibility = View.VISIBLE
                binding.cardLabel.visibility = View.VISIBLE
                binding.storeCardImageView.contentDescription =
                    context?.getString(R.string.active_vtsc_card_image_on_overlay)
                binding.tempCardLabel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.inactive_label_color
                    )
                )
                binding.cardLabel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.inactive_label_color
                    )
                )
                binding.tempCardLabel.text = getString(R.string.inactive)
                binding.accountHolderNameTextView.visibility = View.GONE
            }
        }
    }

}


