package za.co.woolworths.financial.services.android.ui.fragments.account.main.core

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.*
import retrofit2.Response
import retrofit2.http.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.models.network.RetrofitException
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Result
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.core.mapNetworkCallToViewStateFlow
import za.co.woolworths.financial.services.android.ui.wfs.core.mapNetworkState
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.NetworkManager
import java.io.IOException
import java.net.ConnectException
import javax.inject.Inject

open class CoreDataSource @Inject constructor() : NetworkConfig(AppContextProviderImpl()) {

    /**
     * Sealed class type-restricts the result of IO calls to success and failure. The type
     * <T> represents the model class expected from the API call in case of a success
     * In case of success, the result will be wrapped around the OnSuccessResponse class
     * In case of error, the throwable causing the error will be wrapped around OnErrorResponse class
     */

    sealed class IOTaskResult<out DTO : Any> {
        data class Success<out DTO : Any>(val data: DTO) : IOTaskResult<DTO>()
        data class OnFailure<out DTO : Any>(val data: DTO) : IOTaskResult<DTO>()
        data class OnSessionTimeOut<out DTO : Any>(val data: DTO) : IOTaskResult<DTO>()

        data class OnFailed(val throwable: Throwable) : IOTaskResult<Nothing>()
        object NoConnectionState : IOTaskResult<Nothing>()
        object Empty : IOTaskResult<Nothing>()
    }

    /**
     * Utility function that works to perform a Retrofit API call and return either a success model
     * instance or an error message wrapped in an [Exception] class
     * @param messageInCaseOfError Custom error message to wrap around [IOTaskResult.OnFailed]
     * with a default value provided for flexibility
     * @param networkApiCall lambda representing a suspend function for the Retrofit API call
     * @return [IOTaskResult.Success] object of type [T], where [T] is the success object wrapped around
     * [IOTaskResult.Success] if network call is executed successfully, or [IOTaskResult.OnFailed]
     * object wrapping an [Exception] class stating the error
     */
    suspend fun isNetworkConnected(): Boolean {
        return NetworkManager.getInstance().isConnectedToNetwork(WoolworthsApplication.getInstance())
    }

    suspend inline fun <reified T : Any> executeSafeNetworkApiCall(
        crossinline networkApiCall: NetworkAPIInvoke<T>
    ): Flow<IOTaskResult<T>> {
        return flow {
            if (!isNetworkConnected()) {
                emit(IOTaskResult.NoConnectionState)
                return@flow
            }

            val response = networkApiCall.invoke()
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val responseBodyObj = Gson().toJsonTree(responseBody) as? JsonObject
                    when(responseBodyObj?.get("httpCode")?.asInt){
                        AppConstant.HTTP_OK -> emit(IOTaskResult.Success(responseBody))
                        AppConstant.HTTP_SESSION_TIMEOUT_440,
                        AppConstant.HTTP_SESSION_TIMEOUT_400-> emit(IOTaskResult.OnSessionTimeOut(responseBody))
                        else -> emit(IOTaskResult.OnFailure(responseBody))
                    }
                }else {
                   emit(IOTaskResult.Empty)
                }
            } else {
                try {
                    val errorBodyString = response.errorBody()?.string() ?: "Network error"
                    val parsedErrorBody = parseJson(errorBodyString) as T
                    when(response.code()){
                        AppConstant.HTTP_SESSION_TIMEOUT_440,
                        AppConstant.HTTP_SESSION_TIMEOUT_400-> emit(IOTaskResult.OnSessionTimeOut(parsedErrorBody))
                        else -> emit(IOTaskResult.OnFailure(parsedErrorBody))
                    }
                } catch (e: Exception) {
                    val error = IOException("API call failed with error - ${response.errorBody()?.string() ?: "Network error"}")
                    emit(IOTaskResult.OnFailed(error))
                }
            }
        }.catch { exception ->
            if (exception is ConnectException) {
                emit(IOTaskResult.NoConnectionState)
            } else {
                val error = IOException("Exception during network API call: ${exception.message}")
                emit(IOTaskResult.OnFailed(error))
            }
        }
    }



    private fun <T> error(
        apiError: za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError,
        data: T? = null
    ): Result<T> {
        return Result.error(apiError, data)
    }

    private fun getErrorMessage(responseCode: Int = 0): za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError {
        return when (responseCode) {
            0 -> za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError.SomethingWrong
            400 -> za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError.BadRequest
            404 -> za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError.NotFound
            440 -> za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError.SessionTimeOut
            else -> za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError.ServerErrors
        }
    }

    suspend inline fun <reified T : Any> withNetworkAPI(crossinline invokeApi: NetworkAPIInvoke<T>): Flow<ViewState<T>> =
        mapNetworkCallToViewStateFlow { executeSafeNetworkApiCall(invokeApi) }

    suspend inline fun <reified T : Any> network(crossinline invokeApi: NetworkAPIInvoke<T>): Flow<NetworkStatusUI<T>> =
        mapNetworkState { executeSafeNetworkApiCall(invokeApi) }

}

enum class ApiError(val value: String) {
    BadRequest("The request was unacceptable, often due to missing a required parameter"),
    NotFound("The requested resource doesn’t exist."),
    SessionTimeOut("Session timeout"),
    ServerErrors("Something went wrong on the API’s end"),
    SomethingWrong("Something went wrong ,Please check your internet connection");

    companion object {
        fun valueOf(value: String) =
            za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError.values()
                .find { it.value == value }
    }
}

/**
 * Readable naming convention for Network call lambda
 */
typealias NetworkAPIInvoke<T> = suspend () -> Response<T>

private fun <T> displayMaintenanceScreenIfNeeded(url: String, response: Response<T>): Boolean =
    RetrofitException(url, response.code(), null, response).show()

inline fun <reified T: Any> parseJson(body: String?): T {
    // handle OkResponse only
    return Gson().fromJson(body, T::class.java)
}

inline fun <reified T : Any> classOf(item: T) = T::class
