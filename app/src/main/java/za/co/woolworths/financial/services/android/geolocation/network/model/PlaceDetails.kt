package za.co.woolworths.financial.services.android.geolocation.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PlaceDetails {
    @SerializedName("address1")
    @Expose
    var address1: String? = null

    @SerializedName("latitude")
    @Expose
    var latitude: Double? = null

    @SerializedName("nickname")
    @Expose
    var nickname: String? = null

    @SerializedName("placeId")
    @Expose
    var placeId: String? = null

    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("longitude")
    @Expose
    var longitude: Double? = null
}