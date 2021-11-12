package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

class Week {
    @SerializedName("date")
    var date: String? = null

    @SerializedName("daySlots")
    var slots: List<Slot>? = null
}