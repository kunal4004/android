package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class DiscountDetails : Serializable {
    @SerializedName("totalOrderDiscount")
    var totalOrderDiscount: Long? = null
}