package za.co.woolworths.financial.services.android.ui.wfs.theme.dimens

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class MarginDimensions(
    val noMargin : Dp = 0.dp,
    val start: Dp = 24.dp,
    val end: Dp = 24.dp,
    val top: Dp = 24.dp,
    val bottom: Dp = 24.dp,
    val dp3: Dp = 3.dp,
    val dp22: Dp = 22.dp,
    val dp20: Dp = 20.dp,
    val dp16: Dp = 16.dp,
    val dp2 : Dp = 2.dp,
    val dp13: Dp = 13.dp,
    val dp15: Dp = 15.dp,
    val dp8: Dp = 8.dp,
    val dp6: Dp = 6.dp,
    val dp4: Dp = 4.dp,
    val dp10: Dp = 10.dp,
    val dp24: Dp = 24.dp,
    val account_landing_general_icon_spacing_end: Dp = 24.dp
)

val sw320MarginDimensions by lazy {
    MarginDimensions(
        account_landing_general_icon_spacing_end = 16.dp)
}

val sw360MarginDimensions by lazy {
    MarginDimensions(
        account_landing_general_icon_spacing_end = 16.dp)
}


val sw400MarginDimensions by lazy {
    MarginDimensions(
        account_landing_general_icon_spacing_end = 16.dp)
}


val sw440MarginDimensions by lazy {
    MarginDimensions(
        account_landing_general_icon_spacing_end = 16.dp
    )
}

val sw480MarginDimensions by lazy {
    MarginDimensions(
        account_landing_general_icon_spacing_end = 16.dp)
}

val sw520MarginDimensions by lazy {
    MarginDimensions(
        account_landing_general_icon_spacing_end = 16.dp)
}

val sw560MarginDimensions by lazy {
    MarginDimensions(
        account_landing_general_icon_spacing_end = 16.dp)
}

val sw600MarginDimensions by lazy {
    MarginDimensions(
        account_landing_general_icon_spacing_end = 16.dp)
}
