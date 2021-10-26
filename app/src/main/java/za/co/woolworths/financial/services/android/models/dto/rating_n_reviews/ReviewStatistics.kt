package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews

import com.google.gson.annotations.SerializedName

data class ReviewStatistics (

    @SerializedName("averageRating") val averageRating : Float,
    @SerializedName("helpfulVoteCount") val helpfulVoteCount : Int,
    @SerializedName("recommendedCount") val recommendedCount : Int,
    @SerializedName("ratingDistribution") val ratingDistribution : List<RatingDistribution>,
    @SerializedName("reviewCount") val reviewCount : Int,
    @SerializedName("overallRatingRange") val overallRatingRange : Int,
    @SerializedName("recommendedPercentage") val recommendedPercentage : String
)