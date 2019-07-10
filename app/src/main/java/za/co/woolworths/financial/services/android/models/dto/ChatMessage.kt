package za.co.woolworths.financial.services.android.models.dto

class ChatMessage(val type: Type, val message: String) {

    enum class Type(val value: Int) {
        RECEIVED(1), SENT(2)
    }
}