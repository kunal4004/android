package za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify.signInAndSubscribe
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatFloatingActionButtonBubbleView


class ChatFollowMeService : Service() {

    private val liveChatDBRepository: LiveChatDBRepository = LiveChatDBRepository()

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createNotificationChannel()

        val postChatDataIntent = Intent()

        signInAndSubscribe({ result: SendMessageResponse? ->
            postChatDataIntent.action = ChatFloatingActionButtonBubbleView.LIVE_CHAT_PACKAGE
            postChatDataIntent.putExtra(ChatFloatingActionButtonBubbleView.LIVE_CHAT_SUBSCRIPTION_RESULT, Gson().toJson(result))
            sendBroadcast(postChatDataIntent)
            null
        }
        ) {
            postChatDataIntent.action = ChatFloatingActionButtonBubbleView.LIVE_CHAT_PACKAGE
            postChatDataIntent.putExtra(ChatFloatingActionButtonBubbleView.LIVE_CHAT_SUBSCRIPTION_RESULT, "")
            sendBroadcast(postChatDataIntent)
            null
        }
        return START_STICKY
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            )
            serviceChannel.enableVibration(false)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)

            val notificationIntent = Intent(this, WChatActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
            val notification: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("OneApp")
                    .setContentText("Woolworth's Service")
                    .setSmallIcon(R.drawable.method_woolworths)
                    .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
                    .setVibrate(null) // Passing null here silently fails
                    .setContentIntent(pendingIntent)
            startForeground(1, notification.build())
        }
    }
}