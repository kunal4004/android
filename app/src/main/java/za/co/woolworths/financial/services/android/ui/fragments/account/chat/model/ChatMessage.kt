package za.co.woolworths.financial.services.android.ui.fragments.account.chat.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType

sealed class ChatMessage

    @Parcelize
    data class UserMessage(
        var message: String,
        var isWoolworthIconVisible: Boolean = true,
        var sendEmailIntentInfo: SendEmailIntentInfo? = null
    ) : ChatMessage(), Parcelable

    @Parcelize
    data class SendEmailIntentInfo(val emailAddress: String = "", val subjectLine: String = "") :
        Parcelable

    @Parcelize
    data class SendMessageResponse(
        val caption: String? = null,
        val content: String? = null,
        val contentType: String? = null,
        val conversationMessagesId: String? = null,
        val createdAt: String? = null,
        val id: String? = null,
        val messageID: String? = null,
        val relatedMessageID: String? = null,
        val sender: String? = null,
        val sessionId: String? = null,
        val sessionState: SessionStateType? = null,
        val timestamp: String? = null,
        val updatedAt: String? = null,
        var sendEmailIntentInfo: SendEmailIntentInfo? = null,
        var isWoolworthIconVisible: Boolean = true
    ) : ChatMessage(), Parcelable


