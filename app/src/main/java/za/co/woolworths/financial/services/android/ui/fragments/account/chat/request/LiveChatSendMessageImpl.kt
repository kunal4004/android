package za.co.woolworths.financial.services.android.ui.fragments.account.chat.request

import android.text.TextUtils
import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.core.Amplify.API
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.ChatCustomerInfo
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.ILiveChatSendMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.UserMessage
import za.co.woolworths.financial.services.android.util.Assets
import za.co.woolworths.financial.services.android.util.FirebaseManager
import java.util.*

class LiveChatSendMessageImpl : ILiveChatSendMessage {

    private val messageGraphQL: String = Assets.readAsString("graphql/send-message.graphql")
    private val liveChatDBRepository = LiveChatDBRepository()

    override fun send(sessionState: SessionStateType, content: String) {

        val conversationId = liveChatDBRepository.getConversationMessageId()

        if (conversationId.isEmpty()) {
            FirebaseManager.logException("${LiveChatSendMessageImpl::class.java.simpleName} conversationId not found")
            return
        }
        API.mutate(request(sessionState, content), {
            if (sessionState != SessionStateType.CONNECT && !TextUtils.isEmpty(content)) {
                ChatAWSAmplify.addChatMessageToList(UserMessage(content))
            }
        }, {})
    }

    private fun request(sessionState: SessionStateType, content: String): GraphQLRequest<String> {

        val variables = HashMap<String, Any>()
        val conversationId = liveChatDBRepository.getConversationMessageId()

        variables["sessionId"] = conversationId
        variables["sessionType"] = liveChatDBRepository.getSessionType()
        variables["sessionState"] = sessionState
        variables["content"] = content
        variables["contentType"] = "text"
        variables["relatedMessageId"] = ""
        variables["sessionVars"] = liveChatDBRepository.getSessionVars()
        variables["name"] = ChatCustomerInfo.getCustomerFamilyName()
        variables["email"] = ChatCustomerInfo.getCustomerEmail()

        return SimpleGraphQLRequest(
            messageGraphQL,
            variables,
            String::class.java,
            GsonVariablesSerializer()
        )
    }
}