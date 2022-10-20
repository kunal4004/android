package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model
import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.SortOption

data class RatingReviewResponse (

		@SerializedName("limit") val limit : Int,
		@SerializedName("offset") val offset : Int,
		@SerializedName("totalResults") val totalResults : Int,
		@SerializedName("reviews") val reviews : List<Reviews>,
		@SerializedName("reviewStatistics") var reviewStatistics : ReviewStatistics,
		@SerializedName("sortOptions") val sortOptions : MutableList<SortOption>,
		@SerializedName("refinements") val refinements : MutableList<Refinements>,
		@SerializedName("reportReviewOptions") val reportReviewOptions : List<String>,
		@SerializedName("httpCode") val httpCode : Int
)