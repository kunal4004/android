package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

import za.co.woolworths.financial.services.android.models.dto.Response

class RecipientDetailsResponse {
    var recipientDetails: RecipientDetails? = null
    var slotDetails: SlotDetails? = null
    var addressDetails: AddressDetails? = null
    var httpCode: Int = 0
    var response: Response? = null
}