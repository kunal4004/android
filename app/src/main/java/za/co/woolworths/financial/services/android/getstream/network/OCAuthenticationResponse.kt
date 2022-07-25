package za.co.woolworths.financial.services.android.getstream.network

import com.google.gson.annotations.SerializedName

data class OCAuthenticationResponse(@SerializedName("details") val details: Details){
    data class Details(
            @SerializedName("token") val token: String,
            @SerializedName("name") val name: String,
            @SerializedName("userID") val userId: String,
            @SerializedName("isOCShopper") val isOCShopper: Boolean
    )
}
