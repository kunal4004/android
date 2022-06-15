package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider

import android.graphics.Color
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
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType

class InstantStoreCardReplacementCardFragment : Fragment(R.layout.instant_store_card_replacement_card_fragment) {

    val viewModel: MyAccountsRemoteApiViewModel by activityViewModels()

    companion object {
        private const val STORE_CARD_FEATURE_TYPE = "STORE_CARD_FEATURE_TYPE"
        fun newInstance(storeCard: StoreCardFeatureType?) =
            InstantStoreCardReplacementCardFragment().withArgs {
                putParcelable(STORE_CARD_FEATURE_TYPE, storeCard)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val card = arguments?.getParcelable<StoreCardFeatureType?>(STORE_CARD_FEATURE_TYPE) as? StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive
        with(InstantStoreCardReplacementCardFragmentBinding.bind(view)){
            accountHolderNameTextView.text = card?.cardHolderName
            accountHolderNameTextView.setTextColor(Color.WHITE)
            cardImageView.onClick { viewModel.emitEventOnCardTap(card) }
            cardImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                        R.drawable.ic_store_card
                ))
        }
    }

}