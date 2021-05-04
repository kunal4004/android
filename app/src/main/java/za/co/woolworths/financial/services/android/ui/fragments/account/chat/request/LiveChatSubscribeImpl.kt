package za.co.woolworths.financial.services.android.ui.fragments.account.chat.request

import com.amplifyframework.api.ApiException
import com.amplifyframework.api.ApiOperation
import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.core.Amplify.API
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SendMessageResponse
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.ILiveChatSubscribe
import za.co.woolworths.financial.services.android.util.Assets
import java.util.*

class LiveChatSubscribeImpl(private var sessionStateType: SessionStateType? = null, private var message: String? = null) : ILiveChatSubscribe {

    private var subscription: ApiOperation<*>? = null
    private var messageRequest = LiveChatSendMessageImpl()
    private val liveChatDBRepository = LiveChatDBRepository()
    private val onSubscribeMessageByConversationIdDocument: String = Assets.readAsString("graphql/subscribe-event-on-message-by-conversation-id.graphql")

    override fun onSubscribe(onSuccess: (SendMessageResponse?) -> Unit, onFailure: (ApiException) -> Unit) {
        subscription = API.subscribe(onSubscribeMessageByConversationId(liveChatDBRepository.getConversationMessageId()),
                {
                    sessionStateType?.let { state -> message?.let { content -> messageRequest.send(state, content) } }
                },
                { data ->
                    onSuccess(data.data)
                },
                { apiException ->
                    onFailure(apiException)
                }, { }
        )
    }

    // Arrange a request to start a subscription.
    private fun onSubscribeMessageByConversationId(conversationMessagesId: String): GraphQLRequest<SendMessageResponse> {
        val variables = Collections.singletonMap<String, Any>("conversationMessagesId", conversationMessagesId)
        return SimpleGraphQLRequest(onSubscribeMessageByConversationIdDocument, variables, SendMessageResponse::class.java, GsonVariablesSerializer())
    }
}