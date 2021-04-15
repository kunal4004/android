package za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper

import android.app.Service
import android.content.Intent
import android.os.IBinder
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerInfo
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.Utils

class LiveChatIntentService : Service() {

    private val liveChatDBRepository: LiveChatDBRepository = LiveChatDBRepository()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        signIn({

            subscribeToMessageByConversationId({}, {})

        }, {

        })

        return START_STICKY
    }


    private fun signIn(result: () -> Unit, failure: (Any) -> Unit) {
        ChatAWSAmplify.signIn({ conversation ->
            liveChatDBRepository.saveCreateConversationModel(conversation)
            if (conversation == null) {
                logExceptionToFirebase("subscribeToMessageByConversationId")
                failure(failure)
            } else {
                result()
            }
        }, { failure -> failure(failure) })
    }

    private fun subscribeToMessageByConversationId(result: (SendMessageResponse?) -> Unit, failure: (Any) -> Unit) {
        val conversationId = getConversationMessageId()
        if (conversationId.isEmpty()) {
            logExceptionToFirebase("subscribeToMessageByConversationId")
            failure(failure)
            return
        }
        with(liveChatDBRepository) {
            ChatAWSAmplify.subscribeToMessageByConversationId(
                    conversationId,
                    getSessionType(),
                    getSessionVars(),
                    ChatCustomerInfo.getCustomerFamilyName(),
                    ChatCustomerInfo.getCustomerEmail(),
                    { data -> result(data) }, { failure(failure) })
        }
    }

    private fun logExceptionToFirebase(value: String?) = FirebaseManager.logException(value.plus(" ${Utils.toJson(liveChatDBRepository.getLiveChatParams()?.conversation)}"))

    private fun getConversationMessageId(): String = liveChatDBRepository.getLiveChatParams()?.conversation?.id
            ?: ""


}