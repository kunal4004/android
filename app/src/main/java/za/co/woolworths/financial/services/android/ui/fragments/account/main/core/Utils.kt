package za.co.woolworths.financial.services.android.ui.fragments.account.main.core


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource.IOTaskResult

/**
 * Util method that takes a suspend function returning a [Flow] of [IOTaskResult] as input param and returns a
 * [Flow] of [ViewState], which emits [ViewState.Loading] with true prior to performing the IO Task. If the
 * IO operation results a [IOTaskResult.OnSuccess], the result is mapped to a [ViewState.RenderSuccess] instance and emitted,
 * else a [IOTaskResult.OnFailed] is mapped to a [ViewState.RenderFailure] instance and emitted.
 * The flowable is then completed by emitting a [ViewState.Loading] with false
 */
suspend fun <T : Any> getViewStateFlowForNetworkCall(ioOperation: suspend () -> Flow<IOTaskResult<T>>): Flow<ViewState<T>> =
    flow {
        emit(ViewState.Loading(true))
        ioOperation().map {
            when (it) {
                is IOTaskResult.OnSuccess -> ViewState.RenderSuccess(it.data)
                is IOTaskResult.OnFailure -> ViewState.RenderErrorFromResponse(it.data as T)
                is IOTaskResult.OnFailed -> ViewState.RenderFailure(it.throwable)
                is IOTaskResult.Empty -> ViewState.RenderEmpty
            }
        }.collect {
            emit(it)
        }
        emit(ViewState.Loading(false))
    }.flowOn(Dispatchers.IO)
