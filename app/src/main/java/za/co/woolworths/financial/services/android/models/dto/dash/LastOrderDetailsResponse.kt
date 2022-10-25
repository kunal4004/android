package za.co.woolworths.financial.services.android.models.dto.dash

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.checkout.service.network.Response

@Parcelize
class LastOrderDetailsResponse(
    @SerializedName("orderCancellable")
    var orderCancellable: Boolean? = null,

    @SerializedName("total")
    var total: Long? = null,

    @SerializedName("orderId")
    var orderId: String? = null,

    @SerializedName("taxNoteNumbers")
    var taxNoteNumbers: String? = null,

    @SerializedName("state")
    var state: String? = null,

    @SerializedName("submittedDate")
    var submittedDate: String? = null,

    @SerializedName("completedDate")
    var completedDate: String? = null,

    @SerializedName("orderStatus")
    var orderStatus: String? = null,

    @SerializedName("driverTrackingURL")
    var driverTrackingURL: String? = null,

    @SerializedName("isChatEnabled")
    var isChatEnabled: Boolean = false,

    @SerializedName("isDriverTrackingEnabled")
    var isDriverTrackingEnabled: Boolean = false,

    @SerializedName("showDashOrder")
    var showDashOrder: Boolean = false,

    @SerializedName("response")
    var response: Response? = null,

    @SerializedName("httpCode")
    var httpCode: Int? = null
) : Parcelable