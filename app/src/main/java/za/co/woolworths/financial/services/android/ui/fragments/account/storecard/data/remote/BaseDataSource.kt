package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.remote

import retrofit2.Response
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.utils.Result

abstract class BaseDataSource {

    protected suspend fun <T> getResult(call: suspend () -> Response<T>): Result<T> {
        try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) return Result.success(body)
            }
            return error(" ${getErrorMessage(response.code())}")
        } catch (e: Exception) {
            return error(getErrorMessage())
        }

    }

    private fun <T> error(message: String): Result<T> {
        return Result.error(message)
    }

    private fun getErrorMessage(responseCode:Int = 0):String{
        return when (responseCode) {
            400 -> ApiError.BadRequest.value
            404 -> ApiError.NotFound.value
            500, 502, 503, 504 -> ApiError.ServerErrors.value
            else -> ApiError.SomethingWrong.value
        }
    }

}

enum class ApiError(val value: String) {
    BadRequest("The request was unacceptable, often due to missing a required parameter"),
    NotFound("The requested resource doesn’t exist."),
    ServerErrors("Something went wrong on the API’s end"),
    SomethingWrong("Something went wrong ,Please check your internet connection");

    companion object {
        fun valueOf(value: String) = ApiError.values().find { it.value == value }
    }
}