package za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.data

import retrofit2.HttpException
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.RemoteDataSource
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import javax.inject.Inject

interface ICardNotReceivedService {
    suspend fun queryServiceGetCardNotReceived(): ApiResult<Response>
}

class CardNotReceivedDataSource @Inject constructor() : ICardNotReceivedService {

    override suspend fun queryServiceGetCardNotReceived(): ApiResult<Response> {
        return try {
            val result = RemoteDataSource.service.queryServiceNotifyCardNotYetReceived(
                userAgent = "",
                userAgentVersion = "",
                sessionToken = OneAppService.getSessionToken())
            ApiResult.Success(result)
        } catch (h: HttpException) {
            ApiResult.Error(h)
        } catch (e: Exception) {
            ApiResult.Error(e)
        }
    }
}