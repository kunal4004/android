package za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry

import okhttp3.internal.userAgent
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.models.network.GenericResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.RemoteDataSource
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import javax.inject.Inject


interface IContactUsDataSource {
    suspend fun makeEnquiry(emailUsRequest: EmailUsRequest): ApiResult<GenericResponse>
}

class ContactUsDataSource @Inject constructor() : IContactUsDataSource {

    override suspend fun makeEnquiry(emailUsRequest: EmailUsRequest): ApiResult<GenericResponse> {
        return try {
            val result = RemoteDataSource.service.makeEnquiry(
                userAgent="",
                userAgentVersion ="",
                sessionToken = OneAppService.getSessionToken(),
                deviceIdentityToken = OneAppService.getDeviceIdentityToken(),
                emailUsRequest = emailUsRequest
            )
            ApiResult.Success(result)
        } catch (h: HttpException) {
            ApiResult.Error(h)
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }

}