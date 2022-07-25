package za.co.woolworths.financial.services.android.getstream.chat

import io.getstream.chat.android.client.models.Message

data class MessageItem(val isMine: Boolean, val message: Message)
