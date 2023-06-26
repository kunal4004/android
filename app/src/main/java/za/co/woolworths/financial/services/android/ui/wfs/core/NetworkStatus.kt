package za.co.woolworths.financial.services.android.ui.wfs.core

import androidx.compose.runtime.Stable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState

@Stable
data class NetworkStatusUI<T>(
    var data: T? = null,
    val isLoading: Boolean = false,
    var hasError: Boolean = false,
    val errorMessage: RetrofitFailureResult? = null,
    val throwable: Throwable? = null
)

sealed class RetrofitFailureResult {
    data class ServerResponse<T>(val data : T) : RetrofitFailureResult()
    object NoConnectionState : RetrofitFailureResult()
    data class SessionTimeout<T>(val data : T?) : RetrofitFailureResult()
    object SocketTimeOut : RetrofitFailureResult()
}

/**
 * Util method that takes a suspend function returning a [Flow] of [IOTaskResult] as input param and returns a
 * [Flow] of [ViewState], which emits [ViewState.Loading] with true prior to performing the IO Task. If the
 * IO operation results a [IOTaskResult.Success], the result is mapped to a [ViewState.RenderSuccess] instance and emitted,
 * else a [IOTaskResult.OnFailed] is mapped to a [ViewState.RenderFailure] instance and emitted.
 * The flowable is then completed by emitting a [ViewState.Loading] with false
 */

suspend fun <T : Any> mapNetworkCallToViewStateFlow(ioOperation: suspend () -> Flow<CoreDataSource.IOTaskResult<T>>): Flow<ViewState<T>> {
    return flow {
        emit(ViewState.Loading(true))
        ioOperation().flowOn(Dispatchers.IO).map { task ->
            when (task) {
                is CoreDataSource.IOTaskResult.Success -> ViewState.RenderSuccess(task.data)
                is CoreDataSource.IOTaskResult.OnFailure -> ViewState.RenderErrorFromResponse(task.data)
                is CoreDataSource.IOTaskResult.OnSessionTimeOut -> ViewState.RenderErrorFromResponse(task.data)
                is CoreDataSource.IOTaskResult.OnFailed -> ViewState.RenderFailure(task.throwable)
                is CoreDataSource.IOTaskResult.Empty -> ViewState.RenderEmpty
                is CoreDataSource.IOTaskResult.NoConnectionState -> ViewState.RenderNoConnection
            }
        }.collect { viewState ->
            emit(viewState)
            if (viewState !is ViewState.RenderNoConnection) {
                emit(ViewState.Loading(false))
            }
        }
    }
}

suspend fun <T : Any> mapNetworkState(ioOperation: suspend () -> Flow<CoreDataSource.IOTaskResult<T>>): Flow<NetworkStatusUI<T>> {
    return flow {
        val viewState = NetworkStatusUI<T>(data = null, isLoading = true, hasError = false, errorMessage = null)
        emit(viewState)
        ioOperation().flowOn(Dispatchers.IO).map { task ->
            when (task) {
                is CoreDataSource.IOTaskResult.Success -> {
                    viewState.copy(
                        data = task.data,
                        isLoading = false,
                        hasError = false,
                        errorMessage = null
                    )
                }

                is CoreDataSource.IOTaskResult.OnSessionTimeOut -> viewState.copy(
                    data = null,
                    isLoading = false,
                    hasError = true,
                    errorMessage = RetrofitFailureResult.SessionTimeout(data = task.data)
                )

                is CoreDataSource.IOTaskResult.OnFailure -> viewState.copy(
                    data = null,
                    isLoading = false,
                    hasError = true,
                    errorMessage = RetrofitFailureResult.ServerResponse(data = task.data)
                )

                is CoreDataSource.IOTaskResult.OnFailed -> viewState.copy(
                    data = null,
                    isLoading = false,
                    hasError = true,
                    errorMessage = null,
                    throwable = task.throwable
                )

                is CoreDataSource.IOTaskResult.Empty -> viewState.copy(
                    data = null,
                    isLoading = false,
                    hasError = false,
                    errorMessage = null
                )

                is CoreDataSource.IOTaskResult.NoConnectionState -> viewState.copy(
                    data = null,
                    isLoading = false,
                    hasError = true,
                    errorMessage = RetrofitFailureResult.NoConnectionState
                )
            }
        }.collect { result -> emit(result) }
    }
}
