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
           setupView(binding, card)
           storeCardImageView.onClick {
               (requireActivity() as? StoreCardActivity)?.apply {
                   accountViewModel.emitEventOnCardTap(card)
               }
           }
       }
    }


    private fun setupView(
        binding: InstantStoreCardReplacementCardFragmentBinding,
        activateVTC: StoreCardFeatureType.ActivateVirtualTempCard?
    ) {
        binding.accountHolderNameTextView.text = KotlinUtils.getCardHolderNameSurname()
        if (activateVTC?.isTemporaryCardEnabled == true) {
            binding.storeCardImageView.setImageResource(R.drawable.ic_sc_temporary_store_card)
            binding.tempCardLabel.visibility = View.GONE
            binding.cardLabel.visibility = View.GONE
        } else {
            binding.storeCardImageView.setImageResource(R.drawable.ic_sc_inactive)
            binding.tempCardLabel.visibility = View.VISIBLE
            binding.cardLabel.visibility = View.VISIBLE
            binding.tempCardLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_label_color))
            binding.cardLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.inactive_label_color))
            binding.tempCardLabel.text = getString(R.string.inactive)
        }
    }

}


