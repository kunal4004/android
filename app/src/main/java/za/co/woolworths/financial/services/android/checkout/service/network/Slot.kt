package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

class Slot {

    @SerializedName("slotCost")
    var slotCost: Int? = null

    @SerializedName("slotId")
    var slotId: String? = null

    @SerializedName("shipOnDate")
    var shipOnDate: Long? = null

    @SerializedName("stringShipOnDate")
    var stringShipOnDate: String? = null

    @SerializedName("freeDeliverySlot")
    var freeDeliverySlot: Boolean? = null

    @SerializedName("hasReservation")
    var hasReservation: Boolean? = null

    @SerializedName("selected")
    var selected: Boolean? = null

    @SerializedName("available")
    var available: Boolean? = null

    @SerializedName("hourFrom")
    var hourFrom: String? = null

    @SerializedName("hourSlot")
    var hourSlot: String? = null
}