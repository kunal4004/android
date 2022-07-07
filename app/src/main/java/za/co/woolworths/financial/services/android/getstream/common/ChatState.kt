package za.co.woolworths.financial.services.android.getstream.common

sealed class ChatState: State() {
    object ReceivedMessagesData : ChatState()
}