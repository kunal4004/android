package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import com.google.gson.annotations.SerializedName


data class UnSellableCommerceItems (

  @SerializedName("quantity"              ) var quantity              : Int?    = null,
  @SerializedName("productId"             ) var productId             : String? = null,
  @SerializedName("displayCategory"       ) var displayCategory       : String? = null,
  @SerializedName("internalImageURL"      ) var internalImageURL      : String? = null,
  @SerializedName("catalogRefId"          ) var catalogRefId          : String? = null,
  @SerializedName("commerceItemClassType" ) var commerceItemClassType : String? = null,
  @SerializedName("colour"                ) var colour                : String? = null,
  @SerializedName("detailPageURL"         ) var detailPageURL         : String? = null,
  @SerializedName("size"                  ) var size                  : String? = null,
  @SerializedName("productVariant"        ) var productVariant        : String? = null,
  @SerializedName("price"                 ) var price                 : Price?  = Price(),
  @SerializedName("externalImageURL"      ) var externalImageURL      : String? = null,
  @SerializedName("productDisplayName"    ) var productDisplayName    : String? = null,
  @SerializedName("fulfillerType"         ) var fulfillerType         : String? = null,
  @SerializedName("productType"           ) var productType           : String? = null,
  @SerializedName("externalImageRefV2"    ) var externalImageRefV2    : String? = null

)