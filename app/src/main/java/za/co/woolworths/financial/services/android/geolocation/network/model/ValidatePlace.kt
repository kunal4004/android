package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.geolocation.network.model.QuantityLimit__1
import za.co.woolworths.financial.services.android.geolocation.network.model.PlaceDetails
import za.co.woolworths.financial.services.android.geolocation.network.model.DeliveryStatus__1

class ValidatePlace {
    @SerializedName("hasDeliverySlotReservations")
    @Expose
    var hasDeliverySlotReservations: Boolean? = null

    @SerializedName("unDeliverableCommerceItems")
    @Expose
    var unDeliverableCommerceItems: List<Any>? = null

    @SerializedName("stores")
    @Expose
    var stores: List<Store>? = null

    @SerializedName("quantityLimit")
    @Expose
    var quantityLimit: QuantityLimit__1? = null

    @SerializedName("deliverable")
    @Expose
    var deliverable: Boolean? = null

    @SerializedName("links")
    @Expose
    var links: List<Any>? = null

    @SerializedName("deliverySlotsDetails")
    @Expose
    var deliverySlotsDetails: String? = null

    @SerializedName("placeDetails")
    @Expose
    var placeDetails: PlaceDetails? = null

    @SerializedName("unSellableCommerceItems")
    @Expose
    var unSellableCommerceItems: List<Any>? = null

    @SerializedName("deliveryStatus")
    @Expose
    var deliveryStatus: DeliveryStatus__1? = null

    @SerializedName("firstAvailableFoodDeliveryDate")
    @Expose
    var firstAvailableFoodDeliveryDate: String? = null

    @SerializedName("firstAvailableOtherDeliveryDate")
    @Expose
    var firstAvailableOtherDeliveryDate: String? = null
}