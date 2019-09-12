package za.co.woolworths.financial.services.android.models.dto.npc

import com.google.gson.annotations.SerializedName

data class LinkNewCard(
        @SerializedName("productOfferingId") val productOfferingId: Int,
        @SerializedName("visionAccountNumber") val visionAccountNumber: Int,
        @SerializedName("cardNumber") val cardNumber: Int,
        @SerializedName("sequenceNumber") val sequenceNumber: Int,
        @SerializedName("otp") val otp: Int,
        @SerializedName("otpMethod") val otpMethod: String
)