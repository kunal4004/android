package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import com.google.gson.annotations.SerializedName


data class Stores (

  @SerializedName("unDeliverableCommerceItems"      ) var unDeliverableCommerceItems      : ArrayList<String>                  = arrayListOf(),
  @SerializedName("distance"                        ) var distance                        : Int?                               = null,
  @SerializedName("latitude"                        ) var latitude                        : Double?                            = null,
  @SerializedName("deliverable"                     ) var deliverable                     : Boolean?                           = null,
  @SerializedName("storeId"                         ) var storeId                         : String?                            = null,
  @SerializedName("deliverySlotsDetails"            ) var deliverySlotsDetails            : String?                            = null,
  @SerializedName("firstAvailableFoodDeliveryDate"  ) var firstAvailableFoodDeliveryDate  : String?                            = null,
  @SerializedName("firstAvailableOtherDeliveryDate" ) var firstAvailableOtherDeliveryDate : String?                            = null,
  @SerializedName("storeAddress"                    ) var storeAddress                    : String?                            = null,
  @SerializedName("locationId"                      ) var locationId                      : String?                            = null,
  @SerializedName("deliveryDetails"                 ) var deliveryDetails                 : String?                            = null,
  @SerializedName("quantityLimit"                   ) var quantityLimit                   : QuantityLimit?                     = QuantityLimit(),
  @SerializedName("storeName"                       ) var storeName                       : String?                            = null,
  @SerializedName("storeDeliveryType"               ) var storeDeliveryType               : String?                            = null,
  @SerializedName("unSellableCommerceItems"         ) var unSellableCommerceItems         : ArrayList<UnSellableCommerceItems> = arrayListOf(),
  @SerializedName("deliveryStatus"                  ) var deliveryStatus                  : DeliveryStatus?                    = DeliveryStatus(),
  @SerializedName("longitude"                       ) var longitude                       : Double?                            = null

)