package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import java.io.Serializable

data class ValidatePlace (
    @SerializedName("hasDeliverySlotReservations")
    var hasDeliverySlotReservations: Boolean? = null,

    @SerializedName("unDeliverableCommerceItems")
    var unDeliverableCommerceItems: List<Any>? = null,

    @SerializedName("onDemand")
    var onDemand: OnDemand? = null,

    @SerializedName("stores")
    var stores: List<Store>? = null,

    @SerializedName("quantityLimit")
    var quantityLimit: QuantityLimit? = null,

    @SerializedName("deliverable")
    var deliverable: Boolean? = null,

    @SerializedName("links")
    var links: List<Any>? = null,

    @SerializedName("deliverySlotsDetails")
    var deliverySlotsDetails: String? = null,

    @SerializedName("placeDetails")
    var placeDetails: PlaceDetails? = null,

    @SerializedName("unSellableCommerceItems")
    var unSellableCommerceItems: MutableList<UnSellableCommerceItem>? = null,

    @SerializedName("firstAvailableFoodDeliveryDate")
    var firstAvailableFoodDeliveryDate: String? = null,

    @SerializedName("firstAvailableOtherDeliveryDate")
    var firstAvailableOtherDeliveryDate: String? = null,

    @SerializedName("deliveryDetails")
    var deliveryDetails: String? = null
): Serializable