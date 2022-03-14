package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem

class ValidatePlace {
    @SerializedName("hasDeliverySlotReservations")
    var hasDeliverySlotReservations: Boolean? = null

    @SerializedName("unDeliverableCommerceItems")
    var unDeliverableCommerceItems: List<Any>? = null

    @SerializedName("stores")
    var stores: List<Store>? = null

    @SerializedName("quantityLimit")
    var quantityLimit: QuantityLimit? = null

    @SerializedName("deliverable")
    var deliverable: Boolean? = null

    @SerializedName("links")
    var links: List<Any>? = null

    @SerializedName("deliverySlotsDetails")
    var deliverySlotsDetails: String? = null

    @SerializedName("placeDetails")
    var placeDetails: PlaceDetails? = null

    @SerializedName("unSellableCommerceItems")
    var unSellableCommerceItems: MutableList<UnSellableCommerceItem>? = null

    @SerializedName("deliveryStatus")
    var deliveryStatus: DeliveryStatus? = null

    @SerializedName("firstAvailableFoodDeliveryDate")
    var firstAvailableFoodDeliveryDate: String? = null

    @SerializedName("firstAvailableOtherDeliveryDate")
    var firstAvailableOtherDeliveryDate: String? = null
}