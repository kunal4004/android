package za.co.woolworths.financial.services.android.ui.fragments.account.chat.content

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.awfs.coordination.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatService
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_NO_INTERNET_RESULT
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_UNREAD_MESSAGE_COUNT_PACKAGE
import za.co.woolworths.financial.services.android.ui.views.ToastFactory

class LiveChatNotificationImpl : ILiveChatNotification {

    override fun headUpNotification(messageResponse: SendMessageResponse, context: Context?) {
        context ?: return

        if (TextUtils.isEmpty(messageResponse.content))
            return

        GlobalScope.launch(Dispatchers.Main) {
            val woolworthsApplication = context as? WoolworthsApplication
            LiveChatDBRepository().updateUnreadMessageCount()
            ChatAWSAmplify.sessionStateType = messageResponse.sessionState
            broadcastMessageCountResult(woolworthsApplication)
            val currentActivity = woolworthsApplication?.currentActivity
            currentActivity?.let {
                ToastFactory.liveChatHeadUpNotificationWindow(
                    it.window?.decorView?.rootView,
                    it,
                    messageResponse
                )
            }
        }
    }

    override fun broadcastResultToAmplifySubscribe(context: Context?, result: String?) {
        val postChatDataIntent = Intent()
        postChatDataIntent.action = ChatFloatingActionButtonBubbleView.LIVE_CHAT_PACKAGE
        postChatDataIntent.putExtra(
            ChatFloatingActionButtonBubbleView.LIVE_CHAT_SUBSCRIPTION_RESULT,
            result
        )
        context?.sendBroadcast(postChatDataIntent)

    }

    override fun broadcastMessageCountResult(context: Context?) {
        context?.sendBroadcast(Intent(LIVE_CHAT_UNREAD_MESSAGE_COUNT_PACKAGE))
    }

    override fun broadcastResultShowNoConnectionToast(context: Context?) {
        val postChatDataIntent = Intent()
        postChatDataIntent.action = ChatFloatingActionButtonBubbleView.LIVE_CHAT_PACKAGE
        postChatDataIntent.putExtra(
            LIVE_CHAT_NO_INTERNET_RESULT,
            LIVE_CHAT_NO_INTERNET_RESULT
        )
        context?.sendBroadcast(postChatDataIntent)
    }

    override fun createNotificationChannel(context: Context?): NotificationCompat.Builder? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                LiveChatService.CHANNEL_ID, bindString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )
            serviceChannel.enableVibration(false)
            val manager = context?.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)

            val notificationIntent = Intent()
            val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
            return context?.let {
                NotificationCompat.Builder(it, LiveChatService.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    //.setContentTitle(bindString(R.string.app_name))
                    .setContentText(bindString(R.string.woolies_chat_active))
                    .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
                    .setVibrate(null) // Passing null here silently fails
                    .setContentIntent(pendingIntent)
            }
        }
        return null
    }
}