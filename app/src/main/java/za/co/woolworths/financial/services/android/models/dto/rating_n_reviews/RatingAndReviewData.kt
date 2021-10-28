package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.Response

data class RatingAndReviewData (
    @SerializedName("data")val data: List<RatingReviewResopnse>,
    @SerializedName("httpCode")val httpCode: Int,
    @SerializedName("response")val response: Response
)