package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Week : Serializable {
    @SerializedName("date")
    var date: String? = null

    @SerializedName("daySlots")
    var slots: List<Slot>? = null
}