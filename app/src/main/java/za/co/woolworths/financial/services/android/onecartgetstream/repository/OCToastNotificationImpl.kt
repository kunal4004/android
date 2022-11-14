package za.co.woolworths.financial.services.android.onecartgetstream.repository

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.awfs.coordination.R
import com.awfs.coordination.databinding.OcChatToastNotificationBinding
import com.google.android.material.snackbar.Snackbar
import za.co.woolworths.financial.services.android.onecartgetstream.OCChatActivity
import za.co.woolworths.financial.services.android.onecartgetstream.common.constant.OCConstant
import za.co.woolworths.financial.services.android.onecartgetstream.service.UpdateMessageCount
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.AppConstant

class OCToastNotificationImpl : OCToastNotification {

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
        val binding = OcChatToastNotificationBinding.inflate(LayoutInflater.from(context), null, false)
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
        binding.ocChatLayout?.setOnClickListener {
            OCConstant.ocObserveCountMessage = 0
            UpdateMessageCount.value = 0
            snackbar.dismiss()
            snackbarLayout.removeAllViews()
            context.startActivity(OCChatActivity.newIntent(context, orderId))
        }
        UpdateMessageCount.observe(context as LifecycleOwner) {
            updateMessageCount = it
            binding.ocMessageCount?.text = it?.toString()
        }
        if (!snackbar.isShown) {
            snackbarLayout.addView(binding.root, 0)
            snackbar.show()
        }
    }

}
