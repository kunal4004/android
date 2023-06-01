package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.Content
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.box_shimmer_icon_label
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.box_shimmer_icon_rounded_corner
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.box_shimmer_label
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.box_shimmer_label_rounded_corner
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.box_shimmer_text_label
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.theme.Shimmer

@Composable
fun ShimmerTitleDescriptionAndNextArrowItem(brush : Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 24.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ShimmerLabel(brush = brush, width = 0.45f, height = 9.dp)
            ShimmerLabel(brush = brush, width = 0.3f, height = 7.dp)
        }
        ShimmerIconLabel(brush = brush, boxSize = 16.dp)
    }
}

@Composable
fun ShimmerTextLabel(width: Float = 0.4f,brush : Brush){
    Box(modifier = Modifier
        .fillMaxWidth(width)
        .testAutomationTag(box_shimmer_text_label)
        .padding(start = 24.dp, end = 2.dp, top = 16.dp, bottom = 16.dp)
        .clip(MaterialTheme.shapes.small)
        .height(16.dp)
        .background(brush = brush))
}

@Composable
fun ShimmerLabel(width: Float = 0.4f, height: Dp = 16.dp, brush: Brush) {
    Box(modifier = Modifier.
    fillMaxWidth(width)
        .testAutomationTag(box_shimmer_label)
        .clip(MaterialTheme.shapes.small)
        .height(height)
        .background(brush = brush))
}

@Composable
fun ShimmerLabelWithRoundedCorner(width: Float = 0.4f, height: Dp = 16.dp, brush: Brush, animateAlpha : Float = 1f) {
    Box(modifier = Modifier.
    fillMaxWidth(width)
        .testAutomationTag(box_shimmer_label_rounded_corner)
        .clip(MaterialTheme.shapes.extraLarge)
        .alpha(animateAlpha)
        .height(height)
        .background(brush = brush))
}

@Composable
fun ShimmerIconLabel(boxSize: Dp = 24.dp, brush: Brush) {
    Box(modifier = Modifier
        .size(boxSize)
        .testAutomationTag(box_shimmer_icon_label)
        .clip(MaterialTheme.shapes.small)
        .background(brush = brush))
}

@Composable
fun ShimmerIconWithRoundedCorner(boxSize: Dp = Shimmer.boxSize, brush: Brush, animateAlpha : Float = 1f) {
    Box(modifier = Modifier
        .size(boxSize)
        .testAutomationTag(box_shimmer_icon_rounded_corner)
        .clip(MaterialTheme.shapes.extraLarge)
        .alpha(animateAlpha)
        .background(brush = brush))
}


@Composable
fun LoadingShimmerList(contentList: MutableList<Content>) {
    BrushShimmerEffect { brush ->
        BoxBackground {
            ListColumn(list = contentList) { item ->
                Column {
                    ShimmerTextLabel(brush = brush)
                    DividerThicknessOne()
                }
                val size = item.children.size.minus(1)
                item.children.forEachIndexed { index, _ ->
                    Column {
                        ShimmerTitleDescriptionAndNextArrowItem(brush = brush)
                        if (index == (size)) DividerThicknessEight() else DividerThicknessOne()
                    }
                }
            }
        }
    }
}