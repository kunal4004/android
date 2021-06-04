package za.co.woolworths.financial.services.android.ui.fragments.account.chat.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.util.*

sealed class ChatMessage

    @Parcelize
    data class SenderMessage(
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
        val createdAt: Date? = null,
        val id: String? = null,
        val messageID: String? = null,
        val relatedMessageID: String? = null,
        val sender: String? = null,
        val sessionId: String? = null,
        var sessionState: SessionStateType? = null,
        val timestamp: Long? = null,
        val updatedAt: Date? = null,
        var sendEmailIntentInfo: SendEmailIntentInfo? = null,
        var isWoolworthIconVisible: Boolean = true
    ) : ChatMessage(), Parcelable


