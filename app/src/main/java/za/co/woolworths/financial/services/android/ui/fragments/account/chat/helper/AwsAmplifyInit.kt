package za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper

import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.logging.AndroidLoggingPlugin
import com.amplifyframework.logging.LogLevel
import com.awfs.coordination.BuildConfig
import com.awfs.coordination.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.KotlinUtils

class AmplifyInit {

    init {
        GlobalScope.launch(Dispatchers.Main) {
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

                val awsConfiguration = AmplifyConfiguration
                    .builder(awsConfigurationJSONObject)
                    .devMenuEnabled(false)
                    .build()
                Amplify.addPlugin(AWSCognitoAuthPlugin())
                Amplify.addPlugin(AWSApiPlugin())
                Amplify.addPlugin(AndroidLoggingPlugin(if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.NONE))
                Amplify.configure(awsConfiguration, context)
            } catch (ex: Exception) {
                FirebaseManager.logException(ex)
            }
        }
    }
}