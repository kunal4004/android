package za.co.woolworths.financial.services.android.models.dto.npc

data class BlockCardRequestBody(val visionAccountNumber: String, val cardNumber: String, val sequenceNumber: String, val blockReason: String)