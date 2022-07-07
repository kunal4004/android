package za.co.woolworths.financial.services.android.getstream.chat

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.livedata.controller.ChannelController

class ChatViewModel: ViewModel() {

    var channelId: String? = null

    private val chatClient: ChatClient by lazy { ChatClient.instance() }

    public fun fetchMessages(){
        chatClient.channel(channelId!!).watch().enqueue { result ->
            if (result.isSuccess) {
                var channel = result.data()

                channel.messages
            }
        }
    }
}