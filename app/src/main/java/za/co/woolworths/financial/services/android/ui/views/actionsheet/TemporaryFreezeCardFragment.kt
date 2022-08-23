package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.content.DialogInterface
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.databinding.TemporaryFreezeCartLayoutBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.base.ViewBindingBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.AccountOptionsManageCardFragment.Companion.AccountOptionsLandingKey

@AndroidEntryPoint
class TemporaryFreezeCardFragment :
    ViewBindingBottomSheetDialog<TemporaryFreezeCartLayoutBinding>(TemporaryFreezeCartLayoutBinding::inflate) {

    private var mBundle0f: Bundle = bundleOf(AccountOptionsLandingKey to TEMPORARY_FREEZE_CARD_FRAGMENT_CANCEL_RESULT)

    companion object {
        const val TEMPORARY_FREEZE_CARD_FRAGMENT_CONFIRM_RESULT =
            "TEMPORARY_FREEZE_CARD_FRAGMENT_CONFIRM_RESULT"
        const val TEMPORARY_FREEZE_CARD_FRAGMENT_CANCEL_RESULT =
            "TEMPORARY_FREEZE_CARD_FRAGMENT_CANCEL_RESULT"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        with(binding.cancelTextView) {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            onClick {
                mBundle0f = bundleOf(AccountOptionsLandingKey to TEMPORARY_FREEZE_CARD_FRAGMENT_CANCEL_RESULT)
                dismiss()
            }
        }

        binding.confirmFreezeCardButton.onClick {
            mBundle0f = bundleOf(AccountOptionsLandingKey to TEMPORARY_FREEZE_CARD_FRAGMENT_CONFIRM_RESULT)
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(AccountOptionsLandingKey, mBundle0f)
    }
}