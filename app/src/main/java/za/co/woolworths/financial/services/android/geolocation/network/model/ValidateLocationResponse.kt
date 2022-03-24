package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ValidateLocationResponse (
    @SerializedName("validatePlace")
    var validatePlace: ValidatePlace? = null,
    @SerializedName("response")
    var response: Response? = null,
    @SerializedName("httpCode")
    var httpCode: Int? = null
):Serializable