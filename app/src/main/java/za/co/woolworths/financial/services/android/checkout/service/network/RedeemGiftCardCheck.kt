package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class RedeemGiftCardCheck : Serializable {
    @SerializedName("remainingAmount")
    var remainingAmount: Double? = null

    @SerializedName("giftCardPaymentValue")
    var giftCardPaymentValue: Long? = null

    @SerializedName("links")
    var links: List<Any>? = null

    @SerializedName("explodeCard")
    var explodeCard: Boolean? = null
}