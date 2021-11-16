package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

class HourSlots {
    @SerializedName("slotNum")
    var slotNum: Int = -1

    @SerializedName("slot")
    var slot: String? = null
}