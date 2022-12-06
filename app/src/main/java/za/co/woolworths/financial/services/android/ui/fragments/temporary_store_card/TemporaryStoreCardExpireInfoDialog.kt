package za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.TemporaryStoreCardExpireInfoDialogBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class TemporaryStoreCardExpireInfoDialog : WBottomSheetDialogFragment() {
    companion object {
        fun newInstance() = TemporaryStoreCardExpireInfoDialog().withArgs {
        }
    }

    private lateinit var binding: TemporaryStoreCardExpireInfoDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = TemporaryStoreCardExpireInfoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.done.setOnClickListener { dismiss() }
    }
}