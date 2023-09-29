package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Repository

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Response.DyChangeAttributeResponse
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException
import javax.inject.Inject

class DyReportEventRepositoryImpl @Inject constructor(

) : DyReportEventRepository {

    override suspend fun getDyReportEventResponse(reportEventRequest: PrepareChangeAttributeRequestEvent?): Resource<DyChangeAttributeResponse> {
        return try {
            val response =
                reportEventRequest?.let { OneAppService().dynamicYieldChangeAttribute(it) }
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