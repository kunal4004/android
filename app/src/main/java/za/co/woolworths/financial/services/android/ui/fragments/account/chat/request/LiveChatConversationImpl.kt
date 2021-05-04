package za.co.woolworths.financial.services.android.ui.fragments.account.chat.request

import com.amplifyframework.api.ApiException
import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.core.Amplify.API
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.Conversation
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.ILiveChatConversation
import za.co.woolworths.financial.services.android.util.Assets

class LiveChatConversationImpl : ILiveChatConversation {

    private val createConversation: String = Assets.readAsString("graphql/create-conversation.graphql")

    override fun conversation(onSuccess: () -> Unit, onFailure: (ApiException) -> Unit) {
        API.mutate(request(), { response ->
            val conversation = response.data
            LiveChatDBRepository().saveConversation(conversation)
            onSuccess()
        }, { apiException ->
            onFailure(apiException)
        })
    }

    private fun request(): GraphQLRequest<Conversation> {
        return SimpleGraphQLRequest(createConversation, HashMap(), Conversation::class.java, GsonVariablesSerializer())
    }

}