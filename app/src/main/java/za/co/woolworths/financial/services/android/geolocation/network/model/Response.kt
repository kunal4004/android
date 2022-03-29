package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Response(
    @SerializedName("code")
    var code: String? = null,
    @SerializedName("desc")
    var desc: String? = null,

) : Serializable