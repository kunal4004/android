package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.accordion

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.common.click.clickableSingle
import za.co.woolworths.financial.services.android.ui.wfs.component.DividerLight1dp
import za.co.woolworths.financial.services.android.ui.wfs.component.MyIcon
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight6dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerWidth16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.TextFuturaFamilyHeader1
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansSemiBoldH3
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.findActivity
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.AvailableFundsSufficiency
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.ProductOnDisplay
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.fragment.ShoptimiserDetailsFragment
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.viewmodel.AccordionDividerVisibility
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.viewmodel.ShopOptimiserViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.DisabledTextFunctionalGreyColor
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

/**
 * Composable function for displaying the content of a shop optimiser accordion item.
 * @param productOnDisplay A MutableMap entry containing the product information.
 */
@Composable
fun ShopOptimiserViewModel.ShopOptimiserAccordionContent(
    productOnDisplayItem: Map.Entry<String, ProductOnDisplay>
) {
    val productOnDisplay = productOnDisplayItem.value
    val key = productOnDisplayItem.key

    // Check if the product is currently loading
    if (productOnDisplay.isLoading) {
        ShopOptimiserAccordionLoadingShimmer()
    }

    // Check if the product is not loading
    if (!productOnDisplay.isLoading) {
        val isLastProduct = remember { productOnDisplay.isLastProduct }
        val isSufficientFundsAvailable =
            remember { productOnDisplay.isSufficientFundsAvailable == AvailableFundsSufficiency.SUFFICIENT }
        val title = remember { productOnDisplay.wfsPaymentMethods?.title ?: "N/A" }
        val description = remember {
            productOnDisplay?.wfsPaymentMethods?.description
                ?: productOnDisplay.availableFunds.toString()
        }

            Column(
                modifier = Modifier
                    .background(White)
                    .testAutomationTag(stringResource(id = R.string.shoptimiser_child_accordion_column))
            ) {
                val context = LocalContext.current

                DividerLight1dp()

                Row(
                    modifier = Modifier
                        .testAutomationTag(stringResource(id = R.string.shoptimiser_child_accordion_row))
                        .clickableSingle {                            // Handle click event if sufficient funds are available
                            selectedOnDisplayProduct = productOnDisplay
                            navigateToShopOptimiserDetailWidget(context.findActivity())
                        }
                        .padding(
                            bottom = if (isSufficientFundsAvailable) {
                                if (isLastProduct) Margin.noMargin else Margin.dp16
                            } else Margin.noMargin,
                            top = Margin.dp16,
                            start = Margin.start,
                            end = Margin.end
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display product icon
                    Image(
                        painter = painterResource(id = productOnDisplay.drawableId),
                        contentDescription = stringResource(
                            id = R.string.shoptimiser_child_accordion_icon, key
                        )
                    )
                    SpacerWidth16dp()
                    Column(modifier = Modifier.weight(1f)) {
                        // Display product title
                        TextFuturaFamilyHeader1(
                            text = title,
                            isUpperCased = true,
                            locator = stringResource(
                                id = R.string.shoptimiser_child_accordion_text,
                                key
                            ),
                            fontWeight = FontWeight.W500,
                            textColor = DisabledTextFunctionalGreyColor,
                            fontSize = FontDimensions.sp12
                        )
                        SpacerHeight6dp(height = Margin.dp4)
                        // Display product description or available funds
                        TextOpenSansSemiBoldH3(
                            text = description,
                            locator = stringResource(
                                id = R.string.shoptimiser_child_accordion_text,
                                key
                            ),
                            color = if (isSufficientFundsAvailable) Color.Black else DisabledTextFunctionalGreyColor,
                            fontSize = FontDimensions.sp14
                        )
                    }
                    // Display info icon
                    MyIcon(
                        id = R.drawable.icon_info,
                        contentDescriptionId = R.string.shoptimiser_child_accordion_image,
                        modifier = Modifier.size(Dimens.sixteen_dp)
                    )
                }

                if (!isSufficientFundsAvailable) {
                    SpacerHeight6dp(height = Margin.dp2)
                    TextOpenSansSemiBoldH3(
                        text = insufficientFundsFooterLabel(),
                        color = Color.Black,
                        modifier = Modifier
                            .padding(start = Margin.start)
                            .testAutomationTag("footerLabel"),
                        textAlign = TextAlign.Start,
                        fontSize = FontDimensions.sp11,
                        locator = "footerLabel"
                    )
                    SpacerHeight6dp()
                }

                // Add a divider if this is the last product
                if (isLastProduct && accordionDividerVisibility == AccordionDividerVisibility.VISIBLE) {
                    DividerLight1dp()
                }
            }
        }
}


/**
 * Navigate to the Shoptimiser Details Fragment.
 * @param activity The parent activity where the fragment should be shown.
 */
fun navigateToShopOptimiserDetailWidget(activity: AppCompatActivity?) {
    activity?.supportFragmentManager?.let {
        ShoptimiserDetailsFragment().show(it, ShoptimiserDetailsFragment::class.java.simpleName)
    }
}
