package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Response : Serializable {
    @SerializedName("code")
    var code: String? = null

    @SerializedName("desc")
    var desc: String? = null
}