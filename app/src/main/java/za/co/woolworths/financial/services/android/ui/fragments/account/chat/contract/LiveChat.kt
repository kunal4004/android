package za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract

class LiveChat(private val auth: ILiveChatAuth,
               private val conversation: ILiveChatConversation,
               private val subscribe: ILiveChatSubscribe,
               private val fetchAllAgentMessages: IListAllAgentMessage)
    : ILiveChatAuth by auth,
        ILiveChatConversation by conversation,
        ILiveChatSubscribe by subscribe,
        IListAllAgentMessage by fetchAllAgentMessages