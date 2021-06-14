package za.co.woolworths.financial.services.android.ui.fragments.account.chat.content


import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.LiveChatPresenter
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse

interface ILiveChatOnStartCommand {

    fun onReconnectToSubscribeAPI(
        liveChatPresenter: LiveChatPresenter,
        onSuccess: (Any) -> Unit,
        onFailure: (Any) -> Unit
    )

    fun onStartConversationBySender(
        liveChatPresenter: LiveChatPresenter,
        onSuccess: (Any) -> Unit,
        onFailure: (Any) -> Unit
    )

    fun onSubscribeData(agentMessage: SendMessageResponse, onSuccess: (Any) -> Unit)
}