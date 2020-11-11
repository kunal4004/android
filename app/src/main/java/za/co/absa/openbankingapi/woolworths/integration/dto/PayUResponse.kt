package za.co.absa.openbankingapi.woolworths.integration.dto

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.Response

data class PayUResponse(
        @SerializedName("redirection") val redirection: PMARedirection,
        @SerializedName("response") val response: Response,
        @SerializedName("httpCode") val httpCode: Int
)