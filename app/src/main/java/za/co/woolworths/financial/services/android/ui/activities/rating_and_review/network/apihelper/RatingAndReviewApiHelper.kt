package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper

import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.RetrofitApiProviderImpl
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.ReviewFeedback

class RatingAndReviewApiHelper() : RetrofitConfig(AppContextProviderImpl(), RetrofitApiProviderImpl()) {

    suspend fun getMoreReviews(prodId: String, offset: Int, sort: String?, refinements: String?) =
        mApiInterface.getMoreReviews(
            getSessionToken(),
            getDeviceIdentityToken(),
            prodId,
            10,
            offset,
            sort,
            refinements
        )

    suspend fun submitReviewFeedback(reviewFeedback: ReviewFeedback) =
        mApiInterface.submitFeedback(
            getSessionToken(),
            getDeviceIdentityToken(),
            reviewFeedback
        )
}