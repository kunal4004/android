package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

@Composable
fun SpacerBottom(height: Dp = Dimens.ten_dp, bgColor: Color = White) {
    Spacer(modifier = Modifier
                    .height(height)
                    .fillMaxWidth()
                    .background(bgColor)
    )
}


@Composable
fun SpacerWidth8dp(width: Dp = Dimens.eight_dp, bgColor: Color = White) {
    Spacer(modifier = Modifier
            .width(width = width)
            .fillMaxWidth()
            .background(bgColor)
    )
}

@Composable
fun SpacerWidth16dp(width: Dp = Dimens.sixteen_dp, bgColor: Color = White) {
    Spacer(modifier = Modifier
            .width(width = width)
            .fillMaxWidth()
            .background(bgColor)
    )
}

@Composable
fun SpacerWidthDp(width: Dp = Dimens.sixteen_dp, bgColor: Color = White) {
    Spacer(modifier = Modifier
        .width(width = width)
        .fillMaxWidth()
        .background(bgColor)
    )
}

@Composable
fun SpacerWidth4dp(width: Dp = Dimens.four_dp, bgColor: Color = White) {
    Spacer(modifier = Modifier
        .width(width = width)
        .fillMaxWidth()
        .background(bgColor)
    )
}

@Composable
fun SpacerWidth24dp(width: Dp = Dimens.dp24, bgColor: Color = White) {
    Spacer(modifier = Modifier
                    .width(width = width)
                    .fillMaxWidth()
                    .background(bgColor)
    )
}

@Composable
fun SpacerHeight24dp(height: Dp = Dimens.dp24, bgColor: Color = White) {
    Spacer(modifier = Modifier
                    .height(height = height)
                    .fillMaxWidth()
                    .background(bgColor))
}

@Composable
fun SpacerHeight8dp(height: Dp = Dimens.eight_dp, bgColor: Color = White) {
    SpacerHeight24dp(height = height, bgColor = bgColor)
}

@Composable
fun SpacerHeight80dp(height: Dp = Dimens.eighty_dp, bgColor: Color = White) {
    SpacerHeight24dp(height = height, bgColor = bgColor)
}

@Composable
fun SpacerHeight10dp(height: Dp = Dimens.ten_dp, bgColor: Color = White) {
    SpacerHeight24dp(height = height, bgColor = bgColor)
}

@Composable
fun SpacerHeight12dp(height: Dp = Dimens.twelve_dp, bgColor: Color = White) {
    SpacerHeight24dp(height = height, bgColor = bgColor)
}

@Composable
fun SpacerHeight5dp(height: Dp = Dimens.five_dp, bgColor: Color = White) {
    SpacerHeight24dp(height = height, bgColor = bgColor)
}
@Composable
fun SpacerHeight6dp(height: Dp = Dimens.six_dp, bgColor: Color = White) {
    SpacerHeight24dp(height = height, bgColor = bgColor)
}

@Composable
fun SpacerHeight16dp(height: Dp = Margin.dp16, bgColor: Color = White) {
    SpacerHeight24dp(height = height, bgColor = bgColor)
}