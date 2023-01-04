package za.co.woolworths.financial.services.android.onecartgetstream.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.pushprovider.huawei.HuaweiPushDeviceGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Order
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.OrderSummary
import za.co.woolworths.financial.services.android.models.dto.OrdersResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.onecartgetstream.OCChatActivity
import za.co.woolworths.financial.services.android.onecartgetstream.common.constant.OCConstant.Companion.ORDER_PENDING_PICKING
import za.co.woolworths.financial.services.android.onecartgetstream.common.constant.OCConstant.Companion.isOCChatBackgroundServiceRunning
import za.co.woolworths.financial.services.android.onecartgetstream.model.OCAuthenticationResponse
import za.co.woolworths.financial.services.android.onecartgetstream.repository.OCToastNotification
import za.co.woolworths.financial.services.android.receivers.DashOrderReceiver
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@AndroidEntryPoint
class DashChatMessageListeningService : LifecycleService(), ChatEventListener<NewMessageEvent> {

    private lateinit var chatClient: ChatClient
    // Controllers for observing data changes within the channel, where key is orderId
    private var chatChannelClients = HashMap<String, ChannelClient>()
    private var ordersSummary = ArrayList<OrderSummary>()
    private var channelIdToOrderIdMap = HashMap<String, String>()
    private var isServiceRunning = true
    @Inject
    lateinit var ocToastNotification: OCToastNotification

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isServiceRunning){
            lifecycleScope.launch(Dispatchers.IO) {
                isOCChatBackgroundServiceRunning = true
                isServiceRunning = false
                connectUserAndListenToChannels()
                createNotificationChannel(applicationContext)?.build()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel(context: Context?): NotificationCompat.Builder? {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, bindString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )
            serviceChannel.enableVibration(false)
            val manager = context?.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)

            val notificationIntent = Intent()
            val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
            val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, pendingIntentFlag)
            return context?.let {
                NotificationCompat.Builder(it, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentText(bindString(R.string.woolies_chat_active))
                    .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
                    .setVibrate(null) // Passing null here silently fails
                    .setContentIntent(pendingIntent)
            }
        }
        return null
    }

    // Scenario A: service is started on app launch; user adds to cart, checkout and make payment; that order goes to pending_picking state and shopper initiates a chat with this user - this would mean the service is not listening to this new channel
    // Scenario B: Same as above, except there's no channel for the service to listen to, which means it will stop on launch itself. When new order's channel is opened, service needs to be started and listen to that new channel.
    private fun connectUserAndListenToChannels() {
        chatClient = getOneCartStreamChatClient(this)
        authenticateOneCart(
            onSuccess = { userId, displayName, token ->
                connectUser(
                    userId,
                    displayName,
                    token,
                    onSuccess = {
                        fetchOrdersPendingPicking { pendingOrders ->
                            if (!pendingOrders.isNullOrEmpty()) {
                                var countOrderDetailsRemaining = pendingOrders.size
                                ordersSummary = ArrayList()
                                channelIdToOrderIdMap = HashMap()

                                val fnGetChannelForOrders = {
                                    // Cache ordersSummary for use during deep-linking
                                    Utils.setCachedOrdersPendingPicking(ordersSummary.toTypedArray())

                                    fetchChannels(
                                        chatClient,
                                        onSuccess = { channels ->
                                            if (channels.isEmpty()) {
                                                killService()
                                                return@fetchChannels
                                            }

                                            channels.forEach { channel ->
                                                getRecipientChannelMember(
                                                    chatClient,
                                                    channel.cid,
                                                    onSuccess = { member ->
                                                        ordersSummary.firstOrNull { !it.shopperId.isNullOrEmpty() && member.id.contains(it.shopperId!!) }?.let { orderForChannel ->
                                                            orderForChannel.orderId?.let { orderId ->
                                                                channelIdToOrderIdMap[channel.cid] = orderId ?: ""
                                                                // Append channel and start listening to it through delegate
                                                                getChatChannelClient(orderId, channel.cid)
                                                            }
                                                        }
                                                    },
                                                    onFailure = {
                                                        // Ignored for now
                                                    }
                                                )
                                            }
                                        },
                                        onFailure = {
                                            killService()
                                        }
                                    )
                                }

                                pendingOrders.forEach { order ->
                                    fetchOrderDetails(
                                        order.orderId,
                                        onSuccess = { orderSummary ->
                                            ordersSummary.add(orderSummary)
                                            countOrderDetailsRemaining -= 1
                                            if (countOrderDetailsRemaining == 0) {
                                                fnGetChannelForOrders()
                                            }
                                        },
                                        onFailure = {
                                            countOrderDetailsRemaining -= 1
                                            if (countOrderDetailsRemaining == 0) {
                                                fnGetChannelForOrders()
                                            }
                                        }
                                    )
                                }

                            } else {
                                killService()
                            }
                        }
                    },
                    onFailure = {
                        killService()
                    }
                )
            },
            onFailure = {
                killService()
            }
        )
    }

    private fun getChatChannelClient(orderId: String, channelId: String): ChannelClient {
        getChatChannelClient(orderId)?.let { return it } ?: kotlin.run {
            val channelClient = chatClient.channel(channelId)
            appendChatChannelClient(orderId, channelClient)
            return channelClient
        }
    }

    private fun getChatChannelClient(orderId: String): ChannelClient? = chatChannelClients.get(orderId)

    private fun appendChatChannelClient(orderId: String, channelClient: ChannelClient) {
        chatChannelClients[orderId] = channelClient
        channelClient.subscribeFor(this)
    }

    private fun killService() {
        stopSelf()
    }

    override fun onEvent(event: NewMessageEvent) {
        chatClient.getCurrentUser()?.let { currentUser ->
            if (currentUser.id != event.user.id) {
                // Incoming message from other channel member
                val channelId = event.cid
                val orderId = channelIdToOrderIdMap[event.cid]
                val orderSummary = ordersSummary.firstOrNull { it.orderId == orderId }

                sendBroadCastEvent(event.totalUnreadCount)
                //TODO:
                /*
                  Hiding Toast as per requirement. currently not needed.
                   if again requirement come will enable.
                   if not needed.... need to remove all OC chat Toast implementation.
                 */

//                if (WoolworthsApplication.getInstance().currentActivity != null &&
//                    WoolworthsApplication.getInstance().currentActivity::class != OCChatActivity::class
//                ) {
//                    UpdateMessageCount.value = ++ocObserveCountMessage
//                        GlobalScope.launch(Dispatchers.Main) {
//                            val woolworthsApplication = WoolworthsApplication.getInstance()
//                            woolworthsApplication?.currentActivity?.let {
//                                it.window?.decorView?.rootView?.apply {
//                                    orderId?.let { orderID ->
//                                        ocToastNotification.showOCToastNotification(it,
//                                            "0",
//                                            250,
//                                            orderID)
//                                    }
//                                }
//                            }
//                    }
//
//                }

            }
        }
    }


    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannelId"

        fun getUnreadMessageForOrder(context: Context, orderId: String) {
            val chatClient = getOneCartStreamChatClient(context)
            authenticateOneCart(onSuccess = { userId, displayName, token ->
                onAuthenticationSuccess(userId, displayName, token, chatClient, orderId)
            },
                onFailure = {}
            )
        }
        
        private fun onAuthenticationSuccess(
            userId: String,
            displayName: String,
            token: String,
            chatClient: ChatClient,
            orderId: String
        ) {
            connectUser(userId, displayName, token, onSuccess = {
                onConnectUserSuccess(chatClient, orderId)
            },
                onFailure = {}
            )
        }

        private fun onConnectUserSuccess(chatClient: ChatClient, orderId: String) {
            fetchOrderDetails(
                orderId,
                onSuccess = { orderSummary ->
                    onFetchOrderDetailsSuccess(chatClient, orderSummary.shopperId)
                },
                onFailure = {}
            )
        }

        private fun onFetchOrderDetailsSuccess(chatClient: ChatClient, shopperId: String?) {
            fetchChannels(
                chatClient,
                onSuccess = { channels ->
                    if (channels.isEmpty()) {
                        return@fetchChannels
                    }
                    onFetchChannelsSuccess(channels, chatClient, shopperId)
                },
                onFailure = {}
            )
        }

        private fun onFetchChannelsSuccess(
            channels: List<Channel>,
            chatClient: ChatClient,
            shopperId: String?
        ) {
            channels.forEach { channel ->
                getRecipientChannelMember(
                    chatClient,
                    channel.cid,
                    onSuccess = { member ->
                        shopperId?.let {
                            if (member.id.contains(it)) {
                                queryChannelRequestForUnreadCount(chatClient, channel)
                            }
                        }
                    },
                    onFailure = {
                        // Ignored for now
                    }
                )
            }
        }

        private fun queryChannelRequestForUnreadCount(chatClient: ChatClient, channel: Channel) {
            // Get channel
            val queryChannelRequest =
                QueryChannelRequest().withState()
            chatClient.queryChannel(
                channel.type,
                channel.id,
                queryChannelRequest
            ).enqueue { result ->
                if (result.isSuccess) {
                    // Unread count for current user
                    val unreadCount: Int = result.data().unreadCount ?: 0
                    sendBroadCastEvent(unreadCount)
                }
            }
        }

        fun sendBroadCastEvent(totalUnreadCount: Int) {

            if (WoolworthsApplication.getInstance().currentActivity != null &&
                WoolworthsApplication.getInstance().currentActivity::class != OCChatActivity::class
            ) {
                WoolworthsApplication.getInstance()?.currentActivity?.let {
                    val broadCastIntent = Intent()
                    broadCastIntent.action = DashOrderReceiver.ACTION_LAST_DASH_ORDER
                    broadCastIntent.putExtra(DashOrderReceiver.EXTRA_UNREAD_MESSAGE_COUNT, totalUnreadCount)
                    LocalBroadcastManager.getInstance(it).sendBroadcast(broadCastIntent)
                }
            }
        }

        fun getChannelForOrder(context: Context, orderId: String, onSuccess: (Channel) -> Unit, onFailure: () -> Unit) {
            val chatClient = getOneCartStreamChatClient(context)
            authenticateOneCart(
                onSuccess = { userId, displayName, token ->
                    connectUser(
                        userId,
                        displayName,
                        token,
                        onSuccess = {
                            fetchOrderDetails(
                                orderId,
                                onSuccess = { orderSummary ->
                                    fetchChannels(
                                        chatClient,
                                        onSuccess = { channels ->
                                            if (channels.isEmpty()) {
                                                onFailure()
                                                return@fetchChannels
                                            }

                                            channels.forEach { channel ->
                                                getRecipientChannelMember(
                                                    chatClient,
                                                    channel.cid,
                                                    onSuccess = { member ->
                                                        if (!orderSummary.shopperId.isNullOrEmpty() && member.id.contains(orderSummary.shopperId!!)) {
                                                            onSuccess(channel)
                                                        }
                                                    },
                                                    onFailure = {
                                                        // Ignored for now
                                                    }
                                                )
                                            }
                                        },
                                        onFailure = {
                                            onFailure()
                                        }
                                    )
                                },
                                onFailure
                            )
                        },
                        onFailure = {
                            onFailure()
                        }
                    )
                },
                onFailure = {
                    onFailure()
                }
            )
        }

        fun getOrderIdForChannel(context: Context, channelId: String, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
            val chatClient = getOneCartStreamChatClient(context)
            authenticateOneCart(
                onSuccess = { userId, displayName, token ->
                    connectUser(
                        userId,
                        displayName,
                        token,
                        onSuccess = {
                            val fnGetOrderIdFromServerOrders = {
                                fetchOrdersPendingPicking { pendingOrders ->
                                    if (!pendingOrders.isNullOrEmpty()) {
                                        var countOrderDetailsRemaining = pendingOrders.size
                                        var ordersSummary = ArrayList<OrderSummary>()

                                        val fnGetChannelForOrders = {
                                            // Cache ordersSummary for use during deep-linking
                                            Utils.setCachedOrdersPendingPicking(ordersSummary.toTypedArray())

                                            getRecipientChannelMember(
                                                chatClient,
                                                channelId,
                                                onSuccess = { member ->
                                                    ordersSummary.firstOrNull { !it.shopperId.isNullOrEmpty() && member.id.contains(it.shopperId!!) }?.let { orderForChannel ->
                                                        orderForChannel.orderId?.let { orderId ->
                                                            onSuccess(orderId)
                                                        } ?: kotlin.run {
                                                            onFailure()
                                                        }
                                                    }
                                                },
                                                onFailure = {
                                                    onFailure()
                                                }
                                            )
                                        }

                                        pendingOrders.forEach { order ->
                                            fetchOrderDetails(
                                                order.orderId,
                                                onSuccess = { orderSummary ->
                                                    ordersSummary.add(orderSummary)
                                                    countOrderDetailsRemaining -= 1
                                                    if (countOrderDetailsRemaining == 0) {
                                                        fnGetChannelForOrders()
                                                    }
                                                },
                                                onFailure = {
                                                    countOrderDetailsRemaining -= 1
                                                    if (countOrderDetailsRemaining == 0) {
                                                        fnGetChannelForOrders()
                                                    }
                                                }
                                            )
                                        }

                                    } else {
                                        // This would mean that a push notification was received, but no order matched that notification's data
                                        onFailure()
                                    }
                                }
                            }

                            // Check cached orders first
                            val cachedOrderSummaryPendingPicking = Utils.getCachedOrdersPendingPicking()
                            if (!cachedOrderSummaryPendingPicking.isNullOrEmpty()) {
                                getRecipientChannelMember(
                                    chatClient,
                                    channelId,
                                    onSuccess = { member ->
                                        cachedOrderSummaryPendingPicking.firstOrNull {
                                            !it.shopperId.isNullOrEmpty() && member.id.contains(
                                                it.shopperId!!
                                            )
                                        }?.let { orderForChannel ->
                                            orderForChannel.orderId?.let { orderId ->
                                                onSuccess(orderId)
                                            } ?: kotlin.run {
                                                onFailure()
                                            }
                                        } ?: kotlin.run {
                                            // Order not found in cached data, let's find it from server instead
                                            fnGetOrderIdFromServerOrders()
                                        }
                                    },
                                    onFailure = {
                                        onFailure()
                                    }
                                )
                            } else {
                                // No cached order, let's find it from server instead
                                fnGetOrderIdFromServerOrders()
                            }
                        },
                        onFailure = {
                            onFailure()
                        }
                    )
                },
                onFailure = {
                    onFailure()
                }
            )
        }

        private fun getOneCartStreamChatClient(context: Context): ChatClient {
            val notificationConfig = NotificationConfig(
                pushDeviceGenerators = listOf(
                    if (Utils.isGooglePlayServicesAvailable())
                        FirebasePushDeviceGenerator()
                    else
                        HuaweiPushDeviceGenerator(
                            WoolworthsApplication.getAppContext(),
                            appId = context.getString(R.string.huawei_app_id).replace("appid=", "")
                        )
                )
            )

            val chatClient = ChatClient.Builder(AppConfigSingleton.dashConfig?.inAppChat?.apiKey.toString(), WoolworthsApplication.getAppContext())
                .logLevel(ChatLogLevel.ALL)
                .notifications(ChatNotificationHandler(WoolworthsApplication.getAppContext(), notificationConfig))
                .build()

            ChatDomain.Builder(chatClient, WoolworthsApplication.getAppContext())
                .userPresenceEnabled()
                .offlineEnabled()
                .build()

            return chatClient
        }

        private fun authenticateOneCart(onSuccess: (String, String, String) -> Unit, onFailure: () -> Unit) {
            OneAppService.authenticateOneCart().apply {
                enqueue(CompletionHandler(object : IResponseListener<OCAuthenticationResponse> {
                    override fun onSuccess(response: OCAuthenticationResponse?) {
                        response?.apply {
                            if (httpCode == 200) {
                                onSuccess(details.userId, details.name, details.token)
                            }
                        } ?: kotlin.run {
                            onFailure()
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        onFailure()
                    }
                }, OCAuthenticationResponse::class.java))
            }
        }

        private fun connectUser(userId: String, displayName: String, token: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
            val currentUser = ChatClient.instance().getCurrentUser()
            currentUser?.let {
                onFailure()
                return
            }

            val chatUser = User().apply {
                id = userId
                name = displayName
            }

            ChatClient.instance().connectUser(chatUser, token)
                .enqueue { result ->
                    if (result.isSuccess) {
                        ChatClient.instance().getDevices().enqueue {
                            if (it.isSuccess) {
                                val devices = it.data()
                                for (device in devices) {
                                    ChatClient.instance().deleteDevice(device).enqueue()
                                }
                                ChatClient
                                    .instance()
                                    .addDevice(
                                        if (Utils.isGooglePlayServicesAvailable())
                                            Device(
                                                Utils.getOCFCMToken(),
                                                PushProvider.FIREBASE
                                            )
                                        else
                                            Device (
                                                Utils.getOCFCMToken(), // Since Stream uses Woolworths details for Huawei, we can use our own HMS cached token
                                                PushProvider.HUAWEI
                                            )
                                    )
                                    .enqueue()

                                onSuccess()
                            } else {
                                onFailure()
                            }
                        }
                    } else {
                        onFailure()
                    }
                }
        }

        private fun fetchOrdersPendingPicking(onCompletion: (ArrayList<Order>?) -> Unit) {
            OneAppService.getOrders().apply {
                enqueue(CompletionHandler(object : IResponseListener<OrdersResponse> {
                    override fun onSuccess(ordersResponse: OrdersResponse?) {
                        ordersResponse?.upcomingOrders?.filter { it.deliveryStatus?.Food?.equals(ORDER_PENDING_PICKING) == true }?.let {
                            onCompletion(ArrayList(it))
                        } ?: kotlin.run {
                            onCompletion(null)
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        onCompletion(null)
                    }
                }, OrdersResponse::class.java))
            }
        }

        private fun fetchChannels(chatClient: ChatClient, onSuccess: (List<Channel>) -> Unit, onFailure: () -> Unit) {
            chatClient.getCurrentUser()?.let { currentUser ->
                val filter = Filters.and(
                    Filters.`in`("members", listOf<String>(currentUser.id))
                )
                val sort = QuerySort.desc<Channel>("created_at")
                val request = QueryChannelsRequest(
                    filter = filter,
                    querySort = sort,
                    limit = 10,
                    messageLimit = 35
                )

                chatClient
                    .queryChannels(request)
                    .enqueue { result ->
                        if (result.isSuccess) {
                            onSuccess(result.data())
                        } else {
                            onFailure()
                        }
                    }
            } ?: kotlin.run {
                onFailure()
            }
        }

        private fun fetchOrderDetails(orderId: String, onSuccess: (OrderSummary) -> Unit, onFailure: () -> Unit) {
            OneAppService.getOrderDetails(orderId).enqueue(CompletionHandler(object :
                IResponseListener<OrderDetailsResponse> {
                override fun onSuccess(ordersResponse: OrderDetailsResponse?) {
                    ordersResponse?.orderSummary?.let {
                        onSuccess(it)
                    } ?: kotlin.run {
                        onFailure()
                    }
                }

                override fun onFailure(error: Throwable?) {
                    onFailure()
                }

            }, OrderDetailsResponse::class.java))
        }

        private fun getRecipientChannelMember(chatClient: ChatClient, channelId: String, onSuccess: (User) -> Unit, onFailure: () -> Unit) {
            chatClient.getCurrentUser()?.let { currentUser ->
                chatClient
                    .channel(channelId)
                    .queryMembers(0, 2, Filters.neutral()).enqueue { result ->
                        if (result.isSuccess) {
                            val member = result.data().last { x -> x.user.id != currentUser.id }
                            onSuccess(member.user)
                        } else {
                            onFailure()
                        }
                    }
            } ?: kotlin.run {
                onFailure()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::chatClient.isInitialized){
            chatClient.disconnect()
        }
        isOCChatBackgroundServiceRunning = false
    }
}