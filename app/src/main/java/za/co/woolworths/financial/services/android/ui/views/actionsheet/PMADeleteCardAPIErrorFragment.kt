package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ErrorDialogFragmentBinding
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class PMADeleteCardAPIErrorFragment : WBottomSheetDialogFragment() {

    private lateinit var binding: ErrorDialogFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ErrorDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            tvDescription?.text = bindString(R.string.pma_delete_card_failure_textview)
            okButtonTapped?.setOnClickListener {
                AnimationUtilExtension.animateViewPushDown(it)
                dismiss()
            }
        }
    }
}