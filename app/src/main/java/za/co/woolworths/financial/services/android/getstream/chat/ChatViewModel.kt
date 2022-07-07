package za.co.woolworths.financial.services.android.getstream.chat

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message


class ChatViewModel: ViewModel() {

    var channelId: String? = null

    private val chatClient: ChatClient by lazy { ChatClient.instance() }

     fun fetchMessages(){
        chatClient.channel(channelId!!).watch().enqueue { result ->
            if (result.isSuccess) {
                var channel = result.data()
                channel.messages
            }
        }
    }
    fun sendMessage(messageText: String) {
        channelId?.let {
            val message = Message(
                cid = it,
                text = messageText
            )
            // chatClient.channel(it).sendMessage(message)
            chatClient.channel(it).sendMessage(message).enqueue { result ->
                if (result.isSuccess) {
                    val message: Message = result.data()
                } else {
                    // Handle result.error()
                }

            }

        }
    }

    fun isMessageMine(message: Message): Boolean {
        val currentUser = chatClient.getCurrentUser() ?: return false
        return message.user.id.equals(currentUser.id)
    }
}