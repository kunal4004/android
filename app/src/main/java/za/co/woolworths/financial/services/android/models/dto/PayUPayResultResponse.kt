package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PayUPayResultResponse(
        @SerializedName("status") val status: String,
        @SerializedName("paymentSuccessful") val paymentSuccessful: Boolean,
        @SerializedName("paymentId") val paymentId: String,
        @SerializedName("amount") val amount: Int,
        @SerializedName("response") val response: Response,
        @SerializedName("httpCode") val httpCode: Int
) : Serializable