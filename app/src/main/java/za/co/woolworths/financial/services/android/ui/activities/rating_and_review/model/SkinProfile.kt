package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model

import com.google.gson.annotations.SerializedName

data class SkinProfile(
        @SerializedName("valueLabel") val valueLabel: String?,
        @SerializedName("label") val label: String?,
        @SerializedName("colorCode") val colorCode: String?
)