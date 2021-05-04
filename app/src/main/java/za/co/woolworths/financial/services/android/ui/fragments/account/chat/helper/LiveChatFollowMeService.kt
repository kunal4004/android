package za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.LiveChat
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatAuthImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatConversationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatListAllAgentConversationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatSubscribeImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_PACKAGE
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_SUBSCRIPTION_RESULT

class LiveChatFollowMeService : Service() {

    private val liveChat = LiveChat(LiveChatAuthImpl(),
            LiveChatConversationImpl(),
            LiveChatSubscribeImpl(SessionStateType.CONNECT, "Hi"),
            LiveChatListAllAgentConversationImpl())

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createNotificationChannel()
        startLiveChat()

        return START_STICKY
    }

    private fun startLiveChat() {
        with(liveChat) {
            signIn({
                //sign in success
                conversation({
                    // conversation success
                    onSubscribe({ message ->
                        postResult(Gson().toJson(message))

                    }, { apiException ->
                        Log.e("authLogin", "apiException subscribe ${Gson().toJson(apiException)}")
                    })

                }, { apiException ->
                    // conversation failure
                    Log.e("authLogin", "apiException conversation ${Gson().toJson(apiException)}")

                })
            }, { authException ->
                //sign in failure
                Log.e("authLogin", "authException signIn ${Gson().toJson(authException)}")
            })
        }
    }

    private fun postResult(result: String?) {
        val postChatDataIntent = Intent()
        postChatDataIntent.action = LIVE_CHAT_PACKAGE
        postChatDataIntent.putExtra(LIVE_CHAT_SUBSCRIPTION_RESULT, result)
        sendBroadcast(postChatDataIntent)
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

    override fun onDestroy() {
        liveChat.onCancel()
        super.onDestroy()
    }
}