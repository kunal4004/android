package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Response(
  var code: String? = null,
  var desc: String? = null,
) : Parcelable