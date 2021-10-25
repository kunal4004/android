package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews

data class RatingAndReviewData (
    val `data`: List<Data>,
    val httpCode: Int,
    val response: Response
)