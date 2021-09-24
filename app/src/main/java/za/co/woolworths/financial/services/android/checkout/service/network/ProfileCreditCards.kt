package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ProfileCreditCards : Serializable {
    @SerializedName("links")
    var links: List<Any>? = null

    @SerializedName("defaultCard")
    var defaultCard: DefaultCard? = null

    @SerializedName("failedCardDetails")
    var failedCardDetails: FailedCardDetails? = null
}