package za.co.woolworths.financial.services.android.models.dto.pma

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.GetPaymentMethod
import za.co.woolworths.financial.services.android.models.dto.Response
import java.io.Serializable

data class PaymentMethodsResponse(
        @SerializedName("httpCode")
        @Expose
        val httpCode: Int,
        @SerializedName("paymentMethods")
        @Expose
        val paymentMethods: MutableList<GetPaymentMethod>?,
        @SerializedName("response")
        @Expose
        val response: Response
) : Serializable