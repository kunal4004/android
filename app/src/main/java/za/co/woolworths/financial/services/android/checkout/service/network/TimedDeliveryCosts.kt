package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

class TimedDeliveryCosts {
    @SerializedName("join")
    var join: Int? = null

    @SerializedName("food")
    var food: Int? = null

    @SerializedName("other")
    var other: Int? = null
}