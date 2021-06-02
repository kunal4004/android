package za.co.woolworths.financial.services.android.util

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties

fun askForReview(activity: Activity?) {
    activity?.apply {
        val manager = ReviewManagerFactory.create(this)
        manager?.requestReviewFlow()?.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                val reviewInfo = request.result
                manager.launchReviewFlow(this, reviewInfo).addOnCompleteListener { _ ->
                    Utils.setInAppReviewRequested()
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.inAppReviewRequest, this)
                }
            }
        }
    }

}

