package za.co.woolworths.financial.services.android.getstream.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import za.co.woolworths.financial.services.android.getstream.channel.ChannelListFragment.Companion.messageType
import za.co.woolworths.financial.services.android.getstream.common.ChatState
import za.co.woolworths.financial.services.android.getstream.common.State

class ChatViewModel: ViewModel() {

    private val chatClient: ChatClient by lazy { ChatClient.instance() }
    private val currentUser: User? by lazy { chatClient.getCurrentUser() }
    private val _state = MutableLiveData<ChatState>()

    lateinit var channelId: String

    val state: LiveData<ChatState> = _state
    val messages: MutableList<Message> = mutableListOf()

    public fun fetchMessages(){
        chatClient.channel(channelId).watch().enqueue { result ->
            if (result.isSuccess) {
                var channel = result.data()

                messages.clear()
                messages.addAll(channel.messages)

                _state.postValue(ChatState.ReceivedMessagesData)
            }
        }
    }

    private fun isMessageMine(message: Message): Boolean{
        currentUser ?: return false
        return message.user.id == currentUser!!.id
    }

    public fun sendMessage(messageText: String){
        val message = Message(
                cid = channelId,
                text = messageText
        )

        chatClient.sendMessage(
                channelId = channelId,
                channelType = messageType,
                message = message
        )
    }
}