package za.co.woolworths.financial.services.android.common

import android.content.Context
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.common_error_bottom_dialog_layout.view.*
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import javax.inject.Inject

class CommonErrorBottomSheetDialogImpl @Inject constructor(

) : CommonErrorBottomSheetDialog {
    override fun showCommonErrorBottomDialog(
        onClickListener: ClickOnDialogButton,
        context: Context,
        title: String,
        desc: String,
        buttonText: String
    ) {
        val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val view = dialog.layoutInflater.inflate(R.layout.common_error_bottom_dialog_layout, null)
        view?.imageError?.setImageDrawable(bindDrawable(R.drawable.ic_vto_error))
        view?.tvErrorTitle?.text = title
        view?.tvErrorDesc?.text = desc
        view?.gotItButton?.text = buttonText
        view?.gotItButton?.setOnClickListener {
            onClickListener.onClick()
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.show()
    }
}
interface ClickOnDialogButton {
    fun onClick()
}