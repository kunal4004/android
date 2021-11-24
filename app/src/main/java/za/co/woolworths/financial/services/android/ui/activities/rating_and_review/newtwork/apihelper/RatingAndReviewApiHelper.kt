package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.newtwork.apihelper

import za.co.woolworths.financial.services.android.models.network.RetrofitConfig

class RatingAndReviewApiHelper() : RetrofitConfig() {

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

}