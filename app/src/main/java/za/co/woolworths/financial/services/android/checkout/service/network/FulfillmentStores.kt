package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class FulfillmentStores : Serializable {
    @SerializedName("01")
    private var _01: String? = null

    @SerializedName("02")
    private var _02: String? = null

    @SerializedName("07")
    private var _07: String? = null
}