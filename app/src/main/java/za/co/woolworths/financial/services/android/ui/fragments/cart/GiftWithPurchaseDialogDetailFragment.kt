package za.co.woolworths.financial.services.android.ui.fragments.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.GiftWithPurchaseDialogFragmentBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class GiftWithPurchaseDialogDetailFragment : WBottomSheetDialogFragment() {

    private lateinit var binding: GiftWithPurchaseDialogFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = GiftWithPurchaseDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gotItButton?.setOnClickListener { dismiss() }
    }
}