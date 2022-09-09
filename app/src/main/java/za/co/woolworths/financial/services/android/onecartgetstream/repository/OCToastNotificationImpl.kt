package za.co.woolworths.financial.services.android.onecartgetstream.repository

import android.app.Activity
import android.view.Gravity
import android.widget.Toast
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.oc_chat_toast_notification.view.*
import kotlinx.android.synthetic.main.single_line_common_toast.*

import javax.inject.Inject

class OCToastNotificationImpl @Inject constructor(

) : OCToastNotification {
    override fun showOCToastNotification(
        context: Activity,
        messageCount: String,
        yOffset: Int,
        orderId: String,
    ) {
        val layout = context.layoutInflater.inflate(
            R.layout.oc_chat_toast_notification,
            context.mainCommonToastLayout)
        layout?.ocMessageCount?.text = messageCount
        layout?.setOnClickListener {
            //TODO: chat
        }

        Toast(context).apply {
            duration = Toast.LENGTH_LONG
            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, yOffset)
            view = layout
            show()
        }
    }
}
