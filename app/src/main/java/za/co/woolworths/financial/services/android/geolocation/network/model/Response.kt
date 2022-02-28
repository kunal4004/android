package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.SerializedName

class Response {
    @SerializedName("code")
    var code: String? = null

    @SerializedName("desc")
    var desc: String? = null
}