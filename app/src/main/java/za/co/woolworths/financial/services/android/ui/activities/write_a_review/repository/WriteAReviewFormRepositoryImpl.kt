package za.co.woolworths.financial.services.android.ui.activities.write_a_review.repository

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.request.PrepareWriteAReviewFormRequestEvent
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.response.WriteAReviewFormResponse
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException
import javax.inject.Inject

class WriteAReviewFormRepositoryImpl @Inject constructor(

) : WriteAReviewFormRepository {

    override suspend fun getWriteAReviewFormResponse(productId: String?, writeAReviewFormRequest: PrepareWriteAReviewFormRequestEvent?): Resource<WriteAReviewFormResponse> {
        return try {
            val response =
                writeAReviewFormRequest?.let { OneAppService().writeAReviewForm(productId,it) }
            if (response?.isSuccessful == true) {
                response.body().let {
                    return when (it?.httpCode) {
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