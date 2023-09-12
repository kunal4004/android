package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.standalone

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFontFamilyAnnotateString
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.findActivity
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.accordion.navigateToShopOptimiserDetailWidget
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.viewmodel.ShopOptimiserViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Black
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
/**
 * Composable function for rendering the Shop Optimiser Standalone PayFlex UI.
 * Displays a PayFlex payment option if it's enabled.
 */
@Composable
fun ShopOptimiserViewModel.ShopOptimiserPayFlexStandAloneUI() {

    // Check if the Standalone PayFlex view is enabled
    if (isStandAlonePayFlexViewEnabled()) {
        val context = LocalContext.current

        // Create a clickable row for PayFlex payment
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = MutableInteractionSource(), // Remove ripple effect
                    indication = null,
                    onClick = {
                        // Handle click event by setting the selected product and navigating to detail widget
                        selectedOnDisplayProduct = standAlonePayFlexPaymentOnDisplay()
                        navigateToShopOptimiserDetailWidget(context.findActivity())
                    })
                .background(Color.White)
                .padding(
                    top = Margin.dp16,
                    start = Margin.dp24,
                    end = Margin.end,
                    bottom = Margin.dp24
                ),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display the PayFlex payment option text
            TextOpenSansFontFamilyAnnotateString(
                annotatedString = generatePayFlexAnnotatedString(),
                color = Black,
                fontSize = FontDimensions.sp12,
                textAlign = TextAlign.Start,
                lineHeight = Dimens.twenty_sp,
                modifier = Modifier.weight(1f).padding(end = Margin.dp4),
                locator = "PayFlexText"
            )

            // Display the PayFlex payment option icon
            Image(painter = painterResource(id = R.drawable.ic_pay_flex), contentDescription = "")
        }
    }
}
