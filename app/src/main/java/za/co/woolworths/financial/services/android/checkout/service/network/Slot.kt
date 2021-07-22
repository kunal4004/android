package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

class Slot {
    @SerializedName("dayOfMonth")
    var dayOfMonth: Int? = null

    @SerializedName("hourFrom")
    var hourFrom: String? = null

    @SerializedName("hourTo")
    var hourTo: String? = null

    @SerializedName("shipOnDate")
    var shipOnDate: Long? = null

    @SerializedName("description")
    var description: String? = null

    @SerializedName("slotId")
    var slotId: String? = null

    @SerializedName("joinSlotId")
    var joinSlotId: String? = null

    @SerializedName("stringShipOnDate")
    var stringShipOnDate: String? = null

    @SerializedName("rushDelSlot")
    var rushDelSlot: Boolean? = null

    @SerializedName("corporateSlot")
    var corporateSlot: Boolean? = null

    @SerializedName("slotCost")
    var slotCost: Int? = null

    @SerializedName("freeDeliverySlot")
    var freeDeliverySlot: Boolean? = null

    @SerializedName("hasReservation")
    var hasReservation: Boolean? = null

    @SerializedName("selected")
    var selected: Boolean? = null

    @SerializedName("available")
    var available: Boolean? = null

    @SerializedName("intHourFrom")
    var intHourFrom: Int? = null

    @SerializedName("intHourTo")
    var intHourTo: Int? = null
}