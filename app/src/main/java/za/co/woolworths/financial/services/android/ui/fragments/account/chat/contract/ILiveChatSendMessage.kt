package za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract

import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType

interface ILiveChatSendMessage {
    fun send(sessionState: SessionStateType, content: String)
}