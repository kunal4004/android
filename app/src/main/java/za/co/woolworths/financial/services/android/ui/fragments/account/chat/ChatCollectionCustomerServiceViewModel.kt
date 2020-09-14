package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSignUpResult
import com.amplifyframework.core.Amplify
import za.co.woolworths.financial.services.android.models.network.NetworkConfig


class ChatCollectionCustomerServiceViewModel : ViewModel() {

    init {
        addUserToCognitoUserPool()
    }

    private fun addUserToCognitoUserPool() {
        val config = NetworkConfig()
        val username = config.getSha1Password()
        val password = config.getApiId()


        Amplify.Auth.signUp(
                username,
                password,
                AuthSignUpOptions.builder()
                        .userAttribute(AuthUserAttributeKey.email(), "myemailaddress@gmail.com")
                        .build(),
                { result: AuthSignUpResult -> Log.e("AuthQuickStart", "Result: $result") }
        ) { error: AuthException? -> Log.e("AuthQuickStart", "Sign up failed", error) }
    }

}