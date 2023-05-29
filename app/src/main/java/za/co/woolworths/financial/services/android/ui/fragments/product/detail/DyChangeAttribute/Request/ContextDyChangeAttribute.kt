package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ContextDyChangeAttribute(
    @SerializedName("device")
    val device: Device
): Serializable