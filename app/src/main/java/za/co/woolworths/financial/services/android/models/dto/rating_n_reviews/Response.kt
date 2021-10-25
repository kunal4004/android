package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews

import com.google.gson.annotations.SerializedName

data class Response (

	@SerializedName("code") val code : Int,
	@SerializedName("desc") val desc : String
)