package za.co.woolworths.financial.services.android.ui.fragments.account.chat

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
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.*

import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.util.Assets
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.KotlinUtils
import java.util.*

object ChatAWSAmplify {

    private var subscription: ApiOperation<*>? = null

    init {
        try {
            val context = WoolworthsApplication.getAppContext()
            val awsConfigurationJSONObject = KotlinUtils.getJSONFileFromRAWResFolder(context, R.raw.awsconfiguration)
            val inAppChat = WoolworthsApplication.getInAppChat()
            val auth = awsConfigurationJSONObject
                    .getJSONObject("auth")
                    .getJSONObject("plugins")
                    .getJSONObject("awsCognitoAuthPlugin")
                    .getJSONObject("CognitoUserPool")
                    .getJSONObject("Default")

            auth.put("PoolId", inAppChat.userPoolId)
            auth.put("AppClientId", inAppChat.userPoolWebClientId)

            val api = awsConfigurationJSONObject
                    .getJSONObject("api")
                    .getJSONObject("plugins")
                    .getJSONObject("awsAPIPlugin")
                    .getJSONObject("api")

            api.put("endpoint", inAppChat.apiURI)

            val awsConfiguration = AmplifyConfiguration.fromJson(awsConfigurationJSONObject)
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(awsConfiguration, context)
            DeveloperMenu.singletonInstance(context).setVisible(false)
        } catch (ex: Exception) {
            FirebaseManager.logException(ex)
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
        val onSubscribeMessageByConversationIdDocument: String = Assets.readAsString("graphql/subscribe-event-on-message-by-conversation-id.graphql")
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
                    sendMessage(conversationMessagesId,
                            sessionType,
                            SessionStateType.CONNECT,
                            "hi",
                            sessionVars,
                            name,
                            email)
                },
                { data -> result(data.data) },
                { onFailure ->
                    failure(onFailure)
                }, { }
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

        sendMessage(conversationMessagesId, sessionType, sessionState, content, sessionVars, name, email)
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

        val messageGraphQL: String = Assets.readAsString("graphql/send-message.graphql")
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
                { },
                { }
        )
    }

    // Arrange a request to start a subscription.
    private fun listMessages(conversationMessagesId: String): GraphQLRequest<GetMessagesByConversation> {
        val listMessageByConversation: String = Assets.readAsString("graphql/get-all-messages-for-conversation.graphql")
        val serializer = GsonVariablesSerializer()
        val variables = HashMap<String, Any>()
        variables["conversationMessagesId"] = conversationMessagesId
        return SimpleGraphQLRequest(listMessageByConversation, variables, GetMessagesByConversation::class.java, serializer)
    }

    fun getMessagesListByConversation(conversationMessagesId: String, result: (GetMessagesByConversation?) -> Unit) {
        API.query(
                listMessages(conversationMessagesId),
                { response -> result(response.data) }, { }
        )
    }

    fun init() {}

}