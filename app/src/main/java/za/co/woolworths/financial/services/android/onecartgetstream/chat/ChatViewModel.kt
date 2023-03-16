package za.co.woolworths.financial.services.android.onecartgetstream.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.observable.Disposable
import za.co.woolworths.financial.services.android.onecartgetstream.common.ChatState


class ChatViewModel : ViewModel() {

    private val chatClient: ChatClient by lazy { ChatClient.instance() }
    private val currentUser: User? by lazy { chatClient.getCurrentUser() }
    private lateinit var otherUser: User
    private val _state = MutableLiveData<ChatState>()
    private val _isOtherUserOnline = MutableLiveData<Boolean>(false)
    private val _otherUserDisplayName = MutableLiveData<String>("")
    private val _otherUserTyping = MutableLiveData<String>("")
    lateinit var channelId: String
    val state: LiveData<ChatState> = _state
    val isOtherUserOnline: LiveData<Boolean> = _isOtherUserOnline
    val otherUserDisplayName: LiveData<String> = _otherUserDisplayName
    val otherUserTyping: LiveData<String> = _otherUserTyping
    val messages: MutableList<Message> = mutableListOf()
    var messageItemDelegate: IMessageItemDelegate
    private lateinit var userWatchingEventsDisposable: Disposable
    private lateinit var newMessageEventDisposable: Disposable
    private lateinit var userTypingEvent: Disposable

    init {
        messageItemDelegate = MessageItemDelegateImpl(
            isMessageOwnedByMe = { message: Message ->
                currentUser ?: false
                message.user.id == currentUser!!.id
            }
        )
    }

    fun fetchMessages() {
        chatClient.channel(channelId).watch().enqueue { result ->
            if (result.isSuccess) {
                messages.clear()
                messages.addAll(result.data().messages)

                chatClient.channel(channelId).markRead().enqueue { result ->
                    //Ignore
                }
                _state.postValue(ChatState.ReceivedMessagesData)
            }
        }
    }

    fun fetchOtherUser() {
        val channelClient = chatClient.channel(channelId)
        channelClient.queryMembers(0, 2, Filters.neutral()).enqueue { result ->
            if (result.isSuccess && !result.data().isNullOrEmpty()) {
                val member = result.data().lastOrNull() { x -> x.user.id != currentUser?.id }
                member?.let {
                    otherUser = it.user
                    _otherUserDisplayName.postValue(otherUser.name)
                }
                observeOtherUserEvents()
                observeNewMessageEvents()
                postOtherUserPresence()
                typingIndicator()

            } else {
                _state.postValue(ChatState.Error(result.error().message))
                postOtherUserPresence(false)
            }
        }
    }

    private fun postOtherUserPresence(isOnline: Boolean? = null) {
        _isOtherUserOnline.postValue(isOnline ?: otherUser.online)
    }

    fun sendMessage(messageText: String) {
        val message = Message(
            cid = channelId,
            text = messageText
        )
        chatClient.channel(channelId).sendMessage(message).enqueue { result ->
            if (!result.isSuccess) {
                _state.postValue(ChatState.Error(result.error().message))
            }
        }
    }

    fun emitIsTyping() {
        chatClient.channel(channelId).keystroke().enqueue()
    }

    fun stopTyping() {
        chatClient.channel(channelId).stopTyping().enqueue()
    }

    private fun observeOtherUserEvents() {
        val channelClient = chatClient.channel(channelId)
        this.userWatchingEventsDisposable = channelClient.subscribeFor(
            UserStartWatchingEvent::class,
            UserStopWatchingEvent::class
        ) { event ->
            when {
                event is UserStartWatchingEvent && event.user.id == otherUser.id -> {
                    otherUser = event.user
                    postOtherUserPresence(true)

                }
                event is UserStopWatchingEvent && event.user.id == otherUser.id -> {
                    otherUser = event.user
                    postOtherUserPresence(false)

                }
            }
        }
    }

    private fun observeNewMessageEvents() {
        val channelClient = chatClient.channel(channelId)
        // Subscribe for new message events
        this.newMessageEventDisposable = channelClient.subscribeFor<NewMessageEvent> { event ->
            val message = event.message
            chatClient.channel(channelId).markRead().enqueue { result ->
                //Ignore
            }
            _state.postValue(ChatState.ReceivedMessageData(message))
        }
    }


    private fun typingIndicator() {
        val nobodyTyping = ""
        val currentlyTyping = mutableSetOf<String>()
        val channelClient = chatClient.channel(channelId)
        this.userTypingEvent = channelClient.subscribeFor(
            TypingStartEvent::class, TypingStopEvent::class
        ) { event ->
            when (event) {
                is TypingStartEvent -> currentlyTyping.add(event.user.name)
                is TypingStopEvent -> currentlyTyping.remove(event.user.name)
                else -> {
                    // Nothing
                }
            }
            when {
                currentlyTyping.isNotEmpty() -> _otherUserTyping.value =
                    currentlyTyping.joinToString(prefix = "typing: ")
                else -> _otherUserTyping.value = nobodyTyping
            }
        }
    }


    fun disconnect() {
        if (::userWatchingEventsDisposable.isInitialized &&
            ::newMessageEventDisposable.isInitialized &&
            ::userTypingEvent.isInitialized

        ) {
            userWatchingEventsDisposable.dispose()
            //Removing message event removes the attached listener for all
            // events from DashChatMessageService as well and not able to listen to events in services
            //hence commenting
//            newMessageEventDisposable.dispose()
            userTypingEvent.dispose()

        }
        chatClient.disconnect()
    }

    fun isConnected(): Boolean {
        val currentUser = ChatClient.instance().getCurrentUser()
        currentUser?.let {
            return true
        }
        return false
    }
}