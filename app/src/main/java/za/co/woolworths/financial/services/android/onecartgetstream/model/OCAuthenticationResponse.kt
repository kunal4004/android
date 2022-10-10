package za.co.woolworths.financial.services.android.onecartgetstream.model

import com.google.gson.annotations.SerializedName

data class OCAuthenticationResponse(@SerializedName("details") val details: Details,
                                    @SerializedName("httpCode") val httpCode: Int){
    data class Details(
            @SerializedName("token") val token: String,
            @SerializedName("name") val name: String,
            @SerializedName("userID") val userId: String,
            @SerializedName("isOCShopper") val isOCShopper: Boolean
    )
}
