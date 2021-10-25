package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews

import com.google.gson.annotations.SerializedName


data class AdditionalFields (

	@SerializedName("valueLabel") val valueLabel : String,
	@SerializedName("label") val label : String
)