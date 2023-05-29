package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Session(
    @SerializedName("dy")
    val dy: String
): Serializable