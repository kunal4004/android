package za.co.woolworths.financial.services.android.models.dto.npc

import com.google.gson.annotations.SerializedName

data class LinkStoreCard(
        @SerializedName("productOfferingId") val productOfferingId: Int,
        @SerializedName("visionAccountNumber") val visionAccountNumber: String,
        @SerializedName("cardNumber") val cardNumber: String?,
        @SerializedName("sequenceNumber") val sequenceNumber: Int,
        @SerializedName("otp") val otp: String,
        @SerializedName("otpMethod") val otpMethod: OTPMethodType?,
        @SerializedName("linkType") var linkType: String = "SC"
)