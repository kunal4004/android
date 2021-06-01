package za.co.woolworths.financial.services.android.ui.fragments.account.chat.content

import android.content.Context
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.LiveChatPresenter
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse

interface ILiveChatOnStartCommand {
    fun onStartConversationBySender(
        liveChatPresenter: LiveChatPresenter,
        onSuccess: (Any) -> Unit,
        onFailure: (Any) -> Unit
    )

    fun onSubscribeData(agentMessage: SendMessageResponse, onSuccess: (Any) -> Unit)
    fun reconnectToNetwork(context: Context,onSuccess: (Any) -> Unit, onFailure: (Any) -> Unit)
}