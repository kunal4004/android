package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

data class DeliveryStatus(val code: String?, var statusDescription: String?, val displayTitle: String, val displayCopy: String?, val displayColour: String?, val isCardNew: Boolean, val isEditable: Boolean, val isCancellable: Boolean, val receivedDate: String)