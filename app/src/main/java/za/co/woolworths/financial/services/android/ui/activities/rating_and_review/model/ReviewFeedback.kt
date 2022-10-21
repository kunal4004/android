package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model

import com.google.gson.annotations.SerializedName

data class ReviewFeedback(

    @SerializedName("contentId"    ) var ContentId    : String? = null,
    @SerializedName("userId"       ) var UserId       : String? = null,
    @SerializedName("contentType"  ) var ContentType  : String? = null,
    @SerializedName("feedbackType" ) var FeedbackType : String? = null,
    @SerializedName("vote"         ) var Vote : String? = null,
    @SerializedName("reasonText"   ) var ReasonText   : String? = null

)