package za.co.woolworths.financial.services.android.ui.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.NonNull
import androidx.core.content.res.ResourcesCompat
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProgressBarBinding

class ProgressBarDialog {

    private var dialog: Dialog? = null

    fun show(context: Context?): Dialog? {
        context?.apply {
            val binding = ProgressBarBinding.inflate(getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater, null, false)
            binding.apply {
                resources?.apply { setColorFilter(cpPbar.indeterminateDrawable, ResourcesCompat.getColor(this, R.color.black, null)) } //Progress Bar Color
            }
            dialog = Dialog(this, R.style.CustomProgressBarTheme)
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                dialog?.window?.statusBarColor = Color.TRANSPARENT
            }
            dialog?.setContentView(binding.root)

            try {
                dialog?.show()
            } catch (ex: IllegalStateException) {
                return null
            }
        }
        return dialog
    }

    fun setColorFilter(@NonNull drawable: Drawable, color: Int) = drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)

    fun dismissDialog() = dialog?.dismiss()


}