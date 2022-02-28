package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Store {
    @SerializedName("unDeliverableCommerceItems")
    @Expose
    var unDeliverableCommerceItems: List<Any>? = null

    @SerializedName("distance")
    @Expose
    var distance: Int? = null

    @SerializedName("deliverable")
    @Expose
    var deliverable: Boolean? = null

    @SerializedName("storeId")
    @Expose
    var storeId: String? = null

    @SerializedName("deliverySlotsDetails")
    @Expose
    var deliverySlotsDetails: String? = null

    @SerializedName("firstAvailableFoodDeliveryDate")
    @Expose
    var firstAvailableFoodDeliveryDate: String? = null

    @SerializedName("firstAvailableOtherDeliveryDate")
    @Expose
    var firstAvailableOtherDeliveryDate: String? = null

    @SerializedName("storeAddress")
    @Expose
    var storeAddress: String? = null

    @SerializedName("quantityLimit")
    @Expose
    var quantityLimit: QuantityLimit? = null

    @SerializedName("storeName")
    @Expose
    var storeName: String? = null

    @SerializedName("storeDeliveryType")
    @Expose
    var storeDeliveryType: String? = null

    @SerializedName("unSellableCommerceItems")
    @Expose
    var unSellableCommerceItems: List<Any>? = null

    @SerializedName("deliveryStatus")
    @Expose
    var deliveryStatus: DeliveryStatus? = null
}