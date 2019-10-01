package za.co.woolworths.financial.services.android.ui.views

import android.app.Dialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import androidx.annotation.NonNull
import androidx.core.content.res.ResourcesCompat
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.progress_bar.view.*

class ProductListingProgressBar {

    lateinit var dialog: Dialog

    fun show(context: Context?): Dialog {
        context?.apply {
            val view = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater)?.inflate(R.layout.progress_bar, null)
            view?.apply {
                setColorFilter(cp_pbar.indeterminateDrawable, ResourcesCompat.getColor(resources, R.color.black, null)) //Progress Bar Color
            }
            dialog = Dialog(this, R.style.CustomProgressBarTheme)
            dialog.setContentView(view)
            dialog.show()
        }
        return dialog
    }

    fun setColorFilter(@NonNull drawable: Drawable, color: Int) = drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)

}