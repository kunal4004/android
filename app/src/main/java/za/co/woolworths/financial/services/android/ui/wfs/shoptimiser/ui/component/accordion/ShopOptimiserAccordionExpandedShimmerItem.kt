package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.accordion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import za.co.woolworths.financial.services.android.ui.wfs.component.BrushShimmerEffect
import za.co.woolworths.financial.services.android.ui.wfs.component.DividerLight1dp
import za.co.woolworths.financial.services.android.ui.wfs.component.ShimmerLabelWithRoundedCorner
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight6dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth24dp
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.White


/**
 * Composable function for previewing the Shop Optimiser Accordion Loading Shimmer.
 * This function wraps the loading shimmer in a theme for previewing purposes.
 */
@Preview
@Composable
fun ShopOptimiserAccordionLoadingShimmerPreview() {
    OneAppTheme {
        ShopOptimiserAccordionLoadingShimmer()
    }
}

/**
 * Composable function for displaying the Shop Optimiser Accordion Loading Shimmer.
 */
@Composable
fun ShopOptimiserAccordionLoadingShimmer() {
    Column(modifier = Modifier.background(White)) {
        SpacerHeight16dp()
        Row {
            SpacerWidth24dp()
            ShimmerIconLabel(
                width = Dimens.fifty_dp,
                height = Dimens.thirty_four_dp
            )
            SpacerWidth16dp()
            Column {
                ShimmerLabelWithRoundedCorner(
                    width = 0.4f,
                    height = Dimens.fifteen_dp
                )
                SpacerHeight6dp(height = Dimens.four_dp)
                ShimmerLabelWithRoundedCorner(
                    width = 0.3f,
                    height = Dimens.fifteen_dp
                )
            }
        }
    }
    SpacerHeight16dp()
    DividerLight1dp()
}

/**
 * Composable function for displaying a shimmering icon label.
 * @param height The height of the shimmering icon label.
 * @param width The width of the shimmering icon label.
 */
@Composable
fun ShimmerIconLabel(height: Dp = Dimens.icon_size_dp, width: Dp = Dimens.icon_size_dp) {
    BrushShimmerEffect { brush ->
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .testAutomationTag(AutomationTestScreenLocator.box_shimmer_icon_label)
                .clip(MaterialTheme.shapes.small)
                .background(brush = brush)
        )
    }
}

