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
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SenderMessage
import za.co.woolworths.financial.services.android.util.Assets
import java.util.*

class LiveChatListAllAgentConversationImpl : IListAllAgentMessage {

    private val listMessageByConversation: String = Assets.readAsString("graphql/get-all-messages-for-conversation.graphql")
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
        onFailure: (ApiException) -> Unit) {
        val conversationId = liveChatDBRepository.getConversationMessageId()

        API.query(
            request(conversationId),
            { agentMessagesList ->
                val messageListFromChatAdapter =
                    ChatAWSAmplify.getChatMessageList()?.toMutableList()

                // reset agent profile icon flag to default
                messageListFromChatAdapter?.forEach {
                    (it as? SenderMessage)?.isWoolworthIconVisible = true
                    (it as? SendMessageResponse)?.isWoolworthIconVisible = true
                }

                // Get agent message list
                var listOfAllConversationByAgent: MutableList<SendMessageResponse>? =
                    agentMessagesList.data?.items?.toMutableList()

                // Sort agent list by createdAt Date Field
                listOfAllConversationByAgent =
                    listOfAllConversationByAgent
                        ?.sortedBy { it.createdAt }
                        ?.toMutableList()

                // Convert MutableList<SendMessageResponse> to mutableListOf<ChatMessage>()
                val chatMessageAgent = mutableListOf<ChatMessage>()
                listOfAllConversationByAgent?.forEach { chatMessageAgent.add(it) }

                // Create a new list with Adapter Messages and Agent Message and remove duplicate items
                val newMessageList: MutableList<ChatMessage>? = messageListFromChatAdapter
                    ?.plus(chatMessageAgent)
                    ?.distinct()
                    ?.toMutableList()

                val listWithoutNullValues : MutableList<ChatMessage>? = mutableListOf()
                // Remove empty messages from list
                newMessageList?.forEach { messages ->
                    val message = TextUtils.isEmpty(
                        when (messages) {
                            is SendMessageResponse -> messages.content
                            is SenderMessage -> messages.message
                        }
                    )
                    if (!message)
                        listWithoutNullValues?.add(messages)
                }

                //Keep a reference of new list
                ChatAWSAmplify.listAllChatMessages = listWithoutNullValues

                //Last Message will determine sessionStateType of UI component
                val lastMessage = listWithoutNullValues
                    ?.groupBy { it as? SendMessageResponse }?.keys
                    ?.lastOrNull()

                // emit result as Pair(first, second)
                onSuccess(Pair(listWithoutNullValues, lastMessage))
            },
            { apiException ->
                onFailure(apiException)
            })
    }

    override fun fetchAllAgentConversation(onSuccess: (Int, SendMessageResponse?) -> Unit) {
        val conversationId = liveChatDBRepository.getConversationMessageId()
        if (TextUtils.isEmpty(conversationId)) return
        API.query(
            request(conversationId),
            { listOfConversationsFromAgent ->
                // Conversation displayed in adapter
                val chatListFromAdapter = ChatAWSAmplify.getChatMessageList()?.toMutableList()

                // reset agent profile icon flag to default
                chatListFromAdapter?.forEach {
                    (it as? SenderMessage)?.isWoolworthIconVisible = true
                    (it as? SendMessageResponse)?.isWoolworthIconVisible = true
                }

               val agentListFromAdapter : MutableList<SendMessageResponse>? = chatListFromAdapter?.filterIsInstance<SendMessageResponse>() as? MutableList<SendMessageResponse>?

                val agentListFromService =  listOfConversationsFromAgent.data?.items?.sortedBy { it.createdAt }

                // query last item in  agent list
                val lastItem = agentListFromService?.firstOrNull()

                val sendMessageResponseList =  agentListFromService?.minus(agentListFromAdapter)

                onSuccess(sendMessageResponseList?.size ?: 0, lastItem)
            }, {})
    }
}