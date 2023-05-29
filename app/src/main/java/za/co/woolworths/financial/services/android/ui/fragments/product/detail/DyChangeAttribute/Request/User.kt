package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User(
    @SerializedName("dyid")
    val dyid: String,
    @SerializedName("dyid_server")
    val dyid_server: String
): Serializable