package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
fun ShimmerTitleDescriptionAndNextArrowItem() {
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
            ShimmerLabel(width = 0.45f, height = 9.dp)
            ShimmerLabel(width = 0.3f, height = 7.dp)
        }
        ShimmerIconLabel(boxSize = 16.dp)
    }
}

@Composable
fun ShimmerTextLabel(width: Float = 0.4f){
    BrushShimmerEffect { brush ->
        Box(
            modifier = Modifier
                .fillMaxWidth(width)
                .testAutomationTag(box_shimmer_text_label)
                .padding(start = 24.dp, end = 2.dp, top = 16.dp, bottom = 16.dp)
                .clip(MaterialTheme.shapes.small)
                .height(16.dp)
                .background(brush = brush)
        )
    }
}

@Composable
fun ShimmerLabel(width: Float = 0.4f, height: Dp = 16.dp) {
    BrushShimmerEffect { brush ->
        Box(
            modifier = Modifier.fillMaxWidth(width)
                .testAutomationTag(box_shimmer_label)
                .clip(MaterialTheme.shapes.small)
                .height(height)
                .background(brush = brush)
        )
    }
}

@Composable
fun ShimmerLabelWithRoundedCorner(width: Float = 0.4f, height: Dp = 16.dp, animateAlpha : Float = 1f) {
    BrushShimmerEffect { brush ->
        Box(
            modifier = Modifier.fillMaxWidth(width)
                .testAutomationTag(box_shimmer_label_rounded_corner)
                .clip(MaterialTheme.shapes.extraLarge)
                .alpha(animateAlpha)
                .height(height)
                .background(brush = brush)
        )
    }
}

@Composable
fun ShimmerIconLabel(boxSize: Dp = 24.dp) {
    BrushShimmerEffect { brush ->
        Box(
            modifier = Modifier
                .size(boxSize)
                .testAutomationTag(box_shimmer_icon_label)
                .clip(MaterialTheme.shapes.small)
                .background(brush = brush)
        )
    }
}

@Composable
fun ShimmerIconWithRoundedCorner(boxSize: Dp = Shimmer.boxSize, animateAlpha : Float = 1f) {
    BrushShimmerEffect { brush ->
        Box(
            modifier = Modifier
                .size(boxSize)
                .testAutomationTag(box_shimmer_icon_rounded_corner)
                .clip(MaterialTheme.shapes.extraLarge)
                .alpha(animateAlpha)
                .background(brush = brush)
        )
    }
}


@Composable
fun LoadingShimmerList(contentList: MutableList<Content>) {
    BoxBackground {
        ListColumn(list = contentList) { item ->
            Column {
                ShimmerTextLabel()
                DividerThicknessOne()
            }
            val size = item.children.size.minus(1)
            item.children.forEachIndexed { index, _ ->
                Column {
                    ShimmerTitleDescriptionAndNextArrowItem()
                    if (index == (size)) DividerThicknessEight() else DividerThicknessOne()
                }
            }
        }
    }
}