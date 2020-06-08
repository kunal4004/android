package za.co.woolworths.financial.services.android.contracts

interface IResponseListener<T> {
    fun onSuccess(response: T?)
    fun onFailure(error: Throwable?){}
}