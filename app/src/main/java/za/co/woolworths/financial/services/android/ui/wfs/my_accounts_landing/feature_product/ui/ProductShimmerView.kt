package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import za.co.woolworths.financial.services.android.ui.wfs.component.BrushShimmerEffect
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.box_shimmer_image
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.createLocator
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin

@Composable
fun ProductShimmerView(key: String,height : Dp = Dimens.eighty_five_dp) {
    BrushShimmerEffect { brush ->
        Box(
            Modifier
                .testAutomationTag(createLocator(box_shimmer_image, key))
                .clip(shape = MaterialTheme.shapes.extraLarge)
                .fillMaxWidth()
                .padding(
                    start = Margin.start,
                    end = Margin.end,
                    top = Margin.dp16
                )
                .height(height)
                .background(brush)
        )
    }
}