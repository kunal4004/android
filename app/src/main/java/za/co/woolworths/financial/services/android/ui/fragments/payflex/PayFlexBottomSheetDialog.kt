package za.co.woolworths.financial.services.android.ui.fragments.payflex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.PayflexBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.AppConstant

class PayFlexBottomSheetDialog : WBottomSheetDialogFragment() {
    private lateinit var binding: PayflexBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = PayflexBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheetBehavior = bottomSheetDialog.behavior
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.isDraggable = false
        binding.apply {
            val maxHeight: Int = (resources.displayMetrics.heightPixels * 0.8).toInt()
            payFlexWebView.apply {
                layoutParams.height = maxHeight
                settings.javaScriptEnabled = true
                loadUrl(AppConstant.PAYFLEX_POP_UP_URL)
            }
            gotIt.setOnClickListener {
                dismiss()
            }
        }
    }
}
