package za.co.woolworths.financial.services.android.util

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication


fun launchInAppReviewFlow(activity: Activity?) {
    activity?.apply {
        val manager = ReviewManagerFactory.create(this)
        manager?.requestReviewFlow()?.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                val reviewInfo = request.result
                manager.launchReviewFlow(this, reviewInfo).addOnCompleteListener { _ ->
                    Utils.setInAppReviewRequested()
                }
            }
        }
    }

}

fun requestInAppReview(eventName: String, activity: Activity?) {
    if (WoolworthsApplication.getInAppReview().isEnabled && WoolworthsApplication.getInAppReview()?.triggerEvents?.contains(eventName) == true && !Utils.isInAppReviewRequested()) {
        launchInAppReviewFlow(activity)
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.inAppReviewRequest, activity)
    }
}

