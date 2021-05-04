package za.co.woolworths.financial.services.android.ui.fragments.account.chat.request

import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.devmenu.DeveloperMenu
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.KotlinUtils

class LiveChatConfigure {
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
}