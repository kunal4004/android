package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.util.Log
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.devmenu.DeveloperMenu
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.ChatMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.EncryptChat
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.UserMessage
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

object ChatAWSAmplify {

    var isChatActivityInForeground = false
    var listAllChatMessages: MutableList<ChatMessage>? = mutableListOf()
    var isUserSubscriptionActive: Boolean = false

    init {
        try {
            val context = WoolworthsApplication.getAppContext()
            val awsConfigurationJSONObject =
                KotlinUtils.getJSONFileFromRAWResFolder(context, R.raw.awsconfiguration)
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
            Log.e("awsException", "successful")
        } catch (ex: Exception) {
            Log.e("awsException", ex.toString())
            FirebaseManager.logException(ex)
        }
    }

    fun init() {}

    fun addChatMessageToList(chatMessage: ChatMessage) {
        listAllChatMessages?.add(
            EncryptChat(
                Utils.aes256EncryptStringAsBase64String(
                    Gson().toJson(
                        chatMessage
                    )
                )
            )
        )
    }

    fun getChatMessageList(): MutableList<ChatMessage> {
        val chatMessageList: MutableList<ChatMessage> = mutableListOf()
        listAllChatMessages?.forEach { encryptedMessage ->
            val encryptedData = encryptedMessage as? EncryptChat
            val decryptMessage = Utils.aes256DecryptBase64EncryptedString(encryptedData?.message)

            try {
                val chatMsg: ChatMessage? = when {
                    decryptMessage.contains(UserMessage::class.java.simpleName) -> {
                        Gson().fromJson(decryptMessage, UserMessage::class.java)
                    }
                    decryptMessage.contains(SendMessageResponse::class.java.simpleName) -> {
                        Gson().fromJson(decryptMessage, SendMessageResponse::class.java)
                    }
                    else -> null
                }


                chatMsg?.let { chatMessageList.add(it) }
            } catch (ex: Exception) {
                FirebaseManager.logException("${ChatAWSAmplify::class.java.simpleName} $ex")
            }
        }
        return chatMessageList
    }
}