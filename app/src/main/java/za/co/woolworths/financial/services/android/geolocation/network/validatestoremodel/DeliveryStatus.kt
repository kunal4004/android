package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import com.google.gson.annotations.SerializedName


data class DeliveryStatus (

  @SerializedName("01" ) var one : Boolean? = null,
  @SerializedName("02" ) var two : Boolean? = null,
  @SerializedName("07" ) var seven : Boolean? = null

)