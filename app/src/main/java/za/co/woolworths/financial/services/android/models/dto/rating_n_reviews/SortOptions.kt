package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews

import com.google.gson.annotations.SerializedName

data class SortOptions (

	@SerializedName("label") val label : String,
	@SerializedName("sortOption") val sortOption : String,
	@SerializedName("selected") val selected : Boolean
)