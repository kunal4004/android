package za.co.woolworths.financial.services.android.models.dto

data class SendEmailIntentInfo(val emailAddress: String = "", val subjectLine: String = "")

class ChatMessage(val type: Type, var message: String, var isMessageSent: Boolean = false, var isWoolworthIconVisible: Boolean = true, var sendEmailIntentInfo : SendEmailIntentInfo?=null) {

    enum class Type(val value: Int) {
        RECEIVED(1), SENT(2)
    }
}