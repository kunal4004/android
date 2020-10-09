package za.co.woolworths.financial.services.android.models.dto.chat.amplify

import za.co.woolworths.financial.services.android.models.dto.chat.Collections
import za.co.woolworths.financial.services.android.models.dto.chat.CustomerService
import za.co.woolworths.financial.services.android.models.dto.chat.LiveChatEnabled
import za.co.woolworths.financial.services.android.models.dto.chat.TradingHours

data class InAppChat(
        val minimumSupportedAppBuildNumber: String,
        val apiURI: String,
        val userPoolId: String,
        val userPoolWebClientId: String,
        val collections: Collections,
        val customerService: CustomerService,
        var liveChatEnabled: LiveChatEnabled? = null,
        val tradingHours: MutableList<TradingHours>,
        var isEnabled: Boolean? = false
)