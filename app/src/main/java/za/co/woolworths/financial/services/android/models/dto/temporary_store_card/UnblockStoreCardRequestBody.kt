package za.co.woolworths.financial.services.android.models.dto.temporary_store_card

data class UnblockStoreCardRequestBody(val visionAccountNumber: String, val cardNumber: String, val sequenceNumber: String, val otp: String, val otpMethod: String)