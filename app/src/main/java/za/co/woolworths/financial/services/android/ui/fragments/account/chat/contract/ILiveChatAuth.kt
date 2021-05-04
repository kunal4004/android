package za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract

import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.result.AuthSignInResult

interface ILiveChatAuth {
    fun signIn(onSuccess: (AuthSignInResult) -> Unit, onFailure: (AuthException) -> Unit)
    fun signOut(onSuccess: () -> Unit,onFailure: (AuthException) -> Unit)
}