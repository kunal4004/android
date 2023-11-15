package za.co.woolworths.financial.services.android.ui.wfs.common.blur

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.tooling.preview.Preview
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens

@Preview
@Composable
fun BlurViewPreview() {
    BlurView {}
}

@Composable
fun BlurView(modifier : Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize().blur(Dimens.thirty_one_dp)) {
        content()
    }
}