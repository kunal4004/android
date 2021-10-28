package za.co.woolworths.financial.services.android.ui.vto.utils


sealed class Resource<T>(val data: T?, val message: String) {
    class Success<T>(data: T) : Resource<T>(data, "")
    class Error<T>(message: String) : Resource<T>(null, message)
    class NoFace<T>(message: String) : Resource<T>(null, message)
}