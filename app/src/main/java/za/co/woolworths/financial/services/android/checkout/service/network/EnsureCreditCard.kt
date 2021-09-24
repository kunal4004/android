package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import java.io.Serializable

class EnsureCreditCard : Serializable {
    @SerializedName("hasCreditCardPaymentGroup")
    var hasCreditCardPaymentGroup: Boolean? = null

    @SerializedName("links")
    var links: List<Any>? = null
}