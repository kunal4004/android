package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class OpenDayDeliverySlot : Serializable {
    @SerializedName("deliverySlotId")
    var deliverySlotId: String? = null

    @SerializedName("deliveryInDays")
    var deliveryInDays: String? = null

    @SerializedName("amount")
    var amount: Long? = null

    @SerializedName("deliveryType")
    var deliveryType: String? = null

    var description: String? = null
}
