package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.ChatMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SenderMessage

object ChatAWSAmplify {

    var isChatActivityInForeground = false
    var listAllChatMessages: MutableList<ChatMessage>? = mutableListOf()
    var isLiveChatActivated: Boolean = false
    var sessionStateType : SessionStateType? = null
    var isLiveChatBackgroundServiceRunning = false
    var isConnectedToInternet = true
    var BOTTOM_NAVIGATION_BADGE_COUNT: Int = 100000

    fun addChatMessageToList(chatMessage: ChatMessage) {
        val content = when (chatMessage) {
            is SenderMessage -> chatMessage.message
            is SendMessageResponse -> chatMessage.content
        }
        if (content?.isNotEmpty() == true)
            listAllChatMessages?.add(chatMessage)
    }

    fun getChatMessageList(): MutableList<ChatMessage>? = listAllChatMessages
}