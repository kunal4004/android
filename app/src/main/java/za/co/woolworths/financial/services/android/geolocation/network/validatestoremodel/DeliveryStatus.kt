package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeliveryStatus(
  var one: Boolean? = null,
  var two: Boolean? = null,
  var seven: Boolean? = null,
) : Parcelable