package za.co.woolworths.financial.services.android.onecartgetstream.repository

import android.app.Activity
import android.graphics.Color
import com.awfs.coordination.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.oc_chat_toast_notification.view.*
import kotlinx.android.synthetic.main.single_line_common_toast.*
import za.co.woolworths.financial.services.android.onecartgetstream.OCChatActivity
import za.co.woolworths.financial.services.android.onecartgetstream.common.constant.OCConstant

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

        val snackbar =
            Snackbar.make(context.findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG)
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        layout?.ocMessageCount?.text = messageCount
        layout?.ocChatLayout?.setOnClickListener {
            OCConstant.ocChatMessageCount = 0
            context.startActivity(OCChatActivity.newIntent(context, orderId))
            snackbar.dismiss()
            snackbarLayout.removeAllViews()
        }
        snackbarLayout.setPadding(20, 0, 20, 250)
        snackbarLayout.addView(layout, 0)
        snackbar.show()

    }
}
