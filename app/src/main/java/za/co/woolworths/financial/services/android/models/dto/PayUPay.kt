package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.annotations.SerializedName

data class PayUPay(
        @SerializedName("amount") val amount: Int,
        @SerializedName("currency") val currency: String,
        @SerializedName("productOfferingId") val productOfferingId: Int,
        @SerializedName("saveCard") val saveCard: Boolean,
        @SerializedName("paymentMethod") val paymentMethod: PayUPaymentMethod,
        @SerializedName("accountNumber") val accountNumber: String
)