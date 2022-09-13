package za.co.woolworths.financial.services.android.onecartgetstream.service

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleService
import com.awfs.coordination.R
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.pushprovider.huawei.HuaweiPushDeviceGenerator
import kotlinx.android.synthetic.main.fragment_shop_my_orders.*
import kotlinx.android.synthetic.main.order_details_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
import za.co.woolworths.financial.services.android.onecartgetstream.common.ChatState
import za.co.woolworths.financial.services.android.onecartgetstream.common.State
import za.co.woolworths.financial.services.android.onecartgetstream.model.OCAuthenticationResponse
import za.co.woolworths.financial.services.android.util.Utils

class DashChatMessageListeningService : LifecycleService(), ChatEventListener<NewMessageEvent> {

    private lateinit var chatClient: ChatClient
    // Controllers for observing data changes within the channel, where key is orderId
    private var chatChannelClients = HashMap<String, ChannelClient>()
    private var ordersSummary = ArrayList<OrderSummary>()
    private var channelIdToOrderIdMap = HashMap<String, String>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: use Coroutine instead of thread
        Thread {
            connectUserAndListenToChannels()
        }.start()
        return super.onStartCommand(intent, flags, startId)
    }

    // TODO: If the user initiates a new chat from My Order Details, for a channel that was not registered here, then that fragment needs to communicate with this service to add the new channel and start listening to it too.
    // Scenario A: service is started on app launch; user adds to cart, checkout and make payment; that order goes to pending_picking state and shopper initiates a chat with this user - this would mean the service is not listening to this new channel
    // Scenario B: Same as above, except there's no channel for the service to listen to, which means it will stop on launch itself. When new order's channel is opened, service needs to be started and listen to that new channel.
    private fun connectUserAndListenToChannels() {
        initializeOneCartStream()
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
                                    fetchChannels(
                                        onSuccess = { channels ->
                                            if (channels.isEmpty()) {
                                                killService()
                                                return@fetchChannels
                                            }

                                            channels.forEach { channel ->
                                                getRecipientChannelMember(
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
                                                        // TODO: handle negative scenario
                                                        // Ignored for now
                                                    }
                                                )
                                            }
                                        },
                                        onFailure = {
                                            // TODO: handle negative scenario
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
                                            // TODO: handle negative scenario
                                            countOrderDetailsRemaining -= 1
                                            if (countOrderDetailsRemaining == 0) {
                                                fnGetChannelForOrders()
                                            }
                                        }
                                    )
                                }

                            } else {
                                // TODO: No pending order - do we need to handle anything else before killing the service?
                                killService()
                            }
                        }
                    },
                    onFailure = {
                        // TODO: handle negative scenario
                        killService()
                    }
                )
            },
            onFailure = {
                // TODO: handle negative scenario
                killService()
            }
        )
    }

    private fun authenticateOneCart(onSuccess: (String, String, String) -> Unit, onFailure: () -> Unit) {
        OneAppService.authenticateOneCart().apply {
            enqueue(CompletionHandler(object : IResponseListener<OCAuthenticationResponse> {
                override fun onSuccess(response: OCAuthenticationResponse?) {
                    response?.apply {
                        onSuccess(details.userId, details.name, details.token)
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
                            // TODO: commenting this part to facilitate push notification troubleshooting. Needs to be uncommented after work is complete.
//                            val devices = it.data()
//                            for (device in devices) {
//                                ChatClient.instance().deleteDevice(device).enqueue()
//                            }

                            ChatClient
                                .instance()
                                .addDevice(
                                    if (Utils.isGooglePlayServicesAvailable())
                                        Device(
                                            WoolworthsApplication.getInstance().oneCartChatFirebaseToken,
                                            PushProvider.FIREBASE
                                        )
                                    else
                                        Device (
                                            Utils.getToken(), // Since Stream uses Woolworths details for Huawei, we can use our own HMS cached token
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
                    // TODO: move string to constant
                    ordersResponse?.upcomingOrders?.filter { it.deliveryStatus?.Food?.equals("PENDING_PICKING") == true }?.let {
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

    private fun fetchChannels(onSuccess: (List<Channel>) -> Unit, onFailure: () -> Unit) {
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

    private fun getRecipientChannelMember(channelId: String, onSuccess: (User) -> Unit, onFailure: () -> Unit) {
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

    private fun initializeOneCartStream() {
        val notificationConfig = NotificationConfig(
            pushDeviceGenerators = listOf(
                if (Utils.isGooglePlayServicesAvailable())
                    FirebasePushDeviceGenerator()
                else
                    HuaweiPushDeviceGenerator(
                        WoolworthsApplication.getAppContext(),
                        appId = "102461773" // TODO: move hardcoded huawei app ID somewhere else
                    )
            )
        )

        chatClient = ChatClient.Builder(AppConfigSingleton.dashConfig?.inAppChat?.apiKey.toString(), WoolworthsApplication.getAppContext())
            .logLevel(ChatLogLevel.ALL)
            .notifications(ChatNotificationHandler(WoolworthsApplication.getAppContext(), notificationConfig))
            .build()

        ChatDomain.Builder(chatClient, WoolworthsApplication.getAppContext())
            .userPresenceEnabled()
            .offlineEnabled()
            .build()
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
        // TODO: do we need to disconnect here? Will this prevent further push notifications from coming in? If not, we can disconnect.
//        chatClient.disconnect()
        stopSelf()
    }

    override fun onEvent(event: NewMessageEvent) {
        chatClient.getCurrentUser()?.let { currentUser ->
            if (currentUser.id != event.user.id) {
                // Incoming message from other channel member
                val channelId = event.cid
                val orderId = channelIdToOrderIdMap[event.cid]
                val orderSummary = ordersSummary.firstOrNull { it.orderId == orderId }
                Log.i("DashService", "Incoming Message from ${event.user.name} for channelId $channelId and orderId $orderId: ${event.message.text}")
                // TODO: Show toast with ability to open chat screen
                // TODO: do not show toast if current screen is either the chat screen or authentication screen (can still receive message while signing out)
                // TODO: show only 1 toast per N seconds (debounce) to prevent overwhelming overlapping toasts for fast incoming messages
                //
            }
        }
    }
}