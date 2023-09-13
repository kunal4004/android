package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import za.co.woolworths.financial.services.android.geolocation.network.model.Store


@Parcelize
data class ValidatePlace(
  var hasDeliverySlotReservations: Boolean? = null,
  var unDeliverableCommerceItems: ArrayList<String> = arrayListOf(),
  var stores: ArrayList<Store> = arrayListOf(),
  var deliverable: Boolean? = null,
  var firstAvailableFoodDeliveryDate: String? = null,
  var firstAvailableOtherDeliveryDate: String? = null,
  var cacheKey: String? = null,
  var deliveryDetails: String? = null,
  var quantityLimit: QuantityLimit? = QuantityLimit(),
  var links: ArrayList<String> = arrayListOf(),
  var liquorDeliverable: Boolean? = null,
  var placeDetails: PlaceDetails? = PlaceDetails(),
  var unSellableCommerceItems: ArrayList<String> = arrayListOf(),
  var deliveryStatus: DeliveryStatus? = DeliveryStatus(),

  ) : Parcelable