package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import za.co.woolworths.financial.services.android.checkout.service.network.FailedAttemptsSC
import java.io.Serializable

class FailedCardDetails : Serializable {
    @SerializedName("isProfileBlockedForGC")
    var isProfileBlockedForGC: Boolean? = null

    @SerializedName("failedAttemptsSC")
    var failedAttemptsSC: FailedAttemptsSC? = null

    @SerializedName("isProfileBlockedForSC")
    var isProfileBlockedForSC: Boolean? = null

    @SerializedName("links")
    var links: List<Any>? = null
}