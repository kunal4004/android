package za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.LiveChat
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatAuthImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatConversationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatListAllAgentConversationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatSubscribeImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_NO_INTERNET_RESULT
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_PACKAGE
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_SUBSCRIPTION_RESULT
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView.Companion.LIVE_CHAT_UNREAD_MESSAGE_COUNT_PACKAGE
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.ReceiverManager
import za.co.woolworths.financial.services.android.util.animation.ConnectivityWatcher
import java.lang.IllegalArgumentException

class LiveChatService : Service() {

    private var receiverManager: ReceiverManager? = null
    private val liveChatDBRepository = LiveChatDBRepository()
    private var isConnectedToInternet = true
    private val liveChat = LiveChat(
        LiveChatAuthImpl(),
        LiveChatConversationImpl(),
        LiveChatSubscribeImpl(SessionStateType.CONNECT, "Hi"),
        LiveChatListAllAgentConversationImpl()
    )

    override fun onCreate() {
        super.onCreate()
        receiverManager = ReceiverManager.init(this)
    }

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        receiverManager?.registerReceiver(
            serviceBroadcastReceiver,
            IntentFilter(CHANNEL_ID)
        )
        createNotificationChannel()
        try {
            ChatAWSAmplify.isLiveChatBackgroundServiceRunning = true
            startLiveChat()
        } catch (ex: IllegalArgumentException) {
            FirebaseManager.logException(ex)
        }

        return START_STICKY
    }

    private fun startLiveChat() {
        with(liveChat) {
            signIn({
                //sign in success
                ChatAWSAmplify.isLiveChatActivated = true
                conversation({
                    // conversation success
                    onSubscribe({ message ->
                        if (message?.sessionState != SessionStateType.CONNECT || !TextUtils.isEmpty(
                                message.content
                            )
                        )
                            message?.let { msg -> ChatAWSAmplify.addChatMessageToList(msg) }

                        if (ChatAWSAmplify.isChatActivityInForeground) {
                            postResult(Gson().toJson(message))
                        } else {
                            displayHeadUpMessage(message)
                        }

                    }, { apiException ->
                        ChatAWSAmplify.isLiveChatActivated = false
                        Log.e("authLogin", "apiException subscribe ${Gson().toJson(apiException)}")
                    })

                }, { apiException ->
                    // conversation failure
                    ChatAWSAmplify.isLiveChatActivated = false
                    Log.e("authLogin", "apiException conversation ${Gson().toJson(apiException)}")

                })
            }, { authException ->
                //sign in failure
                ChatAWSAmplify.isLiveChatActivated = false
                Log.e("authLogin", "authException signIn ${Gson().toJson(authException)}")
            })
        }
    }

    private fun displayHeadUpMessage(message: SendMessageResponse?) {
        if (!TextUtils.isEmpty(message?.content)) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                val woolworthsApplication =
                    applicationContext as? WoolworthsApplication
                liveChatDBRepository.updateUnreadMessageCount()
                ChatAWSAmplify.sessionStateType = message?.sessionState
                postMessageCount()
                val currentActivity = woolworthsApplication?.currentActivity
                currentActivity?.let {
                    ToastFactory.chatFollowMeBubble(
                        it.window?.decorView?.rootView,
                        it,
                        message
                    )
                }
            }
        }
    }

    private fun postResult(result: String?) {
        val postChatDataIntent = Intent()
        postChatDataIntent.action = LIVE_CHAT_PACKAGE
        postChatDataIntent.putExtra(LIVE_CHAT_SUBSCRIPTION_RESULT, result)
        sendBroadcast(postChatDataIntent)
    }

    private fun postResultShowNoInternetToast() {
        val postChatDataIntent = Intent()
        postChatDataIntent.action = LIVE_CHAT_PACKAGE
        postChatDataIntent.putExtra(LIVE_CHAT_NO_INTERNET_RESULT, LIVE_CHAT_NO_INTERNET_RESULT)
        sendBroadcast(postChatDataIntent)
    }

    private fun postMessageCount() {
        val postMessageCountIntent = Intent(LIVE_CHAT_UNREAD_MESSAGE_COUNT_PACKAGE)
        sendBroadcast(postMessageCountIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, bindString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )
            serviceChannel.enableVibration(false)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)

            val notificationIntent = Intent()
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
            val notification: NotificationCompat.Builder =
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    //.setContentTitle(bindString(R.string.app_name))
                    //.setContentText("Woolworth's Service")
                    .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
                    .setVibrate(null) // Passing null here silently fails
                    .setContentIntent(pendingIntent)
            startForeground(1, notification.build())
        }
    }

    override fun onDestroy() {
        ChatAWSAmplify.isLiveChatActivated = false
        ChatAWSAmplify.listAllChatMessages?.clear()
        ChatAWSAmplify.isLiveChatBackgroundServiceRunning = false
        ChatAWSAmplify.sessionStateType = null
        liveChatDBRepository.resetUnReadMessageCount()
        liveChat.onCancel()
        super.onDestroy()
    }

    var serviceBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (ChatAWSAmplify.isLiveChatBackgroundServiceRunning) {
                ChatAWSAmplify.sessionStateType = null
                ChatAWSAmplify.isLiveChatBackgroundServiceRunning = false
                unregisterReceiver()
                stopSelf()
            }
        }
    }

    // Unregister since the activity is not visible
    private fun unregisterReceiver() {
        try {
            receiverManager?.unregisterReceiver(serviceBroadcastReceiver)
        } catch (ex: IllegalArgumentException) {
            FirebaseManager.logException("unregisterReceiver serviceBroadcastReceiver $ex")
        }
    }
}