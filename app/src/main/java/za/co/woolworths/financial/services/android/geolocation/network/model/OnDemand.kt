package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.checkout.service.network.DeliveryStatus
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import java.io.Serializable

class OnDemand : Serializable {
    @SerializedName("unDeliverableCommerceItems")
    var unDeliverableCommerceItems: List<Any?>? = null

    @SerializedName("firstAvailableFoodDeliveryTime")
    var firstAvailableFoodDeliveryTime: String? = null

    @SerializedName("quantityLimit")
    var quantityLimit: QuantityLimit? = null

    @SerializedName("deliverable")
    var deliverable: Boolean? = null

    @SerializedName("storeName")
    var storeName: String? = null

    @SerializedName("deliveryTimeSlots")
    var deliveryTimeSlots: List<DeliveryTimeSlot?>? = null

    @SerializedName("storeId")
    var storeId: String? = null

    @SerializedName("firstAvailableFoodDeliveryCost")
    var firstAvailableFoodDeliveryCost: Int? = null

    @SerializedName("unSellableCommerceItems")
    var unSellableCommerceItems: MutableList<UnSellableCommerceItem>? = null

    @SerializedName("deliveryStatus")
    var deliveryStatus: DeliveryStatus? = null

    @SerializedName("deliveryDetails")
    var deliveryDetails: String? = null
}