package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Suburb : Serializable {
    @SerializedName("regionId")
    var regionId: String? = null

    @SerializedName("fulfillmentStores")
    var fulfillmentStores: FulfillmentStores? = null

    @SerializedName("suburbDeliverable")
    var suburbDeliverable: Boolean? = null

    @SerializedName("postalCode")
    var postalCode: String? = null

    @SerializedName("regionName")
    var regionName: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("id")
    var id: String? = null
}