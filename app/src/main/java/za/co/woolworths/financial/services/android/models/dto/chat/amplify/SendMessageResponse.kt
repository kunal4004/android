package za.co.woolworths.financial.services.android.models.dto.chat.amplify

data class SendMessageResponse(
        val caption: String,
        val content: String,
        val contentType: String,
        val conversationMessagesId: String,
        val createdAt: String,
        val id: String,
        val messageID: String,
        val relatedMessageID: String,
        val sender: String,
        val sessionId: String,
        val sessionState: SessionStateType,
        val timestamp: String,
        val updatedAt: String
)