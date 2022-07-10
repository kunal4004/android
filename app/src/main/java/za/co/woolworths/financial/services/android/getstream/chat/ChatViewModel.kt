package za.co.woolworths.financial.services.android.getstream.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import za.co.woolworths.financial.services.android.getstream.channel.ChannelListFragment
import za.co.woolworths.financial.services.android.getstream.common.ChatState

class ChatViewModel: ViewModel() {

    private val chatClient: ChatClient by lazy { ChatClient.instance() }
    private val currentUser: User? by lazy { chatClient.getCurrentUser() }
    private lateinit var otherUser: User
    private val _state = MutableLiveData<ChatState>()
    private val _isOtherUserOnline = MutableLiveData<Boolean>(false)

    lateinit var channelId: String

    val state: LiveData<ChatState> = _state
    val isOtherUserOnline: LiveData<Boolean> = _isOtherUserOnline
    val messages: MutableList<Message> = mutableListOf()

//    val userPresenceState = ChatDomain.instance().online

    var messageItemDelegate: IMessageItemDelegate

    init {
        messageItemDelegate = MessageItemDelegateImpl(
                isMessageOwnedByMe = { message: Message ->
                    currentUser ?: false
                    message.user.id == currentUser!!.id
                }
        )
    }

    public fun fetchMessages(){
        chatClient.channel(channelId).watch().enqueue { result ->
            if (result.isSuccess) {
                var channel = result.data()

                messages.clear()
                messages.addAll(result.data().messages)

                _state.postValue(ChatState.ReceivedMessagesData)
            }
        }

        //NewMessageEvent
    }

    public fun fetchOtherUser(){
        val channelClient = chatClient.channel(channelId)
        channelClient.queryMembers(0, 2, Filters.neutral()).enqueue { result ->
            if (result.isSuccess) {
                val member = result.data().last { x -> x.user.id != currentUser!!.id }
                otherUser = member.user
                postOtherUserPresence()

            } else {
                _state.postValue(ChatState.Error(result.error().message))
                postOtherUserPresence(false)
            }
        }

        channelClient.subscribe{ event ->
            Log.d("channelClient.subscribe", event.type)
        }

        channelClient.subscribeFor(
                UserStartWatchingEvent::class,
                UserStopWatchingEvent::class
        ){ event ->
            when{
                event is UserStartWatchingEvent && event.user.id == otherUser.id -> otherUser = event.user
                event is UserStopWatchingEvent && event.user.id == otherUser.id -> otherUser = event.user
            }

            postOtherUserPresence()
        }
    }

    private fun postOtherUserPresence(isOnline: Boolean? = null){
        _isOtherUserOnline.postValue(isOnline ?: otherUser.online)
    }

    public fun sendMessage(messageText: String){
        val message = Message(
                cid = channelId,
                text = messageText
        )

        chatClient.channel(channelId).sendMessage(message).enqueue{ result ->
            if (result.isSuccess) {
                val message: Message = result.data()
                _state.postValue(ChatState.ReceivedMessageData(message))
            } else {
                _state.postValue(ChatState.Error(result.error().message))
            }
        }
    }

    public fun emitIsTyping(){
        chatClient.channel(channelId).keystroke()
    }
}