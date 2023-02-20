package za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.TemporaryFreezeCartLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
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

        val binding = TemporaryFreezeCartLayoutBinding.inflate(dialog.layoutInflater, null, false)

        binding.imageIcon.setImageDrawable(bindDrawable(R.drawable.ic_vto_error))
        binding.cancelTextView.text = context.getString(R.string.vto_dismiss)
        binding.title.text = title
        binding.description.text = description
        binding.confirmFreezeCardButton.text = btnText

        binding.confirmFreezeCardButton.setOnClickListener {
            if (btnText == context.getString(R.string.vto_change_setting)) {
                openAppSetting(context)
            } else {
                listener.tryAgain()
            }
            dialog.dismiss()
        }

        binding.cancelTextView?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.setContentView(binding.root)
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






