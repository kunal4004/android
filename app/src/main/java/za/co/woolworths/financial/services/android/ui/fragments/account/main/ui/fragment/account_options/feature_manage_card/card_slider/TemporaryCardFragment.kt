package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.InstantStoreCardReplacementCardFragmentBinding
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.util.KotlinUtils

class TemporaryCardFragment : Fragment(R.layout.instant_store_card_replacement_card_fragment) {

    val viewModel: TemporaryFreezeCardViewModel by activityViewModels()
    val accountViewModel: MyAccountsRemoteApiViewModel by activityViewModels()

    companion object {
        private const val STORE_CARD_FEATURE_TYPE = "STORE_CARD_FEATURE_TYPE"
        fun newInstance(storeCard: StoreCardFeatureType?) =
            TemporaryCardFragment().withArgs {
                putParcelable(STORE_CARD_FEATURE_TYPE, storeCard)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val card = arguments?.getParcelable<StoreCardFeatureType?>(STORE_CARD_FEATURE_TYPE) as? StoreCardFeatureType.TemporaryCardEnabled
        val binding = InstantStoreCardReplacementCardFragmentBinding.bind(view)
        setupView(binding)
        setListener(binding, card)
    }

    private fun setListener(binding: InstantStoreCardReplacementCardFragmentBinding,
                            card: StoreCardFeatureType.TemporaryCardEnabled?) {
        binding.storeCardImageView.onClick {
            (requireActivity() as? StoreCardActivity)?.apply {
                accountViewModel.emitEventOnCardTap(card)
            }
        }
    }

    private fun setupView(binding: InstantStoreCardReplacementCardFragmentBinding) {
        binding.storeCardImageView.setImageResource(R.drawable.store_card_virtual_temp)
        binding.accountHolderNameTextView.text = KotlinUtils.getCardHolderNameSurname()
        binding.accountHolderNameTextView.visibility = VISIBLE
        binding.accountHolderNameTextView.setTextColor(Color.BLACK)
        binding.tempCardLabel.visibility = VISIBLE
        binding.cardLabel.visibility = VISIBLE
    }
}