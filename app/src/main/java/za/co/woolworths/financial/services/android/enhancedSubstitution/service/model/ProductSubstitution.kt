package za.co.woolworths.financial.services.android.enhancedSubstitution.service.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ProductSubstitution(
    @SerializedName("data")
        @Expose
        var data: List<Data>,
    @SerializedName("httpCode")
        @Expose
        val httpCode: Int,
    @SerializedName("response")
        @Expose
        val response: Response
)