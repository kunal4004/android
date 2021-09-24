package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.checkout.service.network.EnsureCreditCard
import za.co.woolworths.financial.services.android.checkout.service.network.RedeemGiftCardCheck
import za.co.woolworths.financial.services.android.checkout.service.network.FetchGiftCardNumber
import za.co.woolworths.financial.services.android.checkout.service.network.ProfileCreditCards
import java.io.Serializable

class PaymentDetails : Serializable {
    @SerializedName("ensureCreditCard")
    var ensureCreditCard: EnsureCreditCard? = null

    @SerializedName("description")
    var description: String? = null

    @SerializedName("redeemGiftCardCheck")
    var redeemGiftCardCheck: RedeemGiftCardCheck? = null

    @SerializedName("links")
    var links: List<Any>? = null

    @SerializedName("publicKey")
    var publicKey: String? = null

    @SerializedName("fetchGiftCardNumber")
    var fetchGiftCardNumber: FetchGiftCardNumber? = null

    @SerializedName("profileCreditCards")
    var profileCreditCards: ProfileCreditCards? = null

    @SerializedName("fingerPrint")
    var fingerPrint: String? = null
}