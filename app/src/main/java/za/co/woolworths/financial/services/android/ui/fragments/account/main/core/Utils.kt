package za.co.woolworths.financial.services.android.ui.fragments.account.main.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource.IOTaskResult

/**
 * Util method that takes a suspend function returning a [Flow] of [IOTaskResult] as input param and returns a
 * [Flow] of [ViewState], which emits [ViewState.Loading] with true prior to performing the IO Task. If the
 * IO operation results a [IOTaskResult.Success], the result is mapped to a [ViewState.RenderSuccess] instance and emitted,
 * else a [IOTaskResult.OnFailed] is mapped to a [ViewState.RenderFailure] instance and emitted.
 * The flowable is then completed by emitting a [ViewState.Loading] with false
 */

suspend fun <T : Any> mapNetworkCallToViewStateFlow(ioOperation: suspend () -> Flow<IOTaskResult<T>>): Flow<ViewState<T>> {
    return flow {
        emit(ViewState.Loading(true))
        ioOperation().flowOn(Dispatchers.IO).map { task ->
            when (task) {
                is IOTaskResult.Success -> ViewState.RenderSuccess(task.data)
                is IOTaskResult.OnFailure -> ViewState.RenderErrorFromResponse(task.data)
                is IOTaskResult.OnFailed -> ViewState.RenderFailure(task.throwable)
                is IOTaskResult.Empty -> ViewState.RenderEmpty
                is IOTaskResult.NoConnectionState -> ViewState.RenderNoConnection
                is IOTaskResult.OnSessionTimeOut ->ViewState.RenderErrorFromResponse(task.data)
            }
        }.collect { viewState ->
            emit(viewState)
            if (viewState !is ViewState.RenderNoConnection) {
                emit(ViewState.Loading(false))
            }
        }
    }
}