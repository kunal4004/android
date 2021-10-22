package za.co.woolworths.financial.services.android.models.dto.rating_n_reviews

import com.google.gson.annotations.SerializedName

data class Reviews (

	@SerializedName("isVerifiedBuyer") val isVerifiedBuyer : Boolean,
	@SerializedName("isStaffMember") val isStaffMember : Boolean,
	@SerializedName("id") val id : Int,
	@SerializedName("productId") val productId : Int,
	@SerializedName("syndicatedSource") val syndicatedSource : String,
	@SerializedName("rating") val rating : Int,
	@SerializedName("isRecommended") val isRecommended : Boolean,
	@SerializedName("submissionTime") val submissionTime : String,
	@SerializedName("reviewText") val reviewText : String,
	@SerializedName("title") val title : String,
	@SerializedName("userNickname") val userNickname : String,
	@SerializedName("totalPositiveFeedbackCount") val totalPositiveFeedbackCount : Int,
	@SerializedName("additionalFields") val additionalFields : List<AdditionalFields>,
	@SerializedName("secondaryRatings") val secondaryRatings : List<SecondaryRatings>,
	@SerializedName("contextDataValue") val contextDataValue : List<ContextDataValue>,
	@SerializedName("tagDimensions") val tagDimensions : List<TagDimensions>,
	@SerializedName("photos") val photos : Photos
)