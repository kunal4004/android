package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.Divider1dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.TextFuturaFamilyHeader1
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFamilyBoldH1
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFontFamilyAnnotateString
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.bounceClick
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.ShopOptimiserConstant.ShopOptimiserConstant.PayFlexKey
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.viewmodel.ShopOptimiserViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.DropdownIndicatorBackground
import za.co.woolworths.financial.services.android.ui.wfs.theme.FloatDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

/**
 * Composable function for rendering the Shoptimiser Accordion Detail Popup.
 * @param viewModel The ViewModel for Shoptimiser providing data.
 * @param onClick A callback function to handle button click events.
 */
@Composable
fun ShopOptimiserAccordionDetailPopup(viewModel: ShopOptimiserViewModel, onClick: () -> Unit) {

    // Obtain relevant data from the ViewModel
    val productOnDisplay = viewModel.selectedOnDisplayProduct
    val availableFundsTitle = viewModel.getInfoLabelAvailableBalance()
    val availableFunds = productOnDisplay?.availableFunds
    val infoTitle = productOnDisplay?.wfsPaymentMethods?.infoTitle ?: ""
    val infoDescription = viewModel.getInfoDescription(productOnDisplay)
    val infoFooterTitle = productOnDisplay?.wfsPaymentMethods?.infoFooterTitle
    val infoFooterDescription = productOnDisplay?.wfsPaymentMethods?.infoFooterDescription
    val cashbackPercentage = productOnDisplay?.earnCashBack
    val cashbackTitle = viewModel.getInfoLabelEarnCashback()

    // Composable layout
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White),
        verticalArrangement = Arrangement.Center
    ) {
        // Add a swipe-up indicator
        SwipeUpRectangleUiIndicator()

        // Display the product image
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    id = viewModel.getShopOptimiserProductImage(
                        accountBinNumber = productOnDisplay?.wfsPaymentMethods?.accountNumberBin,
                        productGroupCode = productOnDisplay?.wfsPaymentMethods?.productGroupCode
                            ?: PayFlexKey
                    )
                ),
                contentDescription = "product_image"
            )
        }

        // Add spacing
        SpacerHeight16dp(height = Dimens.thirty_four_dp)

        // Display the info title
        TextFuturaFamilyHeader1(
            text = infoTitle,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Add spacing
        SpacerHeight16dp()

        // Display the info description
        TextOpenSansFontFamilyAnnotateString(
            modifier  = Modifier.padding(start = Margin.start, end = Margin.end),
            annotatedString = infoDescription,
            fontWeight = FontWeight.W400,
            locator = "infoDescription",
            color = Color.Black
        )

        // Add spacing
        SpacerHeight16dp()

        // Display cashback percentage if available
        cashbackPercentage?.let { DetailInfoItem(cashbackTitle, it) }

        // Display available funds if available
        availableFunds?.let { DetailInfoItem(title = availableFundsTitle ?: "", amount = availableFunds) }

        // Display info footer if neither cashback nor available funds are present
        if (cashbackPercentage.isNullOrEmpty() && availableFunds.isNullOrEmpty()) {
            Divider1dp()
            SpacerHeight16dp()
            Column(modifier = Modifier.padding(start = Margin.start, end = Margin.end)) {
                infoFooterTitle?.let {
                    TextOpenSansFamilyBoldH1(
                        text = it,
                        fontSize = FontDimensions.sp14,
                        locator = it
                    )
                }
                SpacerHeight16dp()
                infoFooterDescription?.let { description ->
                    TextOpenSansFontFamily(
                        text = description,
                        textAlign = TextAlign.Start,
                        fontSize = FontDimensions.sp13,
                        color = Color.Black,
                        locator = description
                    )
                }
            }
        }

        // Add spacing
        SpacerHeight24dp(height = Dimens.twenty_six_dp)

        // Display the "Got It" button
        GotItButton(onClick = onClick)
    }
}

/**
 * Composable function for rendering a native-style swipe-up indicator.
 * This function displays a visual indicator to suggest a swipe-up action.
 */
@Composable
fun SwipeUpRectangleUiIndicator() {
    // Add vertical spacing above the indicator
    SpacerHeight16dp()

    // Create a centered row for the swipe-up indicator
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        // Display a box with a background to represent the swipe-up indicator
        Box(
            modifier = Modifier
                .width(Dimens.forty_dp)
                .height(Dimens.four_dp)
                .clip(RoundedCornerShape(FloatDimensions.native_swipe_up_rounded_corner))
                .background(DropdownIndicatorBackground)
        )
    }

    // Add vertical spacing below the indicator
    SpacerHeight16dp(height = Dimens.forty_dp)
}

/**
 * Composable function for rendering a "Got It" button with optional text.
 * @param text The text to display on the button (default is "Got It").
 * @param onClick A callback function to handle button click events.
 */
@Composable
fun GotItButton(text: String? = stringResource(id = R.string.got_it), onClick : () -> Unit) {
    // Create a row layout for the button
    Row(
        modifier = Modifier.padding(
            start = Margin.start,
            end = Margin.end,
            top = Margin.top,
            bottom = Margin.bottom)
            .bounceClick {onClick.invoke()}) {
        // Create a TextButton with the provided text
        TextButton(modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.fifty_dp),
            shape = RectangleShape,
            border = BorderStroke(Dimens.oneDp, brush = SolidColor(Color.Black)),
            colors = ButtonDefaults.textButtonColors(containerColor = Color.Black),
            onClick = { onClick.invoke()  }) {
            // Display the button text with specified styles
            TextFuturaFamilyHeader1(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black),
                text = text?.uppercase() ?: "",
                textColor = Color.White,
                letterSpacing = 0.6.sp,
                fontSize = FontDimensions.sp12,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Composable function for previewing the "Got It" button.
 */
@Preview
@Composable
fun GotItButtonPreview(){
    GotItButton {
        // Provide a sample onClick callback for preview
    }
}

/**
 * Composable function for rendering a detail item with a title and amount.
 * @param title The title of the detail item.
 * @param amount The amount associated with the detail item.
 */
@Composable
fun DetailInfoItem(title: String?, amount: String) {
    // Create a column layout for the detail item
    Column {
        // Add a divider line above the detail item
        Divider1dp()

        // Create a row for displaying the title and amount
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Margin.start,
                end = Margin.end,
                top = Margin.top,
                bottom = Margin.bottom
            )) {
            // Display the title with specified styles
            TextOpenSansFamilyBoldH1(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = title,
                fontSize = FontDimensions.sp14, locator = title ?: ""
            )

            // Display the amount with specified styles
            TextOpenSansFontFamily(
                modifier = Modifier.wrapContentWidth(),
                text = amount,
                color  = Color.Black,
                fontSize = FontDimensions.sp14,
                locator = "$title Amount"
            )
        }
    }
}

