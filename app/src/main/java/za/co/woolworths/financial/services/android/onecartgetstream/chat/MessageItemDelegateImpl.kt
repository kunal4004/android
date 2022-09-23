package za.co.woolworths.financial.services.android.onecartgetstream.chat

import io.getstream.chat.android.client.models.Message

internal class MessageItemDelegateImpl(override val isMessageOwnedByMe: ( message: Message) -> Boolean) : IMessageItemDelegate