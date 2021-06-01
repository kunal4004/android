package za.co.woolworths.financial.services.android.models.dto.chat.amplify

import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse

data class GetMessagesByConversation(val items: MutableList<SendMessageResponse>)