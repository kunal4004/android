package za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract

import com.amplifyframework.api.ApiException
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.GetMessagesByConversation

interface IListAllAgentMessage {
    fun list(onSuccess: (GetMessagesByConversation?) -> Unit, onFailure: (ApiException) -> Unit)
}