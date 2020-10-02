package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.content.Context
import android.util.Log
import com.amplifyframework.api.ApiException
import com.amplifyframework.api.ApiOperation
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.aws.GsonVariablesSerializer
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.Amplify.API
import com.amplifyframework.core.Amplify.Auth
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.devmenu.DeveloperMenu
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionType
import com.awfs.coordination.R
import com.crashlytics.android.Crashlytics
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.Conversation
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SendMessageResponse
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.util.Assets
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.util.*

class ChatCustomerServiceAWSAmplify {

    private var subscription: ApiOperation<*>? = null

    fun init(context: Context) {
        try {
            val awsConfigurationJSONObject = KotlinUtils.getGSONFileFromRAWResFolder(context, R.raw.awsconfiguration)
            val awsConfiguration = AmplifyConfiguration.fromJson(awsConfigurationJSONObject)
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(awsConfiguration, context)
            DeveloperMenu.singletonInstance(context).setVisible(false)
        } catch (ex: Exception) {
            Crashlytics.log(ex.message)
        }
    }

    fun signIn(result: (Conversation?) -> Unit, error: (Any) -> Unit) {

        val networkConfig = NetworkConfig()
        val username = networkConfig.getApiId()
        val password = networkConfig.getSha1Password()

        Auth.signIn(username, password, { createConversation({ result -> result(result) }, { failure -> error(failure) }) }, { error(it) })
    }

    // Create conversation
    private fun conversationRequest(): GraphQLRequest<Conversation> {
        val createConversation: String = Assets.readAsString("graphql/create-conversation.graphql")
        return SimpleGraphQLRequest(createConversation, HashMap<String, Any>(), Conversation::class.java, GsonVariablesSerializer())
    }

    private fun createConversation(result: (Conversation?) -> Unit, failure: (ApiException) -> Unit) {
        API.mutate(conversationRequest(), { response -> result(response.data) }, { error -> failure(error) })
    }

    // Arrange a request to start a subscription.
    private fun onSubscribeMessageByConversationId(conversationMessagesId: String): GraphQLRequest<SendMessageResponse> {
        val onSubscribeMessageByConversationIdDocument: String = Assets.readAsString("graphql/subscribe-event-message-by-conversation-id.graphql")
        val serializer = GsonVariablesSerializer()
        val variables = Collections.singletonMap<String, Any>("conversationMessagesId", conversationMessagesId)
        return SimpleGraphQLRequest(onSubscribeMessageByConversationIdDocument, variables, SendMessageResponse::class.java, serializer)
    }

    fun subscribeToMessageByConversationId(conversationMessagesId: String,
                                           sessionType: SessionType,
                                           sessionVars: String,
                                           name: String,
                                           email: String,
                                           result: (SendMessageResponse?) -> Unit, failure: (ApiException) -> Unit) {
        subscription = API.subscribe(onSubscribeMessageByConversationId(conversationMessagesId),
                {
                    Log.d("subscriptionLeg", "Connection Established")
                    sendMessage(conversationMessagesId,
                            sessionType,
                            SessionStateType.CONNECT,
                            "",
                            sessionVars,
                            name,
                            email)
                },
                { data -> result(data.data) },
                { onFailure ->
                    Log.d("subscribeToConversation", "Subscription completed")
                    failure(onFailure)
                },
                { Log.d("subscribeToConversation", "Subscription completed") }
        )
    }

    fun cancelSubscribeMessageByConversationId() {
        subscription?.cancel()
    }

    fun queryServiceSignOut(conversationMessagesId: String,
                            sessionType: SessionType,
                            sessionState: SessionStateType,
                            content: String,
                            sessionVars: String,
                            name: String,
                            email: String, onResult: () -> Unit, onError: () -> Unit) {

        sendMessage(conversationMessagesId, sessionType, sessionState, content,
                sessionVars,
                name,
                email)
        // Ensure sign out from all device
        Auth.signOut(AuthSignOutOptions.builder().globalSignOut(true).build(), { onResult() }, { onError() })
    }

    private fun sendMessageRequest(sessionId: String,
                                   sessionType: SessionType,
                                   sessionState: SessionStateType,
                                   content: String,
                                   sessionVars: String,
                                   name: String,
                                   email: String): GraphQLRequest<String> {

        val messageGraphQL: String = Assets.readAsString("graphql/mutation-event-send-message.graphql")
        val serializer = GsonVariablesSerializer()
        val variables = HashMap<String, Any>()

        variables["sessionId"] = sessionId
        variables["sessionType"] = sessionType
        variables["sessionState"] = sessionState
        variables["content"] = content
        variables["contentType"] = "text"
        variables["relatedMessageId"] = ""
        variables["sessionVars"] = sessionVars
        variables["name"] = name
        variables["email"] = email

        Log.e("sesisonVars", sessionVars)

        return SimpleGraphQLRequest(messageGraphQL, variables, String::class.java, serializer)
    }

    fun sendMessage(sessionId: String,
                    sessionType: SessionType,
                    sessionState: SessionStateType,
                    content: String,
                    sessionVars: String,
                    name: String,
                    email: String) {

        API.mutate(sendMessageRequest(sessionId,
                sessionType,
                sessionState,
                content,
                sessionVars,
                name,
                email),
                { response -> Log.d("sendMessageSuccess", response.data ?: "") },
                { error -> Log.d("sendMessageError", error.toString()) }
        )
    }

}