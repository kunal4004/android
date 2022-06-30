package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Address : Serializable {
    @SerializedName("suburbId")
    var suburbId: String? = null

    @SerializedName("address2")
    var address2: String? = null

    @SerializedName("address1")
    var address1: String? = null

    @SerializedName("postalCode")
    var postalCode: String? = null

    @SerializedName("primaryContactNo")
    var primaryContactNo: String? = null

    @SerializedName("nickname")
    var nickname: String? = null

    @SerializedName("recipientName")
    var recipientName: String? = null

    @SerializedName("suburb")
    var suburb: String? = null

    @SerializedName("id")
    var id: String? = null

    @SerializedName("region")
    var region: String? = null

    @SerializedName("placesId")
    var placesId: String? = null

    @SerializedName("addressType")
    var addressType: String? = null

    @SerializedName("latitude")
    var latitude: Double? = null

    @SerializedName("longitude")
    var longitude: Double? = null

    @SerializedName("verified")
    var verified: Boolean = false

    @SerializedName("newAddress")
    var newAddress: Boolean = false

    @SerializedName("updateAddressInfo")
    var updateAddressInfo: Boolean = false

    @SerializedName("storeAddress")
    var storeAddress: Boolean = false

    @SerializedName("storeId")
    var storeId: String? = null

    @SerializedName("city")
    var city: String? = null

}