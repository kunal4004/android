package za.co.woolworths.financial.services.android.geolocation.network.model

import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import java.io.Serializable

data class Store(
    var unDeliverableCommerceItems: List<Any>? = null,
    var distance: Int? = null,
    var deliverable: Boolean? = null,
    var storeId: String? = null,
    var deliverySlotsDetails: String? = null,
    var firstAvailableFoodDeliveryDate: String? = null,
    var firstAvailableOtherDeliveryDate: String? = null,
    var storeAddress: String? = null,
    var quantityLimit: QuantityLimit? = null,
    var storeName: String? = null,
    var storeDeliveryType: String? = null,
    var unSellableCommerceItems: MutableList<UnSellableCommerceItem>? = null,
    var locationId: String? = "",
    var longitude: Double? = null,
    var latitude: Double? = null,
    var deliveryDetails: String? = null,
) : Serializable