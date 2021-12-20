package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model

import com.google.gson.annotations.SerializedName

data class SortOptions (

	@SerializedName("label") val label : String,
	@SerializedName("sortOption") val sortOption : String,
	@SerializedName("selected") val selected : Boolean
)