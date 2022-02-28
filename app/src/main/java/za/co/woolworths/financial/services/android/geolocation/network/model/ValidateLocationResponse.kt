package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace

class ValidateLocationResponse {
    @SerializedName("validatePlace")
    @Expose
    var validatePlace: ValidatePlace? = null

    @SerializedName("response")
    @Expose
    var response: Response? = null

    @SerializedName("httpCode")
    @Expose
    var httpCode: Int? = null
}