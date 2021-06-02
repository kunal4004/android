package za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract

import za.co.woolworths.financial.services.android.ui.fragments.account.chat.content.ILiveChatNotification
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.content.ILiveChatOnStartCommand

class LiveChatPresenter(
    private val auth: ILiveChatAuth,
    private val conversation: ILiveChatConversation,
    private val subscribe: ILiveChatSubscribe,
    private val agentMessages: IListAllAgentMessage,
    private val content: ILiveChatOnStartCommand,
    private val liveChatNotification: ILiveChatNotification
) : ILiveChatAuth by auth,
    ILiveChatConversation by conversation,
    ILiveChatSubscribe by subscribe,
    IListAllAgentMessage by agentMessages,
    ILiveChatOnStartCommand by content,
    ILiveChatNotification by liveChatNotification

class LiveChatReconnectPresenter(
    private val auth: ILiveChatAuth,
    private val subscribe: ILiveChatSubscribe,
    private val agentMessages: IListAllAgentMessage,
    private val liveChatNotification: ILiveChatNotification
) : ILiveChatAuth by auth,
    ILiveChatSubscribe by subscribe,
    IListAllAgentMessage by agentMessages,
    ILiveChatNotification by liveChatNotification