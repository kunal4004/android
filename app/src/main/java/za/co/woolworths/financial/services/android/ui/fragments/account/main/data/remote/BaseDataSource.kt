package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote

import retrofit2.Response
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Result

abstract class BaseDataSource : NetworkConfig(AppContextProviderImpl()) {

    protected suspend fun <T> getResult(call: suspend () -> Response<T>): Result<T> {
        try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) return Result.success(body)
            }
            return error(getErrorMessage(response.code()), response.body())
        } catch (e: Exception) {
            return error(getErrorMessage())
        }

    }

    private fun <T> error(apiError: ApiError, data: T? = null): Result<T> {
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
        fun valueOf(value: String) = values().find { it.value == value }
    }
}

