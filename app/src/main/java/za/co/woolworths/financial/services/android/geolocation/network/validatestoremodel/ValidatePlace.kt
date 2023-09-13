package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.geolocation.network.model.Store


data class ValidatePlace (

  @SerializedName("hasDeliverySlotReservations"     ) var hasDeliverySlotReservations     : Boolean?          = null,
  @SerializedName("unDeliverableCommerceItems"      ) var unDeliverableCommerceItems      : ArrayList<String> = arrayListOf(),
  @SerializedName("stores"                          ) var stores                          : ArrayList<Store> = arrayListOf(),
  @SerializedName("deliverable"                     ) var deliverable                     : Boolean?          = null,
  @SerializedName("firstAvailableFoodDeliveryDate"  ) var firstAvailableFoodDeliveryDate  : String?           = null,
  @SerializedName("firstAvailableOtherDeliveryDate" ) var firstAvailableOtherDeliveryDate : String?           = null,
  @SerializedName("cacheKey"                        ) var cacheKey                        : String?           = null,
  @SerializedName("deliveryDetails"                 ) var deliveryDetails                 : String?           = null,
  @SerializedName("quantityLimit"                   ) var quantityLimit                   : QuantityLimit?    = QuantityLimit(),
  @SerializedName("links"                           ) var links                           : ArrayList<String> = arrayListOf(),
  @SerializedName("liquorDeliverable"               ) var liquorDeliverable               : Boolean?          = null,
  @SerializedName("placeDetails"                    ) var placeDetails                    : PlaceDetails?     = PlaceDetails(),
  @SerializedName("unSellableCommerceItems"         ) var unSellableCommerceItems         : ArrayList<String> = arrayListOf(),
  @SerializedName("deliveryStatus"                  ) var deliveryStatus                  : DeliveryStatus?   = DeliveryStatus()

)