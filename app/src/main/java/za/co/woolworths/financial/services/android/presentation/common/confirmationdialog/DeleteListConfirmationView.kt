package za.co.woolworths.financial.services.android.presentation.common.confirmationdialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.presentation.common.CheckboxTitleText
import za.co.woolworths.financial.services.android.presentation.common.DescriptionText
import za.co.woolworths.financial.services.android.presentation.common.TitleText
import za.co.woolworths.financial.services.android.presentation.common.UnderlineButton
import za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.components.ConfirmationUiState

@Composable
fun DeleteListConfirmationView(
    modifier: Modifier = Modifier,
    data: ConfirmationUiState,
    onCheckBoxChange: (Boolean) -> Unit,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        TitleText(text = stringResource(id = data.title))
        Spacer(modifier = Modifier.height(8.dp))
        DescriptionText(
            modifier = Modifier
                .padding(horizontal = 24.dp),
            text = stringResource(id = data.desc)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (data.showCheckBox) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CheckboxTitleText(text = stringResource(id = data.checkBoxTitle))
                Checkbox(
                    modifier = Modifier.scale(0.7f),
                    checked = data.isChecked,
                    onCheckedChange = onCheckBoxChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.Black,
                        uncheckedColor = Color.Black,
                        checkmarkColor = Color.White
                    )
                )
            }
        }

        BlackButton(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp),
            text = stringResource(id = data.confirmText).uppercase(),
            onButtonClick = onConfirmClick,
        )

        UnderlineButton(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 8.dp),
            text = stringResource(id = data.cancelText),
            onButtonClick = onCancelClick
        )
    }
}
