package za.co.woolworths.financial.services.android.ui.wfs.theme.extension

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import za.co.woolworths.financial.services.android.ui.wfs.theme.Obsidian
import za.co.woolworths.financial.services.android.ui.wfs.theme.WhiteWithOpacity30

class OneAppRippleTheme : RippleTheme {

    @Composable
    override fun defaultColor(): Color = WhiteWithOpacity30
    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(
        draggedAlpha = 0.5f,
        focusedAlpha = 0.3f,
        hoveredAlpha = 0.2f,
        pressedAlpha = 0.6f,
    )

}