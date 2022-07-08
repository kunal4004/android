package za.co.woolworths.financial.services.android.getstream.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import za.co.woolworths.financial.services.android.getstream.channel.ChannelListFragment
import za.co.woolworths.financial.services.android.getstream.common.ChatState

class ChatViewModel: ViewModel() {

    private val chatClient: ChatClient by lazy { ChatClient.instance() }
    private val currentUser: User? by lazy { chatClient.getCurrentUser() }
    private val _state = MutableLiveData<ChatState>()

    lateinit var channelId: String

    val state: LiveData<ChatState> = _state
    val messages: MutableList<Message> = mutableListOf()

    lateinit var messageItemDelegate: IMessageItemDelegate

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

    public fun observeOtherUserPresence(){
        val filter = Filters.and(
                Filters.eq("type", ChannelListFragment.messageType)
        )

        chatClient.channel(channelId).queryMembers(0, 2, filter).enqueue{ result ->
            if(result.isSuccess){
                var otherUser = result.data().filter { x -> x.user.id != currentUser!!.id }.first()
                var temp = ""
            }else{

            }
        }
    }

    public fun emitIsTyping(){
        chatClient.channel(channelId).keystroke()
    }
}