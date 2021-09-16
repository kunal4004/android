package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

class SortedJoinDeliverySlot {
    @SerializedName("hourSlots")
    var hourSlots: List<String>? = null

    @SerializedName("headerDates")
    var headerDates: List<HeaderDate>? = null

    @SerializedName("week")
    var week: List<Week>? = null
}