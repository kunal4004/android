package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

class TimedDeliveryStartDates {
    @SerializedName("join")
    var join: String? = null

    @SerializedName("food")
    var food: String? = null

    @SerializedName("other")
    var other: String? = null
}