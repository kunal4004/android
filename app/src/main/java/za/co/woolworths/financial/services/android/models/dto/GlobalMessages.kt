package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.annotations.SerializedName

data class GlobalMessages(
        @SerializedName("qualifierMessages") val qualifierMessages: List<String>,
        @SerializedName("deliveryMessage") val deliveryMessage: String
)