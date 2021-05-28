package za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.content.LiveChatNotificationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.content.LiveChatOnStartCommandImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.LiveChatPresenter
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatAuthImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatConversationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatListAllAgentConversationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatSubscribeImpl
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.ReceiverManager
import java.lang.IllegalArgumentException

class LiveChatService : Service() {

    private var receiverManager: ReceiverManager? = null
    private val liveChatDBRepository = LiveChatDBRepository()
    private val liveChatPresenter = LiveChatPresenter(
        LiveChatAuthImpl(),
        LiveChatConversationImpl(),
        LiveChatSubscribeImpl(SessionStateType.CONNECT, "Hi"),
        LiveChatListAllAgentConversationImpl(),
        LiveChatOnStartCommandImpl(),
        LiveChatNotificationImpl()
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
        with(liveChatPresenter) {
            onStartConversationBySender(this, { item ->
                when (item) {
                    is SendMessageResponse -> headUpNotification(item, applicationContext)
                    else -> broadcastResultToAmplifySubscribe(applicationContext, item as? String)
                }
            }, {
                GlobalScope.launch(Dispatchers.Main) {
                    ChatAWSAmplify.isLiveChatActivated = false
                    broadcastResultToAmplifySubscribe(applicationContext, null)
                }
            })
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, liveChatPresenter.createNotificationChannel(applicationContext)?.build())
        }
    }

    override fun onDestroy() {
        ChatAWSAmplify.isLiveChatActivated = false
        ChatAWSAmplify.listAllChatMessages?.clear()
        ChatAWSAmplify.isLiveChatBackgroundServiceRunning = false
        ChatAWSAmplify.sessionStateType = null
        liveChatDBRepository.resetUnReadMessageCount()
        liveChatPresenter.onCancel()
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

    private fun unregisterReceiver() {
        try {
            receiverManager?.unregisterReceiver(serviceBroadcastReceiver)
        } catch (ex: IllegalArgumentException) {
            FirebaseManager.logException("unregisterReceiver serviceBroadcastReceiver $ex")
        }
    }
}