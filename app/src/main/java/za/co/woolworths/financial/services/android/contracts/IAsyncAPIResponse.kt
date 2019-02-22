package za.co.woolworths.financial.services.android.contracts

open class AsyncAPIResponse {
    interface ResponseDelegate<T> {
        fun onSuccess(response: T)
        fun onFailure(errorMessage: String)
    }
}