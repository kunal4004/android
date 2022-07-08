package za.co.woolworths.financial.services.android.getstream.common

import io.getstream.chat.android.client.models.Message
import za.co.woolworths.financial.services.android.getstream.chat.MessageItem

sealed class ChatState {
    object ReceivedMessagesData : ChatState()
    data class ReceivedMessageData(val message: Message): ChatState()
    data class Error(val errorMessage: String?) : ChatState()
}