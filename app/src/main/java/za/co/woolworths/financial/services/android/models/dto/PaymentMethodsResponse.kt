package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PaymentMethodsResponse(
        @SerializedName("httpCode")
        @Expose
        val httpCode: Int,
        @SerializedName("paymentMethods")
        @Expose
        val paymentMethods: MutableList<GetPaymentMethod>,
        @SerializedName("response")
        @Expose
        val response: Response
) : Serializable