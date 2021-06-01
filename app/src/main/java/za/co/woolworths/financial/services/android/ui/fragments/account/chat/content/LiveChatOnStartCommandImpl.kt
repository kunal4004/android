package za.co.woolworths.financial.services.android.ui.fragments.account.chat.content

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.LiveChatPresenter
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.LiveChatReconnectPresenter
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatAuthImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatListAllAgentConversationImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatSubscribeImpl
import za.co.woolworths.financial.services.android.util.animation.ConnectivityWatcher

class LiveChatOnStartCommandImpl : ILiveChatOnStartCommand {

    private val liveChatReconnectPresenter = LiveChatReconnectPresenter(
        LiveChatAuthImpl(),
        LiveChatSubscribeImpl(),
        LiveChatListAllAgentConversationImpl(),
        LiveChatOnStartCommandImpl(),
        LiveChatNotificationImpl()
    )

    override fun onStartConversationBySender(
        liveChatPresenter: LiveChatPresenter,
        onSuccess: (Any) -> Unit,
        onFailure: (Any) -> Unit
    ) {
        with(liveChatPresenter) {
            signIn({
                // sign In
                ChatAWSAmplify.isLiveChatActivated = true
                conversation({
                    //conversation
                    onSubscribe({ message ->
                        message?.let { msg -> onSubscribeData(msg, onSuccess) }
                    }, {
                        // subscribe error
                        onFailure(it)
                    })
                }, {
                    //conversation error
                    onFailure(it)
                })
            }, {
                onFailure(it)
                // sign in error
            })
        }
    }

    override fun reconnectToNetwork(
        context: Context,
        onSuccess: (Any) -> Unit,
        onFailure: (Any) -> Unit
    ) {
        with(liveChatReconnectPresenter) {
            ConnectivityWatcher(context).observeForever { hasConnection ->
                if (!hasConnection) {
                    ChatAWSAmplify.isConnectedToInternet = false
                    broadcastResultShowNoConnectionToast(context)
                } else {
                    if (!ChatAWSAmplify.isConnectedToInternet) {
                        ChatAWSAmplify.isConnectedToInternet = true
                        messageListFromAgent({ newMessageList ->
                            val messagesList = newMessageList.first
                            val lastMessage = newMessageList.second
                            /**
                             * TODO:: IN NEXT SPRINT, BROADCAST EVENT TO SHOW MESSAGE COUNT WHEN APP RECONNECT TO WIFI
                             */
                        }, { apiException ->
                            onFailure(apiException)
                        })
                    }
                }
            }
        }
    }

    override fun onSubscribeData(agentMessage: SendMessageResponse, onSuccess: (Any) -> Unit) {
        if (agentMessage.sessionState != SessionStateType.CONNECT || !TextUtils.isEmpty(agentMessage.content))
            ChatAWSAmplify.addChatMessageToList(agentMessage)

        if (ChatAWSAmplify.isChatActivityInForeground) {
            onSuccess(Gson().toJson(agentMessage))
        } else {
            onSuccess(agentMessage)
        }
    }

}