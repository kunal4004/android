package za.co.woolworths.financial.services.android.ui.fragments.account.main.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen
import retrofit2.Response
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Result
import java.io.IOException
import javax.inject.Inject

open class CoreDataSource @Inject constructor() : NetworkConfig() {

    /**
     * Sealed class type-restricts the result of IO calls to success and failure. The type
     * <T> represents the model class expected from the API call in case of a success
     * In case of success, the result will be wrapped around the OnSuccessResponse class
     * In case of error, the throwable causing the error will be wrapped around OnErrorResponse class
     */
    sealed class IOTaskResult<out DTO : Any> {
        data class OnSuccess<out DTO : Any>(val data: DTO) : IOTaskResult<DTO>()
        data class OnFailed(val throwable: Throwable) : IOTaskResult<Nothing>()
    }

    /**
     * Utility function that works to perform a Retrofit API call and return either a success model
     * instance or an error message wrapped in an [Exception] class
     * @param messageInCaseOfError Custom error message to wrap around [IOTaskResult.OnFailed]
     * with a default value provided for flexibility
     * @param networkApiCall lambda representing a suspend function for the Retrofit API call
     * @return [IOTaskResult.OnSuccess] object of type [T], where [T] is the success object wrapped around
     * [IOTaskResult.OnSuccess] if network call is executed successfully, or [IOTaskResult.OnFailed]
     * object wrapping an [Exception] class stating the error
     */

    suspend fun <T : Any> performSafeNetworkApiCall(
        messageInCaseOfError: String = "Network error",
        allowRetries: Boolean = true,
        numberOfRetries: Int = 2,
        networkApiCall: NetworkAPIInvoke<T>
    ): Flow<IOTaskResult<T>> {
        var delayDuration = 1000L
        val delayFactor = 2
        return flow {
            val response = networkApiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(IOTaskResult.OnSuccess(it))
                }
                    ?: emit(IOTaskResult.OnFailed(IOException("API call successful but empty response body")))
                return@flow
            }
            emit(
                IOTaskResult.OnFailed(
                    IOException(
                        "API call failed with error - ${
                            response.errorBody()
                                ?.string() ?: messageInCaseOfError
                        }"
                    )
                )
            )
            return@flow
        }.catch { e ->
            emit(IOTaskResult.OnFailed(IOException("Exception during network API call: ${e.message}")))
            return@catch
        }.retryWhen { cause, attempt ->
            if (!allowRetries || attempt > numberOfRetries || cause !is IOException) return@retryWhen false
            delay(delayDuration)
            delayDuration *= delayFactor
            return@retryWhen true
        }.flowOn(Dispatchers.IO)
    }

    private fun <T> error(apiError: za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError, data: T? = null): Result<T> { return Result.error(apiError, data) }

    private fun getErrorMessage(responseCode: Int = 0): za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError {
        return when (responseCode) {
            0 -> za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError.SomethingWrong
            400 -> za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError.BadRequest
            404 -> za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError.NotFound
            440 -> za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError.SessionTimeOut
            else -> za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError.ServerErrors
        }
    }
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
