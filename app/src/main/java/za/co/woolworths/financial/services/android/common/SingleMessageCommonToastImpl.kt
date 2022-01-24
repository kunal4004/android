package za.co.woolworths.financial.services.android.common

import android.app.Activity
import android.view.Gravity
import android.widget.Toast
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.single_line_common_toast.*
import kotlinx.android.synthetic.main.single_line_common_toast.view.*

import javax.inject.Inject

class SingleMessageCommonToastImpl @Inject constructor(

) : SingleMessageCommonToast {

    override fun showMessage(context: Activity, mesage: String,yOffset:Int) {

        val layout = context.layoutInflater.inflate(
            R.layout.single_line_common_toast,
            context.mainCommonToastLayout)
         layout?.toastMessage?.text = mesage

        Toast(context).apply {
            duration = Toast.LENGTH_LONG
            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, yOffset)
            view = layout
            show()
        }

    }
}