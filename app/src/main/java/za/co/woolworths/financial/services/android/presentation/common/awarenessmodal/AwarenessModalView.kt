package za.co.woolworths.financial.services.android.presentation.common.awarenessmodal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.awfs.coordination.R
import za.co.wigroup.androidutils.views.BottomButtonView
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.presentation.common.DescriptionText
import za.co.woolworths.financial.services.android.presentation.common.DialogHandle
import za.co.woolworths.financial.services.android.presentation.common.TitleText
import za.co.woolworths.financial.services.android.presentation.common.UnderlineButton
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight32dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight40dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight8dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

@Composable
fun AwarenessModalView(
    viewModel: AwarenessViewModel,
    onEvent: (AwarenessScreenEvents) -> Unit
) {
   val uiState by viewModel.screenState.collectAsStateWithLifecycle()
    AwarenessModalViewStateless(uiState = uiState, onEvent = onEvent)
}

@Composable
fun AwarenessModalViewStateless(
    modifier: Modifier = Modifier,
    uiState: AwarenessModalState,
    onEvent: (AwarenessScreenEvents) -> Unit
) {

    Column (
        modifier = modifier
            .background(White)
            .padding(vertical = 8.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ){
        DialogHandle()
        SpacerHeight40dp()
        Image(
            painter = painterResource(id = uiState.iconAwareness),
            contentDescription = stringResource(id = R.string.cd_awareness_icon)
        )
        SpacerHeight32dp()
        TitleText(text = stringResource(id = uiState.awarenessTitle))
        SpacerHeight8dp()
        DescriptionText(text = stringResource(id = uiState.awarenessDesc))

        SpacerHeight24dp()
        NoteView(
            Modifier.fillMaxWidth(),
            text = stringResource(id = uiState.noteText),
            value = uiState.isChecked
        ) {
            onEvent(AwarenessScreenEvents.DontShowAgainClicked(it))
        }
        SpacerHeight16dp()
        BlackButton(text = stringResource(id = uiState.confirmButton).uppercase()) {
            onEvent(AwarenessScreenEvents.ConfirmButtonClick)
        }
        UnderlineButton(text = stringResource(id = uiState.dismissButton)) {
            onEvent(AwarenessScreenEvents.DismissButtonClick)
        }
    }
}


@Preview
@Composable
fun PreviewAwarenessModalView(){
    OneAppTheme {
        AwarenessModalViewStateless(uiState = AwarenessModalState(
            iconAwareness = R.drawable.ic_awareness_substitute,
            awarenessTitle = R.string.awareness_substitute_title,
            awarenessDesc = R.string.awareness_substitute_desc,
            confirmButton = R.string.choose_substitutes,
            dismissButton = R.string.continue_to_checkout,
            noteText = R.string.my_list_delete_this_list_checkbox_title
        )) {}
    }
}
