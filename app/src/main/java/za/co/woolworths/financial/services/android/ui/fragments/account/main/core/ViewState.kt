package za.co.woolworths.financial.services.android.ui.fragments.account.main.core

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
