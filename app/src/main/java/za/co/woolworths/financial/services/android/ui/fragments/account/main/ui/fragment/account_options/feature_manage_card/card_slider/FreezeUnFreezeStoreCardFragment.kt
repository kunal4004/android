package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card_slider

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.awfs.coordination.databinding.InstantStoreCardReplacementCardFragmentBinding
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment

class FreezeUnFreezeStoreCardFragment() :
    ViewBindingFragment<InstantStoreCardReplacementCardFragmentBinding>(
        InstantStoreCardReplacementCardFragmentBinding::inflate
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.card_freeze))
    }
}