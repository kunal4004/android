package za.co.woolworths.financial.services.android.models.dto

class ChatMessage(val type: Type, var message: String, var isMessageSent: Boolean = false, var isWoolworthIconVisible: Boolean = true) {

    enum class Type(val value: Int) {
        RECEIVED(1), SENT(2)
    }
}