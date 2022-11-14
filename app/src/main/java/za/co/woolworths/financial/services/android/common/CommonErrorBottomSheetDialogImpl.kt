package za.co.woolworths.financial.services.android.common

import android.content.Context
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CommonErrorBottomDialogLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable

class CommonErrorBottomSheetDialogImpl: CommonErrorBottomSheetDialog {
    override fun showCommonErrorBottomDialog(
        onClickListener: ClickOnDialogButton,
        context: Context,
        title: String,
        desc: String,
        buttonText: String
    ) {
        val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val binding = CommonErrorBottomDialogLayoutBinding.inflate(dialog.layoutInflater, null, false)
        binding.imageError?.setImageDrawable(bindDrawable(R.drawable.ic_vto_error))
        binding.tvErrorTitle?.text = title
        binding.tvErrorDesc?.text = desc
        binding.gotItButton?.text = buttonText
        binding.gotItButton?.setOnClickListener {
            onClickListener.onClick()
            dialog.dismiss()
        }
        dialog.setContentView(binding.root)
        dialog.show()
    }
}

interface ClickOnDialogButton {
    fun onClick()
}