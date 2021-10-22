package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews

import com.google.gson.annotations.SerializedName

data class RatingDistribution (

	@SerializedName("ratingValue") val ratingValue : Int,
	@SerializedName("count") val count : Int
)