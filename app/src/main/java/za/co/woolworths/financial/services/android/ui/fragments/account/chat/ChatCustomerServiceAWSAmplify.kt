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
import za.co.woolworths.financial.services.android.models.JWTDecodedModel
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.Conversation
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SendMessageResponse
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Assets
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities
import java.util.*

class ChatCustomerServiceAWSAmplify(private var account: Account? = null) {

    private var jWTDecodedModel: JWTDecodedModel? = SessionUtilities.getInstance().jwt
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

    fun subscribeToMessageByConversationId(conversationMessagesId: String, sessionType: SessionType, result: (SendMessageResponse?) -> Unit, failure: (ApiException) -> Unit) {
        subscription = API.subscribe(onSubscribeMessageByConversationId(conversationMessagesId),
                {
                    sendMessage(conversationMessagesId, sessionType, SessionStateType.CONNECT, "")
                },
                { data ->
                    result(data.data)
                },
                { onFailure -> failure(onFailure) },
                { Log.d("subscribeToConversation", "Subscription completed") }
        )
    }

    fun cancelSubscribeMessageByConversationId() {
        subscription?.cancel()
    }

    fun queryServiceSignOut(conversationMessagesId: String, sessionType: SessionType, sessionState: SessionStateType, content: String, onResult: () -> Unit, onError: () -> Unit) {

        sendMessage(conversationMessagesId, sessionType, sessionState, content)
        // Ensure sign out from all device
        Auth.signOut(AuthSignOutOptions.builder().globalSignOut(true).build(), { onResult() }, { onError() })
    }

    private fun sendMessageRequest(sessionId: String, sessionType: SessionType, sessionState: SessionStateType, content: String): GraphQLRequest<String> {

        val messageGraphQL: String = Assets.readAsString("graphql/mutation-event-send-message.graphql")
        val serializer = GsonVariablesSerializer()

        val variables = HashMap<String, Any>()

        variables["sessionId"] = sessionId
        variables["sessionType"] = sessionType
        variables["sessionState"] = sessionState
        variables["content"] = content
        variables["contentType"] = "text"
        variables["relatedMessageId"] = ""

        when (sessionState) {
            SessionStateType.ONLINE, SessionStateType.CONNECT, SessionStateType.DISCONNECT -> {
                val sessionVars = getSessionVars()
                Log.e("prsAccountNumber", sessionVars)
                Log.e("sesisonVars", getSessionVars())
                variables["sessionVars"] = sessionVars
                variables["name"] = getCustomerUsername()
                variables["email"] = getCustomerEmail()
            }
            else -> {
            }
        }
        return SimpleGraphQLRequest(messageGraphQL, variables, String::class.java, serializer)
    }

    fun sendMessage(sessionId: String, sessionType: SessionType, sessionState: SessionStateType, content: String) {
        API.mutate(
                sendMessageRequest(sessionId, sessionType, sessionState, content),
                { response -> Log.i("sendMessageSuccess", response.data ?: "") },
                { error -> Log.i("sendMessageError", error.toString()) }
        )
    }

    private fun getCustomerFamilyName(): String {
        val familyName = jWTDecodedModel?.family_name?.get(0)
        return KotlinUtils.firstLetterCapitalization(familyName) ?: ""
    }

    fun getCustomerUsername(): String {
        val username = jWTDecodedModel?.name?.get(0)
        return KotlinUtils.firstLetterCapitalization(username) ?: ""
    }

    private fun getCustomerEmail() = jWTDecodedModel?.email?.get(0) ?: ""

    private fun getCustomerC2ID() = jWTDecodedModel?.C2Id ?: ""

    private fun getSessionVars(): String {
        val prsAccountNumber = account?.accountNumber ?: ""
        val prsCardNumber = if (account?.productGroupCode?.toLowerCase(Locale.getDefault()) == "cc") account?.accountNumber
                ?: "" else account?.accountNumber ?: ""
        val prsC2id = getCustomerC2ID()
        val prsFirstname = getCustomerUsername()
        val prsSurname = getCustomerFamilyName()
        val prsProductOfferingId = account?.productOfferingId?.toString() ?: "0"
        val prsProductOfferingDescription = when (account?.productGroupCode?.toLowerCase(Locale.getDefault())) {
            "sc" -> "StoreCard"
            "pl" -> "PersonalLoan"
            "cc" -> "CreditCard"
            else -> ""
        }

        return bindString(R.string.chat_send_message_session_var_params, prsAccountNumber, prsCardNumber, prsC2id, prsFirstname, prsSurname, prsProductOfferingId, prsProductOfferingDescription)
    }
}