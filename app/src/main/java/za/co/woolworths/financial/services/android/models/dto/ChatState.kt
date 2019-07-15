package za.co.woolworths.financial.services.android.models.dto

data class ChatState(val sessionId: String?, val agentId: String?, val agentNickName: String?, val text: List<String>?, val url: String?, val urlContents: String?, val transferred: String?, val urlTransfer: String?, val isTyping: Int?, val urlOnClose: String?, val state: Int?, val urlRequested: String?)