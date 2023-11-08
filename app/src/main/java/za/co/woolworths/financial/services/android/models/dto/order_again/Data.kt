package za.co.woolworths.financial.services.android.models.dto.order_again

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {
    @SerializedName("responses")
    @Expose
    var responses: List<Response>? = null
}