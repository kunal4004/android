package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model

import com.google.gson.annotations.SerializedName

data class Refinements (

	@SerializedName("displayName") val displayName : String,
	@SerializedName("navigationState") val navigationState : String,
	@SerializedName("selected") var selected : Boolean
)