package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import com.google.gson.annotations.SerializedName


data class Response (

  @SerializedName("code" ) var code : String? = null,
  @SerializedName("desc" ) var desc : String? = null

)