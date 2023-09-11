package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import com.google.gson.annotations.SerializedName


data class Price (

  @SerializedName("amount"        ) var amount        : Int? = null,
  @SerializedName("wasPrice"      ) var wasPrice      : Int? = null,
  @SerializedName("rawTotalPrice" ) var rawTotalPrice : Int? = null,
  @SerializedName("salePrice"     ) var salePrice     : Int? = null,
  @SerializedName("listPrice"     ) var listPrice     : Int? = null

)