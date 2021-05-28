package za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract

import com.amplifyframework.api.ApiException
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.ChatMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse

interface IListAllAgentMessage {
    fun messageListFromAgent(onSuccess:(Pair<MutableList<ChatMessage>?, SendMessageResponse?>) -> Unit, onFailure: (ApiException) -> Unit)

}