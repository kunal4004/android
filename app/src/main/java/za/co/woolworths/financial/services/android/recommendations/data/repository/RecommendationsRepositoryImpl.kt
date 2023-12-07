package za.co.woolworths.financial.services.android.recommendations.data.repository

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException
import javax.inject.Inject

class RecommendationsRepositoryImpl @Inject constructor(

) : RecommendationsRepository {

    override suspend fun getRecommendationResponse(recommendationRequest: RecommendationRequest?, requestData: Boolean, fulfillmentStoreId: String?): Resource<RecommendationResponse> {

        return try {
            val response = recommendationRequest?.let { OneAppService().recommendation(it, requestData, fulfillmentStoreId) }
            if (response?.isSuccessful == true) {
                response.body()?.let {
                    return when (it.httpCode) {
                        AppConstant.HTTP_OK ->
                            Resource.success(it)
                        else ->
                            Resource.error(R.string.error_unknown, it)
                    }
                } ?: Resource.error(R.string.error_unknown, null)
            } else {
                Resource.error(R.string.error_unknown, null)
            }
        } catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }
    }

}

