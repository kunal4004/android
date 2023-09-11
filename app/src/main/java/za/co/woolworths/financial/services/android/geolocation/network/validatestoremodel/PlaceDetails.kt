package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class PlaceDetails (
    @SerializedName("address1")
    var address1: String? = null,

    @SerializedName("latitude")
    var latitude: Double? = null,

    @SerializedName("nickname")
    var nickname: String? = null,

    @SerializedName("placeId")
    var placeId: String? = null,

    @SerializedName("id")
    var id: String? = null,

    @SerializedName("longitude")
    var longitude: Double? = null,
): Serializable