package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

/**
 * Created by Kunal Uttarwar on 05/07/21.
 */
class DeleteAddressResponse {

    @SerializedName("response")
    public val response: Response? = null

    @SerializedName("httpCode")
    public val httpCode: Int? = null
}