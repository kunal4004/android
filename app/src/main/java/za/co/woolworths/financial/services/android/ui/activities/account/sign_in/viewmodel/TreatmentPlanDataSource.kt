package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel

import retrofit2.HttpException
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.RemoteDataSource
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import javax.inject.Inject

interface ITreatmentPlanDataSource {
    suspend fun fetchTreatmentPlanEligibility(productGroupCode: String): ApiResult<EligibilityPlanResponse>
}

class TreatmentPlanDataSource @Inject constructor() : ITreatmentPlanDataSource {

    override suspend fun fetchTreatmentPlanEligibility(productGroupCode: String): ApiResult<EligibilityPlanResponse> {
        return try {
            val result = RemoteDataSource.service.fetchCollectionCheckEligibility(
                userAgent = "",
                userAgentVersion = "",
                sessionToken = OneAppService().getSessionToken(),
                productGroupCode = productGroupCode,
                deviceIdentityToken = OneAppService().getDeviceIdentityToken())
            ApiResult.Success(result)
        } catch (h: HttpException) {
            ApiResult.Error(h)
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }

}