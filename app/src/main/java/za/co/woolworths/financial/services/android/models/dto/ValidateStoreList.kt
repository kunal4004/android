package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.checkout.service.network.DeliveryStatus

/**
 * Created by Kunal Uttarwar on 14/07/21.
 */
class ValidateStoreList {
    @SerializedName("unDeliverableCommerceItems")
    var unDeliverableCommerceItems: List<Any>? = null

    @SerializedName("storeAddress")
    var storeAddress: String? = null

    @SerializedName("quantityLimit")
    var quantityLimit: QuantityLimit? = null

    @SerializedName("navSuburbDetails")
    var navSuburbDetails: String? = null

    @SerializedName("deliverable")
    var deliverable: Boolean? = null

    @SerializedName("storeName")
    var storeName: String? = null

    @SerializedName("storeId")
    var storeId: String? = null

    @SerializedName("deliverySlotsDetails")
    var deliverySlotsDetails: String? = null

    @SerializedName("unSellableCommerceItems")
    var unSellableCommerceItems: List<Any>? = null

    @SerializedName("deliveryStatus")
    var deliveryStatus: DeliveryStatus? = null

    @SerializedName("firstAvailableFoodDeliveryDate")
    var firstAvailableFoodDeliveryDate: String? = null
}