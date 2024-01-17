package za.co.woolworths.financial.services.android.presentation.common.confirmationdialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.presentation.common.ProgressView
import za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.components.ProgressViewUiState

@Composable
fun DeleteProgressBarView(
    state: ProgressViewUiState
) {
    ProgressView(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 290.dp),
        title = if (state.listName.isNotEmpty())
            stringResource(id = state.title, state.listName)
        else
            stringResource(id = state.title),
        desc = stringResource(id = state.desc)
    )
}
