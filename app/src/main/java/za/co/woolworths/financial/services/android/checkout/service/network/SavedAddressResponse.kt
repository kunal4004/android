package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.Response
import java.io.Serializable

class SavedAddressResponse : Serializable {
    @SerializedName("addresses")
    var addresses: ArrayList<Address>? = null

    @SerializedName("primaryContactNo")
    var primaryContactNo: String? = null

    @SerializedName("defaultAddressNickname")
    var defaultAddressNickname: String? = null

    @SerializedName("response")
    var response: Response? = null

    @SerializedName("httpCode")
    var httpCode: Int? = null
}