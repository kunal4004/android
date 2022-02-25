package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Response {
    @SerializedName("code")
    @Expose
    var code: String? = null

    @SerializedName("desc")
    @Expose
    var desc: String? = null
}