package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class QuantityLimit__1 {
    @SerializedName("foodMaximumQuantity")
    @Expose
    var foodMaximumQuantity: Int? = null

    @SerializedName("other")
    @Expose
    var other: Int? = null

    @SerializedName("foodLayoutColour")
    @Expose
    var foodLayoutColour: String? = null

    @SerializedName("otherLayoutColour")
    @Expose
    var otherLayoutColour: String? = null

    @SerializedName("food")
    @Expose
    var food: Int? = null

    @SerializedName("otherMaximumQuantity")
    @Expose
    var otherMaximumQuantity: Int? = null
}