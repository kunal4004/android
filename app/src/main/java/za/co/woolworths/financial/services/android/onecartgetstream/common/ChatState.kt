package za.co.woolworths.financial.services.android.onecartgetstream.common

import io.getstream.chat.android.client.models.Message

sealed class ChatState {
    object ReceivedMessagesData : ChatState()
    data class ReceivedMessageData(val message: Message): ChatState()
    data class Error(val errorMessage: String?) : ChatState()
}