package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.bounceClick
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.ui.ButtonState
import za.co.woolworths.financial.services.android.ui.wfs.theme.*

@Preview
@Composable
fun ButtonCarouselPreview() {
    OneAppTheme {
        Column {
            SurfaceTag(value = stringResource(id = R.string.apply_now))
        }
    }
}

@Composable
fun SurfaceTag(
    modifier: Modifier = Modifier,
    value: String,
    containerColor: Color = Obsidian,
    contentColor: Color = White) {
    Surface(
        modifier =  modifier.height(Dimens.button_height_twenty_six_dp),
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small) {
        TextOpenSansSemiBoldH3(
            modifier = modifier
                .wrapContentSize()
                .padding(
                    start = Dimens.fifteen_dp,
                    end = Dimens.fifteen_dp
                ),
            color = contentColor,
            text = value.uppercase(),
            letterSpacing = Dimens.one_sp,
            locator = ""
        )
    }
}

@Composable
fun ButtonBorder(label: String, modifier: Modifier = Modifier,textAlign : TextAlign = TextAlign.Start, onClick: () -> Unit) {
    OutlinedButton(
        modifier =  modifier.bounceClick { onClick() }.then(
            Modifier
                .height(Dimens.fifty_dp)
        ),
        shape = MaterialTheme.shapes.extraSmall,
        onClick = {onClick() },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = White,
            contentColor = Obsidian,
            disabledContainerColor = White,
            disabledContentColor = White,

            ),

        border = BorderStroke(Dimens.oneDp, BlackOpacity10)
    ) {
        TextOpenSansSemiBoldH3(text = label.uppercase(), modifier = modifier, textAlign = textAlign, letterSpacing = Dimens.one_sp, locator = "")
    }
}

@Preview
@Composable
fun SurfaceButtonPreview() {
    val label = "View"
    SurfaceTextButton(buttonState = ButtonState.IDLE, buttonLabel = label, locator = "") {}
}

@Composable
fun SurfaceTextButton(
    modifier: Modifier = Modifier,
    locator : String,
    isClickable : Boolean = true,
    buttonState: ButtonState = ButtonState.IDLE,
        buttonLabel: String,
        bgColor: Color? = null,
        onClick: () -> Unit
) {
    val isLoading = buttonState == ButtonState.LOADING
    Surface(
        modifier = if (isClickable)modifier.padding(top = 0.dp).then(Modifier
            .clickable { onClick() }) else modifier.then(Modifier.testAutomationTag(locator = locator)),
        color = bgColor ?: if (isLoading) Color.Transparent else WhiteWithOpacity10,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = Margin.dp10,
                    end = Margin.dp10,
                    top = Margin.dp6,
                    bottom = Margin.dp4
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (isLoading) {
                true -> {
                    CircularProgressIndicator(
                        strokeWidth = Dimens.two_dp,
                        modifier = Modifier
                            .width(Dimens.dp24)
                            .height(Dimens.dp24),
                        color = White,
                    )
                }
                false -> {
                    ButtonLabel(
                        text = buttonLabel.uppercase(), color = White,
                        modifier = Modifier.wrapContentWidth()
                            .wrapContentHeight()
                            .testAutomationTag(locator = locator)
                    )
                }
            }
        }
    }
}
