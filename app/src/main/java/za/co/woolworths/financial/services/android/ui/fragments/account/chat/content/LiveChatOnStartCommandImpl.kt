package za.co.woolworths.financial.services.android.ui.fragments.account.chat.content

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract.LiveChatPresenter
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse

class LiveChatOnStartCommandImpl : ILiveChatOnStartCommand {

    override fun onReconnectToSubscribeAPI(liveChatPresenter: LiveChatPresenter,
                                                   onSuccess: (Any) -> Unit, onFailure: (Any) -> Unit) {
        liveChatPresenter.onSubscribe({ message ->
            message?.let { msg -> onSubscribeData(msg, onSuccess) }
        }, {
            // subscribe error
            onFailure(it)
        })
    }

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