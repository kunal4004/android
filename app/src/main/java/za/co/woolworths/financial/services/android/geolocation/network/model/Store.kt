package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Store (
    @SerializedName("unDeliverableCommerceItems")
    var unDeliverableCommerceItems: List<Any>? = null,

    @SerializedName("distance")
    var distance: Int? = null,

    @SerializedName("deliverable")
    var deliverable: Boolean? = null,

    @SerializedName("storeId")
    var storeId: String? = null,

    @SerializedName("deliverySlotsDetails")
    var deliverySlotsDetails: String? = null,

    @SerializedName("firstAvailableFoodDeliveryDate")
    var firstAvailableFoodDeliveryDate: String? = null,

    @SerializedName("firstAvailableOtherDeliveryDate")
    var firstAvailableOtherDeliveryDate: String? = null,

    @SerializedName("storeAddress")
    var storeAddress: String? = null,

    @SerializedName("quantityLimit")
    var quantityLimit: QuantityLimit? = null,

    @SerializedName("storeName")
    var storeName: String? = null,

    @SerializedName("storeDeliveryType")
    var storeDeliveryType: String? = null,

    @SerializedName("unSellableCommerceItems")
    var unSellableCommerceItems: List<Any>? = null,


    @SerializedName("longitude")
    var longitude: Double?= null,

    @SerializedName("latitude")
    var latitude: Double?= null,
) : Serializable