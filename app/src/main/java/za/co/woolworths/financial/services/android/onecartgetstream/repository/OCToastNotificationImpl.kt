package za.co.woolworths.financial.services.android.onecartgetstream.repository

import android.app.Activity
import android.graphics.Color
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.awfs.coordination.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.oc_chat_toast_notification.view.*
import kotlinx.android.synthetic.main.single_line_common_toast.*
import za.co.woolworths.financial.services.android.onecartgetstream.OCChatActivity
import za.co.woolworths.financial.services.android.onecartgetstream.common.constant.OCConstant
import za.co.woolworths.financial.services.android.onecartgetstream.service.UpdateMessageCount
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.AppConstant

import javax.inject.Inject



class OCToastNotificationImpl @Inject constructor(

) : OCToastNotification {
    private var updateMessageCount = 0
    override fun showOCToastNotification(
        context: Activity,
        messageCount: String,
        yOffset: Int,
        orderId: String,
    ) {

        showOCChatNotificationOnScreen(context,orderId)
    }

    private fun showOCChatNotificationOnScreen(context: Activity, orderId: String) {
        val layout = context.layoutInflater.inflate(
            R.layout.oc_chat_toast_notification,
            context.mainCommonToastLayout)
        val snackbar =
            Snackbar.make(context.findViewById(android.R.id.content), "",AppConstant.DELAY_20000_MS.toInt())
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
            snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        if (context is BottomNavigationActivity) {
            val bottomNavView: View = context.findViewById(R.id.bottom_line)
            snackbar.anchorView = bottomNavView
            snackbarLayout.setPadding(20, 0, 20, 50)
        } else {
            snackbarLayout.setPadding(20, 0, 20, 250)
        }
        layout?.ocChatLayout?.setOnClickListener {
            OCConstant.ocObserveCountMessage = 0
            UpdateMessageCount.value = 0
            snackbar.dismiss()
            snackbarLayout.removeAllViews()
            context.startActivity(OCChatActivity.newIntent(context, orderId))
        }
        UpdateMessageCount.observe(context as LifecycleOwner) {
            updateMessageCount = it
            layout?.ocMessageCount?.text = it?.toString()
        }
        if (!snackbar.isShown) {
            snackbarLayout.addView(layout, 0)
            snackbar.show()
        }

    }

}
