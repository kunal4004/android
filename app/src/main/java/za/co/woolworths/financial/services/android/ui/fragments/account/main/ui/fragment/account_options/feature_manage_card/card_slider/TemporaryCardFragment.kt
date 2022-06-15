package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider


import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.InstantStoreCardReplacementCardFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel.MyAccountsRemoteApiViewModel
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.util.KotlinUtils

@AndroidEntryPoint
class TemporaryCardFragment : Fragment(R.layout.instant_store_card_replacement_card_fragment) {

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
        val card = arguments?.getParcelable<StoreCardFeatureType?>(STORE_CARD_FEATURE_TYPE) as? StoreCardFeatureType.TemporaryCardEnabled
        with(InstantStoreCardReplacementCardFragmentBinding.bind(view)){
            setupView(card)
        }
    }

    private fun InstantStoreCardReplacementCardFragmentBinding.setupView(card: StoreCardFeatureType.TemporaryCardEnabled?) {
        cardImageView.setImageResource(R.drawable.ic_sc_temporary_store_card)
        accountHolderNameTextView.text = KotlinUtils.getCardHolderNameSurname()
        cardImageView.onClick {
            (requireActivity() as? StoreCardActivity)?.apply {
                accountViewModel.emitEventOnCardTap(card)
            }
        }
        cardImageView.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                    R.drawable.ic_store_card
            )
        )
    }

}