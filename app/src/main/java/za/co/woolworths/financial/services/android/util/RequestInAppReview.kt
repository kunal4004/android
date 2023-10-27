package za.co.woolworths.financial.services.android.util

import android.app.Activity
import android.content.Intent
import com.google.android.play.core.review.ReviewManagerFactory
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.APP_GALLERY_COMMENT_SUBMITTED
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.APP_GALLERY_RATING_SUBMITTED
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HUAWEI_APP_COMMENTS_APP_ACTION_NAME
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HUAWEI_APP_COMMENTS_APP_PACKAGE_NAME
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.RESULT_OK_HUAWEI_REQUEST_CODE


fun launchInAppReviewFlow(activity: Activity?) {
    activity?.apply {

        if (Utils.isGooglePlayServicesAvailable()) {
            val manager = ReviewManagerFactory.create(this)
            manager?.requestReviewFlow()?.addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    val reviewInfo = request.result
                    manager.launchReviewFlow(this, reviewInfo).addOnCompleteListener { _ ->
                        Utils.setInAppReviewRequested()
                    }
                }
            }
        } else if (Utils.isHuaweiMobileServicesAvailable()) {
            val intent = Intent(HUAWEI_APP_COMMENTS_APP_ACTION_NAME)
            intent.setPackage(HUAWEI_APP_COMMENTS_APP_PACKAGE_NAME)
            this.startActivityForResult(intent, RESULT_OK_HUAWEI_REQUEST_CODE)
        }
    }
}

fun requestInAppReview(eventName: String, activity: Activity?) {
    if (AppConfigSingleton.inAppReview?.isEnabled == true && AppConfigSingleton.inAppReview?.triggerEvents?.contains(eventName) == true && !Utils.isInAppReviewRequested()) {
        launchInAppReviewFlow(activity)
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.inAppReviewRequest, activity)
    }
}

fun huaweiRatingsWindowResult(resultCode: Int) {
    when (resultCode) {
        APP_GALLERY_RATING_SUBMITTED,
        APP_GALLERY_COMMENT_SUBMITTED -> {
            Utils.setInAppReviewRequested()
        }
    }
}


