package za.co.woolworths.financial.services.android.onecartgetstream.chat

import io.getstream.chat.android.client.models.Message

interface IMessageItemDelegate {
    val isMessageOwnedByMe: (message: Message) -> Boolean
}