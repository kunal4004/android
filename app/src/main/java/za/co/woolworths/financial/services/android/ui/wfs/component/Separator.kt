package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.DividerColor
import za.co.woolworths.financial.services.android.ui.wfs.theme.DividerColorPDP
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppBackground


@Composable
fun DividerThicknessOne() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(start = 24.dp, end = 15.dp)
            .background(OneAppBackground)
    )
}

@Composable
fun DividerThicknessEight() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(OneAppBackground)
    )
}

@Composable
fun Divider1dp() {
    Divider(
        modifier = Modifier
            .height(1.dp)
            .padding(start = 24.dp, end = 24.dp)
            .background(DividerColor)
    )

}

@Composable
fun DividerLight1dp() {
    Divider(
        modifier = Modifier
            .height(1.dp)
            .padding(start = 24.dp, end = 24.dp)
            .background(DividerColorPDP)
    )
}