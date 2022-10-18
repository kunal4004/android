package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.Response
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.models.network.RetrofitException
import za.co.woolworths.financial.services.android.util.NetworkManager
import java.io.IOException
import java.net.ConnectException
import javax.inject.Inject

//TODO:: delete class and edit imports once Store card enhancements merged to dev
class Result<out T>(val status: Status, val data: T?, val apiError: ApiError?) {

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object {
        fun <T> success(data: T): Result<T> {
            return Result(Status.SUCCESS, data, null)
        }

        fun <T> error(apiError: ApiError, data: T? = null): Result<T> {
            return Result(Status.ERROR, data, apiError)
        }

        fun <T> loading(data: T? = null): Result<T> {
            return Result(Status.LOADING, data, null)
        }
    }
}
suspend fun <T : Any> getViewStateFlowForNetworkCall(ioOperation: suspend () -> Flow<IOTaskResult<T>>): Flow<ViewState<T>> {
    return flow {
        emit(ViewState.Loading(true))
        ioOperation().map { task ->
            when (task) {
                is IOTaskResult.OnSuccess -> ViewState.RenderSuccess(task.data)
                is IOTaskResult.OnFailure -> ViewState.RenderErrorFromResponse(task.data)
                is IOTaskResult.OnFailed -> ViewState.RenderFailure(task.throwable)
                is IOTaskResult.Empty -> ViewState.RenderEmpty
                is IOTaskResult.NoConnectionState -> ViewState.RenderNoConnection
            }
        }.collect { viewState ->
            when (viewState) {
                is ViewState.RenderNoConnection -> emit(viewState)
                else -> {
                    emit(viewState)
                    emit(ViewState.Loading(false))
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
/**
 * Lets the UI act on a controlled bound of states that can be defined here
 */
sealed class ViewState<out T> where T : Any? {

    /**
     * Represents UI state where the UI should be showing a loading UX to the user
     * @param isLoading will be true when the loading UX needs to display, false when not
     */
    data class Loading(val isLoading: Boolean) : ViewState<Nothing>()

    /**
     * Represents the UI state where the operation requested by the UI has been completed successfully
     * and the output of type [T] as asked by the UI has been provided to it
     * @param output result object of [T] type representing the fruit of the successful operation
     */
    data class RenderSuccess<T>(val output: T) : ViewState<T>()

    data class RenderErrorFromResponse<T>(val output: T) : ViewState<T>()

    /**
     * Represents the UI state where the operation requested by the UI has failed to complete
     * either due to a IO issue or a service exception and the same is conveyed back to the UI
     * to be shown the user
     * @param throwable [Throwable] instance containing the root cause of the failure in a [String]
     */
    data class RenderFailure(val throwable: Throwable) : ViewState<Nothing>()

    object RenderEmpty : ViewState<Nothing>()

    object RenderNoConnection : ViewState<Nothing>()

}

infix fun <T> ViewState<T>.renderSuccess(onSuccess: ViewState.RenderSuccess<T>.() -> Unit): ViewState<T> {
    return when (this) {
        is ViewState.RenderSuccess -> {
            onSuccess(this)
            this
        }
        else -> {
            this
        }
    }
}


infix fun <T> ViewState<T>.renderHttpFailureFromServer(onFailure: ViewState.RenderErrorFromResponse<T>.() -> Unit): ViewState<T> {
    return when (this) {
        is ViewState.RenderErrorFromResponse -> {
            onFailure(this)
            this
        }
        else -> {
            this
        }
    }
}

infix fun <T> ViewState<T>.renderFailure(onError: ViewState.RenderFailure.() -> Unit): ViewState<T> {
    return when (this) {
        is ViewState.RenderFailure -> {
            onError(this)
            this
        }
        else -> {
            this
        }
    }
}


infix fun <T> ViewState<T>.renderEmpty(onEmpty: ViewState.RenderEmpty.() -> Unit): ViewState<T> {
    return when (this) {
        is ViewState.RenderEmpty -> {
            onEmpty(this)
            this
        }
        else -> {
            this
        }
    }
}

infix fun <T> ViewState<T>.renderNoConnection(onEmpty: ViewState.RenderNoConnection.() -> Unit): ViewState<T> {
    return when (this) {
        is ViewState.RenderNoConnection -> {
            onEmpty(this)
            this
        }
        else -> {
            this
        }
    }
}

infix fun <T> ViewState<T>.renderLoading(onLoading: ViewState.Loading.() -> Unit): ViewState<T> {
    return when (this) {
        is ViewState.Loading -> {
            onLoading(this)
            this
        }
        else -> {
            this
        }
    }
}
sealed class IOTaskResult<out DTO : Any> {
    data class OnSuccess<out DTO : Any>(val data: DTO) : IOTaskResult<DTO>()
    data class OnFailure<out DTO : Any>(val data: DTO) : IOTaskResult<DTO>()
    data class OnFailed(val throwable: Throwable) : IOTaskResult<Nothing>()
    object NoConnectionState : IOTaskResult<Nothing>()
    object Empty : IOTaskResult<Nothing>()
}
open class CoreDataSource @Inject constructor() : NetworkConfig() {

    /**
     * Sealed class type-restricts the result of IO calls to success and failure. The type
     * <T> represents the model class expected from the API call in case of a success
     * In case of success, the result will be wrapped around the OnSuccessResponse class
     * In case of error, the throwable causing the error will be wrapped around OnErrorResponse class
     */



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

    suspend inline fun <reified T : Any> performSafeNetworkApiCall(
        crossinline networkApiCall: NetworkAPIInvoke<T>
    ): Flow<IOTaskResult<T>> {
        return flow {
            // Emit no connection found
            if (!NetworkManager.getInstance().isConnectedToNetwork(WoolworthsApplication.getInstance())) {
                emit(IOTaskResult.NoConnectionState)
                return@flow
            }

            // Execute api
            with(networkApiCall()) {
                when (isSuccessful) {
                    true -> {
                        body()?.let {
                            emit(IOTaskResult.OnSuccess(it))
                        } ?: emit(IOTaskResult.Empty)
                    }
                    false -> {
                        emit(
                            try {
                                IOTaskResult.OnFailure(parseJson(errorBody()?.string()) as T)
                            } catch (e: Exception) {
                                IOTaskResult.OnFailed(
                                    IOException(
                                        "API call failed with error - ${
                                            errorBody()?.string() ?: "Network error"
                                        }"
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }.catch { exception ->
            if (exception is ConnectException){
                emit(IOTaskResult.NoConnectionState)
            }else {
                emit(IOTaskResult.OnFailed(IOException("Exception during network API call: ${exception.message}")))
            }
            return@catch
        }
    }


    private fun <T> error(
        apiError: ApiError,
        data: T? = null
    ): Result<T> {
        return Result.error(apiError, data)
    }

    private fun getErrorMessage(responseCode: Int = 0): ApiError {
        return when (responseCode) {
            0 -> ApiError.SomethingWrong
            400 -> ApiError.BadRequest
            404 -> ApiError.NotFound
            440 -> ApiError.SessionTimeOut
            else -> ApiError.ServerErrors
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
            ApiError.values()
                .find { it.value == value }
    }
}

/**
 * Readable naming convention for Network call lambda
 */
typealias NetworkAPIInvoke<T> = suspend () -> Response<T>

private fun <T> displayMaintenanceScreenIfNeeded(url: String, response: Response<T>): Boolean =
    RetrofitException(url, response.code()).show()

inline fun <reified T: Any> parseJson(body: String?): T {
    // handle OkResponse only
    return Gson().fromJson(body, T::class.java)
}

