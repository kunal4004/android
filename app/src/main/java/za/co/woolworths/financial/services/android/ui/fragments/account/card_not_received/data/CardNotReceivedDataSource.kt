package za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.data

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.pma.ErrorResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.RemoteDataSource
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import javax.inject.Inject

interface ICardNotReceivedService {
    suspend fun queryServiceNotifyCardNotYetReceived(): ApiResult<Response>
}

data class CardNotReceived(
    val emailBody: String? = null,
    val enquiryType: String? = "cardNotReceived",
    val preferredEmail: String?,
    val preferredName: String?,
    val productGroupCode: String? = null
)

class CardNotReceivedDataSource @Inject constructor(@ApplicationContext val context: Context) :
    ICardNotReceivedService {

    override suspend fun queryServiceNotifyCardNotYetReceived(): ApiResult<Response> {
        val jwtDecodedModel = SessionUtilities.getInstance().jwt
        val preferredEmail = jwtDecodedModel.email?.get(0)
        val emailBody = ""
        val preferredName = jwtDecodedModel.name?.get(0)

        return try {
            val result = RemoteDataSource.service.queryServiceNotifyCardNotYetReceived(
                userAgent = "",
                userAgentVersion = "",
                sessionToken = OneAppService.getSessionToken(),
                emailId = "cardNotReceived",
                body = CardNotReceived(
                    preferredEmail = preferredEmail,
                    emailBody = emailBody,
                    preferredName = preferredName,
                    productGroupCode = AccountsProductGroupCode.STORE_CARD.groupCode.uppercase()
                )
            )
            ApiResult.Success(result)
        } catch (httpException: HttpException) {
            try {
                val errorBody = httpException.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                ApiResult.Failure(errorResponse.response)
            } catch (e: Exception) {
                ApiResult.Error(httpException)
            }
        } catch (exception: Exception) {
            ApiResult.Error(exception)
        }
    }
}