package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ShippingDetailsResponse : Serializable {
    @SerializedName("jsessionId")
    var jsessionId: String? = null

    @SerializedName("auth")
    var auth: String? = null

    @SerializedName("response")
    var response: Response? = null

    @SerializedName("httpCode")
    var httpCode: Int? = null
}