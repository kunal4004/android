package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews

import com.google.gson.annotations.SerializedName

data class Refinements (

	@SerializedName("displayName") val displayName : String,
	@SerializedName("navigationState") val navigationState : String,
	@SerializedName("selected") var selected : Boolean
)