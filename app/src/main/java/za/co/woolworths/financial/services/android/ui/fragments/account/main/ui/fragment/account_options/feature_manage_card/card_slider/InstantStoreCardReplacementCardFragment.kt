package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider

import android.os.Bundle
import android.view.View
import com.awfs.coordination.databinding.InstantStoreCardReplacementCardFragmentBinding
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.DetermineCardToDisplay

class InstantStoreCardReplacementCardFragment() :
    ViewBindingFragment<InstantStoreCardReplacementCardFragmentBinding>(
        InstantStoreCardReplacementCardFragmentBinding::inflate
    ) {

    companion object {
        private const val STORE_CARD = "STORE_CARD"
        fun newInstance(storeCard: StoreCard?) =
            InstantStoreCardReplacementCardFragment().withArgs {
                putParcelable(STORE_CARD, storeCard)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val storeCard = arguments?.getParcelable<StoreCard?>(STORE_CARD)
        when (storeCard?.cardDisplay) {
            DetermineCardToDisplay.StoreCardIsInstantReplacementCardAndInactive -> binding.cardImageView.alpha = 0.3f
            else -> Unit
        }
    }

}