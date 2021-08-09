package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

/**
 * Created by Kunal Uttarwar on 03/08/21.
 */
class SortedOtherDeliverySlot {
    @SerializedName("hourSlots")
    var hourSlots: List<String>? = null

    @SerializedName("headerDates")
    var headerDates: List<HeaderDate>? = null

    @SerializedName("week")
    var week: List<Week>? = null
}