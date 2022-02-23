package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Reviews (
        @SerializedName("isVerifiedBuyer") var isVerifiedBuyer : Boolean,
        @SerializedName("isStaffMember") var isStaffMember : Boolean,
        @SerializedName("id") var id : Int,
        @SerializedName("productId") var productId : String,
        @SerializedName("syndicatedSource") var syndicatedSource : String,
        @SerializedName("rating") var rating : Float,
        @SerializedName("isRecommended") var isRecommended : Boolean,
        @SerializedName("submissionTime") var submissionTime : String,
        @SerializedName("reviewText") var reviewText : String,
        @SerializedName("title") var title : String,
        @SerializedName("userNickname") var userNickname : String,
        @SerializedName("totalPositiveFeedbackCount") var totalPositiveFeedbackCount : Int,
        @SerializedName("additionalFields") var additionalFields : List<AdditionalFields>,
        @SerializedName("secondaryRatings") var secondaryRatings : List<SecondaryRatings>,
        @SerializedName("contextDataValue") var contextDataValue : List<SkinProfile>,
        @SerializedName("tagDimensions") var tagDimensions : List<SkinProfile>,
        @SerializedName("photos") var photos : Photos,
        var isLiked: Boolean = false,
        var isReported: Boolean = false
) : Serializable