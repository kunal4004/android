package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class QuantityLimit (
    @SerializedName("foodMaximumQuantity")
    var foodMaximumQuantity: Int? = null,

    @SerializedName("other")
    var other: Int? = null,

    @SerializedName("foodLayoutColour")
    var foodLayoutColour: String? = null,

    @SerializedName("otherLayoutColour")
    var otherLayoutColour: String? = null,

    @SerializedName("food")
    var food: Int? = null,

    @SerializedName("otherMaximumQuantity")
    var otherMaximumQuantity: Int? = null,
):Serializable