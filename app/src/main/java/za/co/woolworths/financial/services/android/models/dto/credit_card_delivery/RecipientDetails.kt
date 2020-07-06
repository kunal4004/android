package za.co.woolworths.financial.services.android.models.dto.credit_card_delivery

data class RecipientDetails(var telWork: String?, var telCell: String?, var deliverTo: String?, var idNumber: String?, var isThirdPartyRecipient: Boolean? = false)