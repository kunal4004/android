package za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class ProgressIndicator {
    object Spinning : ProgressIndicator()
    object Idle: ProgressIndicator()
    object Success : ProgressIndicator()
    object Failure : ProgressIndicator()
    object NoConnection : ProgressIndicator()
    object UnknownError : ProgressIndicator()
}

interface CircularIndicator {
    fun setState(state : ProgressIndicator)
}

class CircularProgressIndicatorViewModel : ViewModel(), CircularIndicator {

    private val _progressIndicator = MutableSharedFlow<ProgressIndicator>()
    val progressIndicator: SharedFlow<ProgressIndicator> = _progressIndicator

    override fun setState(state: ProgressIndicator) {
        viewModelScope.launch {
            _progressIndicator.emit(state)
        }
    }

}