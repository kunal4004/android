package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Address : Serializable {
    @SerializedName("lastName")
    var lastName: String? = null

    @SerializedName("country")
    var country: String? = null

    @SerializedName("suburbId")
    var suburbId: String? = null

    @SerializedName("address2")
    var address2: String? = null

    @SerializedName("city")
    var city: String? = null

    @SerializedName("address1")
    var address1: String? = null

    @SerializedName("postalCode")
    var postalCode: String? = null

    @SerializedName("primaryContactNo")
    var primaryContactNo: String? = null

    @SerializedName("title")
    var title: String? = null

    @SerializedName("ownerId")
    var ownerId: String? = null

    @SerializedName("secondaryContactNo")
    var secondaryContactNo: String? = null

    @SerializedName("firstName")
    var firstName: String? = null

    @SerializedName("phoneNumber")
    var phoneNumber: String? = null

    @SerializedName("storeAddress")
    var storeAddress: Boolean? = null

    @SerializedName("nickname")
    var nickname: String? = null

    @SerializedName("recipientName")
    var recipientName: String? = null

    @SerializedName("suburb")
    var suburb: String? = null

    @SerializedName("id")
    var id: String? = null

    @SerializedName("state")
    var state: String? = null

    @SerializedName("region")
    var region: String? = null

    @SerializedName("displayName")
    var displayName: String? = null

    @SerializedName("addressType")
    var addressType: String? = null

    @SerializedName("latitude")
    var latitude: Double? = null

    @SerializedName("longitude")
    var longitude: Double? = null
}