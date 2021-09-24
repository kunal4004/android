package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class FetchGiftCardNumber : Serializable {
    @SerializedName("creditCardNumber")
    var creditCardNumber: String? = null

    @SerializedName("staff")
    var staff: Boolean? = null

    @SerializedName("links")
    var links: List<Any>? = null

    @SerializedName("creditCardType")
    var creditCardType: String? = null
}