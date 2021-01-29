package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

import za.co.woolworths.financial.services.android.models.dto.Response

class AvailableTimeSlotsResponse {
    var httpCode: Int = 0
    var response: Response? = null
    val timeslots: List<TimeSlot>? = null
}