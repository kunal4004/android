package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight16dp
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight24dp
import za.co.woolworths.financial.services.android.ui.wfs.component.TextFuturaFamilyHeader1
import za.co.woolworths.financial.services.android.ui.wfs.component.TextOpenSansFontFamilyAnnotateString
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.White

@Preview
@Composable
fun ShowGenericErrorPopupWithTitleAndDescriptionPreview() {
    val infoTitle = "Available Balance"
    val infoDescription = AnnotatedString("This is the balance that you have available to you on your account. If the amount is greyed out, lorem ipsum sit dolor.")
    val buttonLabel = "GOT IT"
    ShowGenericErrorPopupWithTitleAndDescription(title = infoTitle, description = infoDescription, buttonLabel = buttonLabel ){}
}

@Composable
fun ShowGenericErrorPopupWithTitleAndDescription(
    title: String?,
    description: AnnotatedString?,
    buttonLabel: String? = stringResource(id = R.string.ok),
    onClick: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White),
        verticalArrangement = Arrangement.Center
    ) {
        // Add a swipe-up indicator
        SwipeUpRectangleUiIndicator()

        // Display the info title
        TextFuturaFamilyHeader1(
            text = title ?: "N/A",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Add spacing
        SpacerHeight16dp()

        // Display the info description
        TextOpenSansFontFamilyAnnotateString(
            modifier = Modifier.padding(start = Margin.start, end = Margin.end),
            annotatedString =description,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Start,
            locator = "infoDescription",
            color = Color.Black
        )

        // Add spacing
        SpacerHeight24dp(height = Dimens.twenty_six_dp)

        // Display the "Got It" button
        GotItButton(buttonLabel = buttonLabel, onClick = onClick)
    }
}