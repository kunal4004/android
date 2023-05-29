package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EventDyChangeAttribute(
    @SerializedName("name")
    val name: String,
    @SerializedName("properties")
    val properties: Properties
): Serializable