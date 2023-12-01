package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class PlaceDetails (
    var address1: String? = null,
    var latitude: Double? = null,
    var nickname: String? = null,
    var placeId: String? = null,
    var id: String? = null,
    var longitude: Double? = null,
): Parcelable