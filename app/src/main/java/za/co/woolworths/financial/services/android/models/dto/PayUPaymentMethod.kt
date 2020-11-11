package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.annotations.SerializedName

data class PayUPaymentMethod(
        @SerializedName("token") val token: String,
        @SerializedName("creditCardCvv") val creditCardCvv: String,
        @SerializedName("type") val type: String
)