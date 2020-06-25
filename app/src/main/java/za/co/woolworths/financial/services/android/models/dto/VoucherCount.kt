package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.annotations.SerializedName

data class VoucherCount(
        @SerializedName("count") val count: Int?,
        @SerializedName("response") val response: Response,
        @SerializedName("httpCode") val httpCode: Int
)