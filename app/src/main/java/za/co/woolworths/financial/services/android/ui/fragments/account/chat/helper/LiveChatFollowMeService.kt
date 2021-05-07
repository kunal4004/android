package za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.LiveChat
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatAuthImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatConversationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatListAllAgentConversationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatSubscribeImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_PACKAGE
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_SUBSCRIPTION_RESULT
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.UNREAD_MESSAGE_COUNT


class LiveChatFollowMeService : Service() {

    private val liveChatDBRepository = LiveChatDBRepository()
    private val liveChat = LiveChat(
        LiveChatAuthImpl(),
        LiveChatConversationImpl(),
        LiveChatSubscribeImpl(SessionStateType.CONNECT, "Hi"),
        LiveChatListAllAgentConversationImpl()
    )

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
                        if (ChatAWSAmplify.isChatActivityInForeground) {
                            postResult(Gson().toJson(message))
                            sendNotification()
                        } else {
                            val handler = Handler(Looper.getMainLooper())
                            handler.post {
                                liveChatDBRepository.updateUnreadMessageCount()
                                postMessageCount()
                                Toast.makeText(
                                    applicationContext,
                                    message?.content ?: "N/A",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

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

    private fun postMessageCount() {
        val postMessageCount = Intent()
        postMessageCount.action = UNREAD_MESSAGE_COUNT
        sendBroadcast(postMessageCount)
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
            val notification: NotificationCompat.Builder =
                NotificationCompat.Builder(this, CHANNEL_ID)
                    // .setContentTitle("")
                    // .setContentText("Woolworth's Service")
                    // .setSmallIcon(R.drawable.appicon)
                    .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
                    .setVibrate(null) // Passing null here silently fails
                    .setContentIntent(pendingIntent)
            startForeground(1, notification.build())
        }
    }

    override fun onDestroy() {
        ChatAWSAmplify.listAllChatMessages?.clear()
        liveChat.onCancel()
        super.onDestroy()
    }

    private fun sendNotification() {
        var notifyManager: NotificationManager? = null
        val NOTIFY_ID = 1002

        val name = "KotlinApplication"
        val id = "kotlin_app"
        val description = "kotlin_app_first_channel"

        if (notifyManager == null) {
            notifyManager = applicationContext?.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            var mChannel = notifyManager.getNotificationChannel(id)
            if (mChannel == null) {
                mChannel = NotificationChannel(id, name, importance)
                mChannel.description = description
                mChannel.enableVibration(true)
                mChannel.lightColor = Color.GREEN
                mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                notifyManager.createNotificationChannel(mChannel)
            }
        }

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, id)

        val intent: Intent = Intent(applicationContext, WChatActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, 0)

        builder.setContentTitle("Heads Up Notification")  // required
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
            .setContentText(getString(R.string.app_name))  // required
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setTicker("Notification")
            .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))

        val dismissIntent = Intent(applicationContext, WChatActivity::class.java)
        dismissIntent.action = "DISMISS"
        dismissIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingDismissIntent = PendingIntent.getActivity(
            applicationContext, 0, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val dismissAction = NotificationCompat.Action(
            R.drawable.auto_icon,
            "DISMISS", pendingDismissIntent
        )
        builder.addAction(dismissAction)

        val notification = builder.build()
        notifyManager.notify(NOTIFY_ID, notification)
    }
}