package za.co.woolworths.financial.services.android.models.dto.order_again

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Response {
    @SerializedName("requestId")
    @Expose
    var requestId: String? = null

    @SerializedName("actions")
    @Expose
    var actions: List<Action>? = null

    @SerializedName("code")
    @Expose
    var code: String? = null

    @SerializedName("desc")
    @Expose
    var desc: String? = null
}