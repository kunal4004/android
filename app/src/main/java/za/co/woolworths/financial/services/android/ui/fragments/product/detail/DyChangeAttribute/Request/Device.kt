package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request

import com.google.gson.annotations.SerializedName

data class Device(
    @SerializedName("ip")
    val ip: String
): java.io.Serializable