package za.co.woolworths.financial.services.android.models.dto.order_again

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OrderAgainResponse {
    @SerializedName("meta")
    @Expose
    var meta: Meta? = null

    @SerializedName("data")
    @Expose
    var data: Data? = null

    @SerializedName("response")
    @Expose
    var response: Response? = null

    @SerializedName("httpCode")
    @Expose
    var httpCode: Int? = null
}