package za.co.woolworths.financial.services.android.models.dto.quick_shop

import com.google.gson.annotations.SerializedName

data class Suburb(@SerializedName("suburbDeliverable") val suburbDeliverable: Boolean, @SerializedName("postalCode") val postalCode: Int, @SerializedName("name") val name: String, @SerializedName("id") val id: Int, @SerializedName("fulfilmentTypes") val fulfilmentTypes: MutableList<FulfilmentTypes>)