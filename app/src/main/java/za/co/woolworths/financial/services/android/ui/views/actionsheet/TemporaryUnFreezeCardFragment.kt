package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.content.DialogInterface
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.databinding.TemporaryUnfreezeCartLayoutBinding
import za.co.woolworths.financial.services.android.ui.base.ViewBindingBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.base.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.AccountOptionsManageCardFragment.Companion.AccountOptionsLandingKey

class TemporaryUnFreezeCardFragment :
    ViewBindingBottomSheetDialog<TemporaryUnfreezeCartLayoutBinding>(
        TemporaryUnfreezeCartLayoutBinding::inflate
    ) {

    private var mBundle0f: Bundle =
        bundleOf(AccountOptionsLandingKey to UN_FREEZE_TEMPORARY_CARD_CANCEL_RESULT)

    companion object {
        const val UN_FREEZE_TEMPORARY_CARD_CONFIRM_RESULT =
            "UN_FREEZE_TEMPORARY_CARD_CONFIRM_RESULT"
        const val UN_FREEZE_TEMPORARY_CARD_CANCEL_RESULT = "UN_FREEZE_TEMPORARY_CARD_CANCEL_RESULT"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initUI()
    }

    private fun TemporaryUnfreezeCartLayoutBinding.initUI() {
        cancelTextView.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            onClick {
                mBundle0f =
                    bundleOf(AccountOptionsLandingKey to UN_FREEZE_TEMPORARY_CARD_CANCEL_RESULT)
                dismiss()
            }
        }

        unfreezeMyCardButton.onClick {
            mBundle0f =
                bundleOf(AccountOptionsLandingKey to UN_FREEZE_TEMPORARY_CARD_CONFIRM_RESULT)
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(AccountOptionsLandingKey, mBundle0f)
    }
}