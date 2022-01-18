package za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet

import android.app.Activity
import android.view.Gravity
import android.widget.Toast
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.vto_saved_photo_toast.*
import javax.inject.Inject

class VtoSavedPhotoToastImpl @Inject constructor(

) : VtoSavedPhotoToast {

    override fun showSavedPhotoToast(context: Activity) {

        val layout = context.layoutInflater.inflate(
            R.layout.vto_saved_photo_toast,
            context.vtoSavedPhotoLayout)

        Toast(context).apply {
            duration = Toast.LENGTH_LONG
            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 300)
            view = layout
            show()
        }

    }
}