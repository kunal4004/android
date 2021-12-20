package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model

import com.google.gson.annotations.SerializedName

data class RatingDistribution (

	@SerializedName("ratingValue") val ratingValue : Int,
	@SerializedName("count") val count : Int
)