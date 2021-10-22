package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews

import za.co.woolworths.financial.services.android.models.dto.rating_review_anand.Data
import za.co.woolworths.financial.services.android.models.dto.rating_review_anand.Response

data class RatingAndReviewData (
    val `data`: List<Data>,
    val httpCode: Int,
    val response: Response
)