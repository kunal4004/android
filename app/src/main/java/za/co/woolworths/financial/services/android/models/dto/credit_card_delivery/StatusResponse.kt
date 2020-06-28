package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

data class StatusResponse(val deliveryStatus: DeliveryStatus?, val isCardNew: Boolean?, val appointment: Appointment?, var name: String?, var deliverTo: String?, var isThirdPartyRecipient: Boolean?, var receivedDate: String?, var deliveredDate: String?)