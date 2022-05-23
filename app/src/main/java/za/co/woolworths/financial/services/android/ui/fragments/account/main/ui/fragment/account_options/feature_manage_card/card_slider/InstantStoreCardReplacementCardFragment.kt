package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider

import android.os.Bundle
import android.view.View
import com.awfs.coordination.databinding.InstantStoreCardReplacementCardFragmentBinding
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType

class InstantStoreCardReplacementCardFragment :
    ViewBindingFragment<InstantStoreCardReplacementCardFragmentBinding>(
        InstantStoreCardReplacementCardFragmentBinding::inflate
    ) {

    companion object {
        private const val STORE_CARD_FEATURE_TYPE = "STORE_CARD_FEATURE_TYPE"
        fun newInstance(storeCard: StoreCardFeatureType?) =
            InstantStoreCardReplacementCardFragment().withArgs {
                putParcelable(STORE_CARD_FEATURE_TYPE, storeCard)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (arguments?.getParcelable<StoreCardFeatureType?>(STORE_CARD_FEATURE_TYPE)) {
           is StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive -> binding.cardImageView.alpha = 0.3f
            else -> Unit
        }
    }
}