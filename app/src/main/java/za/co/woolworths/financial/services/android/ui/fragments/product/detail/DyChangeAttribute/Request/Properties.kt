package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request

import com.google.gson.annotations.SerializedName

data class Properties(
    @SerializedName("attributeType")
    val attributeType: String,
    @SerializedName("attributeValue")
    val attributeValue: String,
    @SerializedName("dyType")
    val dyType: String
): java.io.Serializable