package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class DeliveryStatus(
    @SerializedName("01")
    private var _01: Boolean? = null,

    @SerializedName("02")
    private var _02: Boolean? = null,

    @SerializedName("04")
    private var _04: Boolean? = null,

    @SerializedName("07")
    private var _07: Boolean? = null,
) : Serializable