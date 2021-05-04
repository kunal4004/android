package za.co.woolworths.financial.services.android.ui.fragments.account.chat.request

import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify.logExceptionToFirebase
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.ChatCustomerInfo
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.ILiveChatSendMessage
import za.co.woolworths.financial.services.android.util.Assets
import java.util.*

class LiveChatSendMessageImpl : ILiveChatSendMessage {

    private val messageGraphQL: String = Assets.readAsString("graphql/send-message.graphql")

    override fun send(sessionState: SessionStateType, content: String): GraphQLRequest<String>? {

        val variables = HashMap<String, Any>()
        val liveChatDBRepository = LiveChatDBRepository()
        val conversationId = liveChatDBRepository.getConversationMessageId()

        if (conversationId.isEmpty()) {
            logExceptionToFirebase("sendMessage conversationId")
            return null
        }

        ChatAWSAmplify.sendMessageMutableList?.add(ChatMessage(ChatMessage.Type.SENT, content))

        variables["sessionId"] = conversationId
        variables["sessionType"] = liveChatDBRepository.getSessionType()
        variables["sessionState"] = sessionState
        variables["content"] = content
        variables["contentType"] = "text"
        variables["relatedMessageId"] = ""
        variables["sessionVars"] = liveChatDBRepository.getSessionVars()
        variables["name"] = ChatCustomerInfo.getCustomerFamilyName()
        variables["email"] = ChatCustomerInfo.getCustomerEmail()

        return SimpleGraphQLRequest(messageGraphQL, variables, String::class.java, GsonVariablesSerializer())
    }
}