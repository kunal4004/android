package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews

import com.google.gson.annotations.SerializedName

data class Photos (

	@SerializedName("thumbnails") val thumbnails : List<Thumbnails>,
	@SerializedName("normal") val normal : List<Normal>
)