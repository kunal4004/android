package za.co.woolworths.financial.services.android.models.dto.chat

import za.co.woolworths.financial.services.android.models.dto.Response


class AgentsAvailableResponse {
    var agentsAvailable: Boolean = false
    var httpCode: Int = 0
    var response: Response? = null
}