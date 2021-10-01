package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

/**
 * Created by Kunal Uttarwar on 28/09/21.
 */
class TimedDeliveryFirstAvailableDates {
    @SerializedName("join")
    var join: String? = null

    @SerializedName("food")
    var food: String? = null

    @SerializedName("other")
    var other: String? = null
}