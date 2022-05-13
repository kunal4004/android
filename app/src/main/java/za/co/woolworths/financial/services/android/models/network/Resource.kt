package za.co.woolworths.financial.services.android.models.network

import androidx.annotation.StringRes

data class Resource<out T>(val status: Status, val data: T?, @StringRes val message: Int) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, -1)
        }

        fun <T> error(@StringRes msgInt: Int, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msgInt)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, -1)
        }
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}