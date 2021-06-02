package za.co.woolworths.financial.services.android.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView

@Suppress("DEPRECATION")
class ServiceTools {

    companion object {

        fun start(activity: Activity?, serviceClass: Class<*>) {
            activity ?: return
            if (!ChatAWSAmplify.isLiveChatBackgroundServiceRunning)
                activity.startService(Intent(activity, serviceClass))
        }

        fun stop(activity: Activity?, serviceClass: Class<*>) {
            activity ?: return
            if (ChatAWSAmplify.isLiveChatBackgroundServiceRunning)
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