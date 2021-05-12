package za.co.woolworths.financial.services.android.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView

@Suppress("DEPRECATION")
class ServiceTools {

    companion object {
        /**
         * Check if the service is Running
         * @param serviceClass the class of the Service
         * @return true if the service is running otherwise false
         */
        fun checkServiceRunning(activity: Activity?, serviceClass: Class<*>): Boolean {
            val manager = activity?.getSystemService(ACTIVITY_SERVICE) as? ActivityManager
            val runningService = manager?.getRunningServices(Int.MAX_VALUE)
            if (runningService != null) {
                for (service in runningService) {
                    if (serviceClass.name == service.service.className) {
                        return true
                    }
                }
            }
            return false
        }

        fun start(activity: Activity?, serviceClass: Class<*>) {
            activity ?: return
            if (!checkServiceRunning(activity, serviceClass))
                activity.startService(Intent(activity, serviceClass))
        }

        fun stop(activity: Activity?, serviceClass: Class<*>) {
            activity ?: return
            if (checkServiceRunning(activity, serviceClass))
                activity.stopService(Intent(activity, serviceClass))
        }

        fun postResult(context: Context?, action: String, result: String?) {
            val postChatDataIntent = Intent()
            postChatDataIntent.action = action
            postChatDataIntent.putExtra(
                ChatFloatingActionButtonBubbleView.LIVE_CHAT_SUBSCRIPTION_RESULT,
                result
            )
            context?.sendBroadcast(postChatDataIntent)
        }
    }
}