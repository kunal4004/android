package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model

import com.google.gson.annotations.SerializedName


data class Normal (

	@SerializedName("id") val id : Int,
	@SerializedName("url") val url : String
)