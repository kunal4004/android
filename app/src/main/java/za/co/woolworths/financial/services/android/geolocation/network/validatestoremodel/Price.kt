package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Price(
  var amount: Int? = null,
  var wasPrice: Int? = null,
  var rawTotalPrice: Int? = null,
  var salePrice: Int? = null,
  var listPrice: Int? = null,
) : Parcelable