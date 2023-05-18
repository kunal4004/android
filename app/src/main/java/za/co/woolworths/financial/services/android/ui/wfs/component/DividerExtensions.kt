package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppBackground


@Composable
fun SectionDivider(height: Dp = Dimens.eight_dp) {
    Spacer(
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .background(OneAppBackground)
    )
}