package za.co.woolworths.financial.services.android.getstream.chat

import io.getstream.chat.android.client.models.Message

interface IMessageItemDelegate {
    val isMessageOwnedByMe: (message: Message) -> Boolean
}