package za.co.woolworths.financial.services.android.ui.fragments.account.main.util

import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.ApiError

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