package za.co.woolworths.financial.services.android.ui.fragments.account.chat.request

import android.text.TextUtils
import com.amplifyframework.api.ApiException
import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.core.Amplify.API
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.GetMessagesByConversation
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.IListAllAgentMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.ChatMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.UserMessage
import za.co.woolworths.financial.services.android.util.Assets
import java.text.SimpleDateFormat
import java.util.*

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
        onSuccess: (Pair<MutableList<ChatMessage>?, SendMessageResponse?>) -> Unit,
        onFailure: (ApiException) -> Unit
    ) {
        val conversationId = liveChatDBRepository.getConversationMessageId()
        API.query(
            request(conversationId),
            { messagesByConversationList ->
                val defaultMessageList = ChatAWSAmplify.getChatMessageList()?.toMutableList()

                // reset agent profile icon flag to default
                defaultMessageList?.forEach {
                    (it as? UserMessage)?.isWoolworthIconVisible = true
                    (it as? SendMessageResponse)?.isWoolworthIconVisible = true
                }

                var agentMessageList: MutableList<SendMessageResponse>? =
                    messagesByConversationList.data?.items?.toMutableList()

                agentMessageList = agentMessageList?.sortedBy { it.createdAt?.toDate() }?.toMutableList()

                val chatMessageAgent = mutableListOf<ChatMessage>()
                agentMessageList?.forEach { chatMessageAgent.add(it) }
                /**
                 * filter messageByConversation List and list of message from adapter,
                 * and remove duplicates
                 * groupBy creates a Map with a key as defined in the Lambda (id in this case),
                 * and a List of the items
                 */

                val messages: MutableList<ChatMessage>? =
                    defaultMessageList?.plus(chatMessageAgent)?.distinct()?.toMutableList()
                messages?.forEach {
                    val message = when (it) {
                        is SendMessageResponse -> it.content
                        is UserMessage -> it.message
                    }
                    if (TextUtils.isEmpty(message))
                        messages.remove(it)
                }

                ChatAWSAmplify.listAllChatMessages = messages
                val latestAgentMessage =
                    messages?.groupBy { it as? SendMessageResponse }?.keys?.lastOrNull()

                onSuccess(Pair(messages, latestAgentMessage))
            },
            { apiException ->
                onFailure(apiException)
            })
    }

    fun String.toDate(): Date? {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(this)
    }
}