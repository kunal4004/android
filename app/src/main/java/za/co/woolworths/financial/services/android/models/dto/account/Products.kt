package za.co.woolworths.financial.services.android.models.dto.account

import com.google.gson.annotations.SerializedName

data class Products(
        @SerializedName("productGroupCode") val productGroupCode: String,
        @SerializedName("productOfferingId") val productOfferingId: Int
)