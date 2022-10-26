package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model

import com.google.gson.annotations.SerializedName

data class Photos (

		@SerializedName("thumbnails") val thumbnails : List<Thumbnails>,
		@SerializedName("normal") val normal : List<Normal>
)