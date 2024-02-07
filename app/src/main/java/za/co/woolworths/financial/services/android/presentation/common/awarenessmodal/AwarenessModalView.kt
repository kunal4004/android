package za.co.woolworths.financial.services.android.presentation.common.awarenessmodal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AwarenessModalView(
    viewModel: AwarenessViewModel,
    onEvent: (AwarenessScreenEvents) -> Unit
) {
   val uiState by viewModel.screenState.collectAsStateWithLifecycle()
    AwarenessModalViewStateless(uiState, onEvent)
}

@Composable
fun AwarenessModalViewStateless(
    uiState: AwarenessModalState,
    onEvent: (AwarenessScreenEvents) -> Unit
) {


}
