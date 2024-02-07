package za.co.woolworths.financial.services.android.presentation.common.awarenessmodal

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AwarenessViewModel @Inject constructor(

) : ViewModel() {

    private var _screenState = MutableStateFlow(AwarenessModalState())
    val screenState = _screenState.asStateFlow()


}