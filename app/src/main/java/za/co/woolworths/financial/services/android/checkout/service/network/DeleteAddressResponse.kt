package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.Response

/**
 * Created by Kunal Uttarwar on 05/07/21.
 */
class DeleteAddressResponse {

    @SerializedName("response")
    public val response: Response? = null

    @SerializedName("httpCode")
    public val httpCode: Int? = null

    @SerializedName("validationErrors")
    public val validationErrors: List<ValidationError>? = null
}