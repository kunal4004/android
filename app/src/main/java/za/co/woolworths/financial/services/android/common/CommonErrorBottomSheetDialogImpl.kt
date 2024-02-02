package za.co.woolworths.financial.services.android.common

import android.content.Context
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CommonErrorBottomDialogLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import javax.inject.Inject

class CommonErrorBottomSheetDialogImpl @Inject constructor(): CommonErrorBottomSheetDialog {
    override fun showCommonErrorBottomDialog(
        onClickListener: ClickOnDialogButton,
        context: Context,
        title: String,
        desc: String,
        buttonText: String,
        isDismissButtonNeeded: Boolean,
        isCanceledOnTouchOutside : Boolean
    ) {
        val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val binding = CommonErrorBottomDialogLayoutBinding.inflate(dialog.layoutInflater, null, false)
        binding.imageError.setImageDrawable(bindDrawable(R.drawable.ic_vto_error))
        binding.tvErrorTitle.text = title
        binding.tvErrorDesc.text = desc
        binding.gotItButton.text = buttonText
        if (isDismissButtonNeeded){
            binding.cancelButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    onClickListener.onDismiss()
                    dialog.dismiss()
                }
            }

        }
        binding.gotItButton.setOnClickListener {
            onClickListener.onClick()
            dialog.dismiss()
        }
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside)
        dialog.show()
    }
}

interface ClickOnDialogButton {
    fun onClick()
    fun onDismiss()
}