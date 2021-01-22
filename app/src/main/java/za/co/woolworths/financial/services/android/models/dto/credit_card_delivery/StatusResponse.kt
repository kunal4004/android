package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

data class StatusResponse(val deliveryStatus: DeliveryStatus?, val bookingreference: String?, var slotDetails: SlotDetails, var recipientDetails: RecipientDetails?, var addressDetails: AddressDetails?, var appointment: AppointmentDetails)