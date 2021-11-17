package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils

import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.Utils

class RatingAndReviewUtil {

    companion object {
        fun isRatingAndReviewConfigavailbel () = Utils.isFeatureEnabled(WoolworthsApplication.getInstance()?.ratingsAndReviews?.minimumSupportedAppBuildNumber  )
            ?: false
    }
}