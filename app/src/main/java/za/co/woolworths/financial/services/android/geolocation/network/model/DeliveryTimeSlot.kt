package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class DeliveryTimeSlot(

    @SerializedName("hourFrom")
    var hourFrom: String? = null,

    @SerializedName("hourTo")
    var hourTo: String? = null,

    @SerializedName("stringShipOnDate")
    var stringShipOnDate: String? = null,

    @SerializedName("intHourFrom")
    var intHourFrom: Int? = null,

    @SerializedName("description")
    var description: String? = null,

    @SerializedName("slotId")
    var slotId: String? = null,

    @SerializedName("details")
    var details: String? = null,

    @SerializedName("selected")
    var selected: Boolean? = null,

    @SerializedName("slotCost")
    var slotCost: Int? = null,
) : Serializable