package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import za.co.woolworths.financial.services.android.ui.wfs.component.BrushShimmerEffect
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin

@Preview
@Composable
fun OfferShimmerViewPreview(){
    OfferShimmerView()
}

@Composable
fun OfferShimmerView(modifier  : Modifier = Modifier
    .fillMaxWidth()
    .padding(start = Margin.start, end = Margin.end),
) {
    BrushShimmerEffect { brush ->
        Box(
            modifier = modifier
                .height(Dimens.hundredSeventyDp)
                .background(brush)
        ) {}
    }
}