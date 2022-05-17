package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.databinding.TemporaryFreezeCartLayoutBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.base.ViewBindingBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.extension.onClick

@AndroidEntryPoint
class TemporaryFreezeCardFragment :
    ViewBindingBottomSheetDialog<TemporaryFreezeCartLayoutBinding>(TemporaryFreezeCartLayoutBinding::inflate) {

    companion object {
        const val TEMPORARY_FREEZE_CARD_FRAGMENT_CONFIRM_RESULT = "TEMPORARY_FREEZE_CARD_FRAGMENT_CONFIRM_RESULT"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        with(binding.cancelTextView) {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            onClick {
                dismiss()
            }
        }

        binding.confirmFreezeCardButton.onClick {
            setFragmentResult(TEMPORARY_FREEZE_CARD_FRAGMENT_CONFIRM_RESULT, bundleOf())
            dismiss()
        }
    }
}