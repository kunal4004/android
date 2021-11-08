package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.service

import android.content.Context
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.util.NetworkManager

class ReviewApiHelper(val offset: Int) : RetrofitConfig() {
    suspend fun getMoreReviews(offset: Int) = mApiInterface.getMoreReviews(
            sessionToken = getSessionToken(),
            deviceIdentityToken = getDeviceIdentityToken(),
            productId =  "676767677",
            limit = 10,
            offset = offset
    )

    fun isConnectedToInternet(context: Context) = NetworkManager
            .getInstance().isConnectedToNetwork(context)

}
