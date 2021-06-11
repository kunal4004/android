package za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract

import com.amplifyframework.api.ApiException

interface ILiveChatConversation {
    fun conversation(onSuccess: () -> Unit, onFailure: (ApiException) -> Unit)
}