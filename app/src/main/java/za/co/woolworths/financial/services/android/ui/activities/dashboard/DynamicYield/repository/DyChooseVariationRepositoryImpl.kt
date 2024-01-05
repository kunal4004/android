package za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.repository

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.dynamicyield.data.response.getResponse.DynamicYieldChooseVariationResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.HomePageRequestEvent
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException
import javax.inject.Inject

class DyChooseVariationRepositoryImpl @Inject constructor(

) : DyChooseVariationRepository {
    override suspend fun getDyChooseVariationResponse(chooseVariationRequestEvent: HomePageRequestEvent?): Resource<DynamicYieldChooseVariationResponse> {
        return try {
            val response = chooseVariationRequestEvent?.let { OneAppService().dynamicYieldHomePage(it) }
            if (response?.isSuccessful == true) {
                response.body().let {
                    return when (it?.httpCode) {
                        AppConstant.HTTP_OK ->
                            Resource.success(it)
                        else ->
                            Resource.error(R.string.error_unknown, it)
                    }
                }?: Resource.error(R.string.error_unknown, null)
            } else {
                Resource.error(R.string.error_unknown, null)
            }
        }catch (e: IOException) {
            FirebaseManager.logException(e)
            Resource.error(R.string.error_internet_connection, null)
        }
    }


}