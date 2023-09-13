package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class QuantityLimit(

  var foodMaximumQuantity: Int? = null,
  var other: Int? = null,
  var foodLayoutColour: String? = null,
  var otherLayoutColour: String? = null,
  var food: Int? = null,
  var otherMaximumQuantity: Int? = null,
) : Parcelable