package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Kunal Uttarwar on 14/10/22.
 */

data class NetworkErrorResponse(
    @SerializedName("httpCode")
    var httpCode: Int,
    @SerializedName("redirectURL")
    val redirectURL: String
) : Serializable