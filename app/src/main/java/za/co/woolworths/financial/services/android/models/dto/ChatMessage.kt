package za.co.woolworths.financial.services.android.models.dto

import android.text.SpannableString

data class SendEmail(val emailAddress: String = "", val subjectLine: String = "")

class ChatMessage(val type: Type, var message: String, var isMessageSent: Boolean = false, var isWoolworthIconVisible: Boolean = true, var sendEmail : SendEmail?=null) {

    enum class Type(val value: Int) {
        RECEIVED(1), SENT(2)
    }
}