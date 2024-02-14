package za.co.woolworths.financial.services.android.presentation.common.awarenessmodal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.LocalMinimumTouchTargetEnforcement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import za.co.woolworths.financial.services.android.presentation.common.CheckboxTitleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteView(
    modifier: Modifier = Modifier,
    text: String = "",
    value: Boolean = false,
    showSwitch: Boolean = true,
    onCheckBoxChange: (Boolean) -> Unit,
) {
    if(showSwitch) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CheckboxTitleText(modifier = Modifier.fillMaxWidth().weight(1f), text = text)
            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                Checkbox(
                    modifier = Modifier.scale(0.7f),
                    checked = value,
                    onCheckedChange = onCheckBoxChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.Black,
                        uncheckedColor = Color.Black,
                        checkmarkColor = Color.White
                    )
                )
            }
        }
    } else {
        CheckboxTitleText(modifier, text)
    }
}
