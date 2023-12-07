package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.accordion

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.rotateComponent
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.viewmodel.ShopOptimiserViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin

/**
 * Composable function to display a rotated arrow icon when accordion expand/collapse.
 * @param degrees The angle in degrees by which to rotate the arrow icon.
 * @param locatorId The string resource ID for the content description of the arrow icon.
 * @param iconId The drawable resource ID for the arrow icon.
 */

@Composable
fun RotatedArrowIcon(
    degrees: Float,
    @StringRes locatorId: Int = R.string.shoptimiser_chevron_up_icon,
    @DrawableRes iconId: Int = R.drawable.ic_chevron_up) {
    Icon(
        painter = painterResource(id = iconId),
        contentDescription = stringResource(id = locatorId),
        modifier = Modifier.rotate(degrees),
    )
}

/**
 * Composable function to display an accordion-style parent component on product detail page.
 * @param viewModel The view model containing data for the component.
 */
@Composable
fun ShopOptimiserAccordionParentItem(viewModel: ShopOptimiserViewModel) {
    // Retrieve component title and description from the view model
    val componentTitle = remember { viewModel.getComponentTitle() ?: ""}
    val componentDescription = remember { viewModel.getComponentDescription() ?: "" }

    // Determine the arrow rotation degree based on the expansion state
    val rotationDegreeArrow = rotateComponent(isExpanded = viewModel.isExpanded)

    Row(
        modifier = Modifier
            .testAutomationTag(stringResource(id = R.string.shoptimiser_parent_accordion_container_row))
            .padding(
                start = Margin.start,
                end = Margin.end,
                top = Margin.noMargin,
                bottom = if (viewModel.isExpanded) Margin.dp16 else Margin.noMargin
            )
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                onClick = {
                    with(viewModel) {
                        // Toggle the expansion state
                        isExpanded = !isExpanded

                        // If expanded, retrieve WFS products for the user
                        if (isExpanded) {
                            getWFSProductsForUser()
                        }
                    }
                }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier
            .weight(1f)
            .testAutomationTag(stringResource(id = R.string.shoptimiser_parent_accordion_container_column))) {
            // Display the component title with specified attributes
            TextOpenSansFontFamily(
                text = componentTitle,
                color = Color.Black,
                fontSize = FontDimensions.sp14,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .testAutomationTag(stringResource(id = R.string.shoptimiser_parent_accordion_title_text)),
                locator = componentTitle
            )

            // Display the component description with specified attributes
            TextOpenSansFontFamily(
                text = componentDescription,
                color = Color.Black,
                fontWeight = FontWeight.W400,
                fontSize = FontDimensions.sp14,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .testAutomationTag(stringResource(id = R.string.shoptimiser_parent_accordion_subtitle_text)),
                locator = componentDescription
            )
        }

        // Display a rotated arrow icon to indicate expansion state
        RotatedArrowIcon(degrees = rotationDegreeArrow)
    }
}