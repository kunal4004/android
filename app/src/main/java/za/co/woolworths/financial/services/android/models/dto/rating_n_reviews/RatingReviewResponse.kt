package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews
import com.google.gson.annotations.SerializedName

data class RatingReviewResponse (

	@SerializedName("limit") val limit : Int,
	@SerializedName("offset") val offset : Int,
	@SerializedName("totalResults") val totalResults : Int,
	@SerializedName("reviews") val reviews : List<Reviews>,
	@SerializedName("reviewStatistics") val reviewStatistics : ReviewStatistics,
	@SerializedName("sortOptions") val sortOptions : List<SortOptions>,
	@SerializedName("refinements") val refinements : List<Refinements>,
	@SerializedName("reportReviewOptions") val reportReviewOptions : List<String>,
	/*@SerializedName("response") val response : Response,*/
	@SerializedName("httpCode") val httpCode : Int

)