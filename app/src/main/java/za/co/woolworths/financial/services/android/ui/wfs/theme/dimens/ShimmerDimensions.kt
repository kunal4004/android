package za.co.woolworths.financial.services.android.ui.wfs.theme.dimens

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class ShimmerDimensions(
    val point35F: Float = 0.35f,
    val sevenDp: Dp = 7.dp,
    val pointFiveFiveF: Float = 0.55f,
    val tenDp: Dp = 10.dp,
    val boxSize: Dp = 24.dp
)

val sw320ShimmerDimensions by lazy { ShimmerDimensions( )}