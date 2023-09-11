package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Stores(
    var unDeliverableCommerceItems: ArrayList<String> = arrayListOf(),
    var distance: Int? = null,
    var latitude: Double? = null,
    var deliverable: Boolean? = null,
    var storeId: String? = null,
    var deliverySlotsDetails: String? = null,
    var firstAvailableFoodDeliveryDate: String? = null,
    var firstAvailableOtherDeliveryDate: String? = null,
    var storeAddress: String? = null,
    var locationId: String? = null,
    var deliveryDetails: String? = null,
    var quantityLimit: QuantityLimit? = QuantityLimit(),
    var storeName: String? = null,
    var storeDeliveryType: String? = null,
    var unSellableCommerceItems: ArrayList<UnSellableCommerceItems> = arrayListOf(),
    var deliveryStatus: DeliveryStatus? = DeliveryStatus(),
    var longitude: Double? = null,

    ) : Parcelable