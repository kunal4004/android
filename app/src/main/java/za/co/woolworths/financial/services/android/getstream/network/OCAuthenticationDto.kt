package za.co.woolworths.financial.services.android.getstream.network

import com.google.gson.annotations.SerializedName

data class OCAuthenticationDto(
        @SerializedName("customerID") val atgId: String,
        @SerializedName("fullName") val fullName: String
)
