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

    companion object {
        const val DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }

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

    override fun messageListFromAgent(
        onSuccess: (Pair<MutableList<ChatMessage>?, SendMessageResponse?>) -> Unit,
        onFailure: (ApiException) -> Unit
    ) {
        val conversationId = liveChatDBRepository.getConversationMessageId()
        API.query(
            request(conversationId),
            { agentMessagesList ->
                val messageListFromChatAdapter =
                    ChatAWSAmplify.getChatMessageList()?.toMutableList()

                // reset agent profile icon flag to default
                messageListFromChatAdapter?.forEach {
                    (it as? UserMessage)?.isWoolworthIconVisible = true
                    (it as? SendMessageResponse)?.isWoolworthIconVisible = true
                }

                // Get agent message list
                var listOfAllConversationByAgent: MutableList<SendMessageResponse>? =
                    agentMessagesList.data?.items?.toMutableList()

                // Sort agent list by createdAt Date Field
                listOfAllConversationByAgent =
                    listOfAllConversationByAgent
                        ?.sortedBy { it.createdAt?.toDate() }
                        ?.toMutableList()

                // Convert MutableList<SendMessageResponse> to mutableListOf<ChatMessage>()
                val chatMessageAgent = mutableListOf<ChatMessage>()
                listOfAllConversationByAgent?.forEach { chatMessageAgent.add(it) }

                // Create a new list with Adapter Messages and Agent Message and remove duplicate items
                val newMessageList: MutableList<ChatMessage>? = messageListFromChatAdapter
                        ?.plus(chatMessageAgent)
                        ?.distinct()
                        ?.toMutableList()

                // Remove empty messages from list
                newMessageList?.forEach {
                    val message = when (it) {
                        is SendMessageResponse -> it.content
                        is UserMessage -> it.message
                    }
                    if (TextUtils.isEmpty(message))
                        newMessageList.remove(it)
                }

                //Keep a reference of new list
                ChatAWSAmplify.listAllChatMessages = newMessageList

                //Last Message will determine sessionStateType of UI component
                val lastMessage = newMessageList
                        ?.groupBy { it as? SendMessageResponse }?.keys
                        ?.lastOrNull()

                // emit result as Pair(first, second)
                onSuccess(Pair(newMessageList, lastMessage))
            },
            { apiException ->
                onFailure(apiException)
            })
    }

    fun String.toDate(): Date? {
        return SimpleDateFormat(DATE_PATTERN, Locale.US).parse(this)
    }
}