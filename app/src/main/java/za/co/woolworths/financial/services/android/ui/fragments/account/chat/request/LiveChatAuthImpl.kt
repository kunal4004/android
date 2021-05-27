package za.co.woolworths.financial.services.android.ui.fragments.account.chat.request

import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.core.Amplify.Auth
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.ILiveChatAuth
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.util.FirebaseManager

class LiveChatAuthImpl : ILiveChatAuth {

    private val sendMessage = LiveChatSendMessageImpl()
    private val liveChatListAllAgentConversation = LiveChatDBRepository()

    override fun signIn(onSuccess: (AuthSignInResult) -> Unit, onFailure: (AuthException) -> Unit) {
        val networkConfig = NetworkConfig()
        val username = networkConfig.getApiId()
        val password = networkConfig.getSha1Password()
        try {
            Auth.signIn(username, password, { authSignInResult ->
                onSuccess(authSignInResult)
            }, { authException ->
                onFailure(authException)
            })
        } catch (ex: IllegalStateException) {
            FirebaseManager.logException(ex)
        }
    }

    override fun signOut(result: () -> Unit) {
        sendMessage.send(SessionStateType.DISCONNECT, "")
        Auth.signOut(
            AuthSignOutOptions.builder().globalSignOut(true).build(),
            {
                liveChatListAllAgentConversation.clearData()
                result()
            },
            { result() })
    }
}