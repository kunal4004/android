package za.co.woolworths.financial.services.android.models.dto.chat

import za.co.woolworths.financial.services.android.models.dto.ChatState
import za.co.woolworths.financial.services.android.models.dto.Response

class PollChatSessionStateResponse {

    var chatState: ChatState? = null
    var httpCode: Int = 0
    var response: Response? = null
}