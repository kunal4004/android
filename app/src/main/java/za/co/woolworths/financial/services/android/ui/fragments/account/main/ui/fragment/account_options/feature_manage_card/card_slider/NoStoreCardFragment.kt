package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider


import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.InstantStoreCardReplacementCardFragmentBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardEnhancementConstant
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.util.KotlinUtils

class NoStoreCardFragment : Fragment(R.layout.instant_store_card_replacement_card_fragment) {

    companion object {
        private const val STORE_CARD_FEATURE_TYPE = "STORE_CARD_FEATURE_TYPE"
        fun newInstance(storeCard: StoreCardFeatureType?) =
            NoStoreCardFragment().withArgs {
                putParcelable(STORE_CARD_FEATURE_TYPE, storeCard)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = InstantStoreCardReplacementCardFragmentBinding.bind(view)
        val card =
            arguments?.getParcelable<StoreCardFeatureType?>(STORE_CARD_FEATURE_TYPE) as? StoreCardFeatureType.ManageMyCard
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
            binding.storeCardImageView.contentDescription =
                context?.getString(R.string.active_store_card_image_on_overlay)
            binding.accountHolderNameTextView.text = KotlinUtils.getCardHolderNameSurname()
            binding.accountHolderNameTextView.setTextColor(Color.WHITE)
            binding.accountHolderNameTextView.visibility = View.VISIBLE
            binding.accountHolderNameTextView.contentDescription =
                context?.getString(R.string.active_store_card_image_embossed_user_name_on_overlay)
        }
    }

}