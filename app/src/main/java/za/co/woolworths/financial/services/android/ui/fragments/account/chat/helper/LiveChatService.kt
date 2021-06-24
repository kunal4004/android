package za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.amplifyframework.AmplifyException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.content.LiveChatNotificationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.content.LiveChatOnStartCommandImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.LiveChatPresenter
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatAuthImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatConversationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatListAllAgentConversationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatSubscribeImpl
import za.co.woolworths.financial.services.android.util.ConnectivityLiveData
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.ReceiverManager
import java.lang.IllegalArgumentException

class LiveChatService : LifecycleService() {

    private var receiverManager: ReceiverManager? = null
    private val liveChatDBRepository = LiveChatDBRepository()
    private var isConnectedToNetwork = true
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        receiverManager?.registerReceiver(
            serviceBroadcastReceiver,
            IntentFilter(CHANNEL_ID)
        )
        createNotificationChannel()
        try {
            connectionDetector()
            ChatAWSAmplify.isLiveChatBackgroundServiceRunning = true
            startLiveChat()
        } catch (ex: IllegalArgumentException) {
            FirebaseManager.logException(ex)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun connectionDetector() {
        ConnectivityLiveData.observe(this, { isConnected ->
            Log.e("isConnectedToNetwork", "$isConnected")
            when (isConnected
                    && ChatAWSAmplify.isLiveChatBackgroundServiceRunning
                    && WoolworthsApplication.getInstance().currentActivity::class != WChatActivity::class) {

                true -> {
                    if (!isConnectedToNetwork) {
                        with(liveChatPresenter) {
                            fetchAllAgentConversation { unreadMessageCount, sendMessageResponse ->
                                sendMessageResponse?.let { item ->
                                    notifySender(
                                        unreadMessageCount,
                                        item
                                    )
                                }
                                onReconnectToSubscribeAPI(this, { item ->
                                    notifySender(1, item)
                                }, {
                                    ChatAWSAmplify.isLiveChatActivated = false
                                    broadcastResultToAmplifySubscribe(applicationContext, null)
                                })
                            }
                        }
                    }
                    isConnectedToNetwork = true
                }
                false -> {
                    isConnectedToNetwork = false
                }
            }
        })
    }

    private fun startLiveChat() {
        with(liveChatPresenter) {
            onStartConversationBySender(this, { item ->
                notifySender(1,item)
            }, { error ->
                GlobalScope.launch(Dispatchers.Main) {
                    when (error) {
                        is AmplifyException -> {
                            // Handshake is corrupted, should resubscribe
                            // onReConnectToSubscribeAPI()
                        }
                        else -> {
                            ChatAWSAmplify.isLiveChatActivated = false
                            broadcastResultToAmplifySubscribe(applicationContext, null)
                        }
                    }
                }
            })
        }
    }

    private fun LiveChatPresenter.notifySender(unreadMessageCount: Int,item: Any) {
        when (item) {
            is SendMessageResponse -> headUpNotification(unreadMessageCount,item, applicationContext)
            else -> broadcastResultToAmplifySubscribe(applicationContext, item as? String)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(
                1,
                liveChatPresenter.createNotificationChannel(applicationContext)?.build()
            )
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