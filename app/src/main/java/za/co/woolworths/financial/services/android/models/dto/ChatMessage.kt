package za.co.woolworths.financial.services.android.models.dto

import android.text.SpannableString

class ChatMessage(val type: Type, var message: SpannableString, var isMessageSent: Boolean = false, var isWoolworthIconVisible: Boolean = true) {

    enum class Type(val value: Int) {
        RECEIVED(1), SENT(2)
    }
}