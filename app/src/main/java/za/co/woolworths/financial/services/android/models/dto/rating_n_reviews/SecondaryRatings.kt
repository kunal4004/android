package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews

import com.google.gson.annotations.SerializedName

data class SecondaryRatings (

	@SerializedName("value") val value : Int,
	@SerializedName("id") val id : String,
	@SerializedName("maxLabel") val maxLabel : String,
	@SerializedName("minLabel") val minLabel : String,
	@SerializedName("displayType") val displayType : String,
	@SerializedName("valueRange") val valueRange : Int,
	@SerializedName("label") val label : String,
	@SerializedName("valueLabel") val valueLabel : String
)