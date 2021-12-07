package za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.temporary_freeze_cart_layout.view.*
import kotlinx.android.synthetic.main.temporary_unfreeze_cart_layout.view.cancelTextView
import kotlinx.android.synthetic.main.temporary_unfreeze_cart_layout.view.description
import kotlinx.android.synthetic.main.temporary_unfreeze_cart_layout.view.title
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import javax.inject.Inject

class VTOErrorSettingBottomSheetDialog @Inject constructor(

) : VtoErrorBottomSheetDialog {

    private lateinit var listener: VtoTryAgainListener

    override fun showErrorBottomSheetDialog(
        fragment: Fragment,
        context: Context,
        title: String,
        description: String,
        btnText: String
    ) {
        try {
            listener = fragment as VtoTryAgainListener
        } catch (e: Exception) {
        }

        val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)

        val view = dialog.layoutInflater.inflate(R.layout.temporary_freeze_cart_layout, null)

        view.imageIcon.setImageDrawable(bindDrawable(R.drawable.ic_vto_error))
        view.cancelTextView.text = context.getString(R.string.vto_dismiss)
        view.title.text = title
        view.description.text = description
        view.confirmFreezeCardButton.text = btnText

        view.confirmFreezeCardButton.setOnClickListener {
            if (btnText == context.getString(R.string.vto_change_setting)) {
                openAppSetting(context)
            } else {
                listener.tryAgain()
            }
            dialog.dismiss()
        }

        view.cancelTextView?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.setContentView(view)
        dialog.show()
    }
}


private fun openAppSetting(context: Context) {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:" + context.packageName)
    ).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(this)
    }
}






