package za.co.woolworths.financial.services.android.ui.fragments.account.chat.request

import com.amplifyframework.api.ApiException
import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.core.Amplify.API
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.GetMessagesByConversation
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.IListAllAgentMessage
import za.co.woolworths.financial.services.android.util.Assets
import java.util.HashMap

class LiveChatListAllAgentConversationImpl : IListAllAgentMessage {

    private val listMessageByConversation: String =
        Assets.readAsString("graphql/get-all-messages-for-conversation.graphql")
    private val liveChatDBRepository = LiveChatDBRepository()

    // Arrange a request to start a subscription.
    private fun request(conversationMessagesId: String): GraphQLRequest<GetMessagesByConversation> {
        val variables = HashMap<String, Any>()
        variables["conversationMessagesId"] = conversationMessagesId
        return SimpleGraphQLRequest(
            listMessageByConversation,
            variables,
            GetMessagesByConversation::class.java,
            GsonVariablesSerializer()
        )
    }

    override fun list(
        onSuccess: (GetMessagesByConversation?) -> Unit,
        onFailure: (ApiException) -> Unit
    ) {
        val conversationId = liveChatDBRepository.getConversationMessageId()
        API.query(
            request(conversationId),
            { response -> onSuccess(response.data) },
            { apiException ->
                onFailure(apiException)
            })
    }
}