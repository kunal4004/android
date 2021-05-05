package za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract

import com.amplifyframework.api.ApiException
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.ChatMessage

interface IListAllAgentMessage {
    fun list(onSuccess: (MutableList<ChatMessage>?) -> Unit, onFailure: (ApiException) -> Unit)
}