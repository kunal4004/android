package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.Response

data class RatingAndReviewData (
        @SerializedName("data")val data: List<RatingReviewResponse>,
        @SerializedName("httpCode")val httpCode: Int,
        @SerializedName("response")val response: Response
)