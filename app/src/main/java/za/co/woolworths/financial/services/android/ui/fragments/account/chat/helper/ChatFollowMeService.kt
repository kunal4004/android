package za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper

import android.app.Service
import android.content.Intent
import android.os.IBinder
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerInfo
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.Utils

class ChatFollowMeService : Service() {

    private val liveChatDBRepository: LiveChatDBRepository = LiveChatDBRepository()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        signIn({
            subscribeToMessageByConversationId({

            }, {

            })

        }, {

        })
        return START_STICKY
    }


    private fun signIn(result: () -> Unit, fails: (Any) -> Unit) {
        ChatAWSAmplify.signIn({ conversation ->
            liveChatDBRepository.saveConversation(conversation)
            if (conversation == null) {
                logExceptionToFirebase("subscribeToMessageByConversationId")
                fails("logExceptionToFirebase")
            } else {
                result()
            }
        }, { error -> fails(error) })
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
                    getConversationMessageId(),
                    getSessionType(),
                    getSessionVars(),
                    ChatCustomerInfo.getCustomerFamilyName(),
                    ChatCustomerInfo.getCustomerEmail(),
                    { data -> result(data) }, { failure(failure) })
        }
    }

    private fun logExceptionToFirebase(value: String?) = FirebaseManager.logException(value.plus(" ${Utils.toJson(liveChatDBRepository.getLiveChatParams()?.conversation)}"))

    private fun getConversationMessageId(): String = liveChatDBRepository.getLiveChatParams()?.conversation?.id ?: ""

}