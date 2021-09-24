package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ShippingDetailsResponse : Serializable {
    @SerializedName("success")
    @Expose
    var success: Boolean? = null

    @SerializedName("links")
    @Expose
    var links: List<Any>? = null

    @SerializedName("orderSummary")
    @Expose
    var orderSummary: OrderSummary? = null

    @SerializedName("paymentDetails")
    @Expose
    var paymentDetails: PaymentDetails? = null
}