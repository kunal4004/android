package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import com.google.gson.annotations.SerializedName


data class ValidateStoreResponse(

  @SerializedName("validatePlace") var validatePlace: ValidatePlace? = ValidatePlace(),
  @SerializedName("response") var response: Response? = Response(),
  @SerializedName("httpCode") var httpCode: Int? = null,

  )