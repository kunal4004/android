package za.co.woolworths.financial.services.android.shoppinglist.view

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily
import za.co.woolworths.financial.services.android.shoppinglist.component.EmptyStateData
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily

/**
 * Created by Kunal Uttarwar on 11/10/23.
 */


@Composable
fun EmptyStateTemplate(
    uiState: EmptyStateData = EmptyStateData(),
    onClickEvent: (isSignedOut: Boolean) -> Unit,
) {
    // No lists created
    EmptyStateTemplateStateless(
        icon = uiState.icon,
        title = uiState.title,
        description = uiState.description,
        buttonText = uiState.buttonText,
        isButtonVisible = uiState.isButtonVisible,
        onClickEvent = {
            onClickEvent(uiState.isSignedOut)
        }
    )
}

@Composable
fun EmptyStateTemplateStateless(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes description: Int,
    @StringRes buttonText: Int,
    isButtonVisible: Boolean,
    onClickEvent: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.color_FFFFFF)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = "Empty List Icon"
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = stringResource(id = title),
            lineHeight = 30.sp,
            modifier = Modifier.padding(horizontal = 81.dp),
            textAlign = TextAlign.Center,
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = description),
            modifier = Modifier.padding(horizontal = 47.dp),
            textAlign = TextAlign.Center,
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colorResource(id = R.color.color_444444)
        )
        Spacer(modifier = Modifier.height(40.dp))
        if (isButtonVisible) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 24.dp)
                    .background(color = Color.White, shape = RectangleShape),
                onClick = {
                    onClickEvent()
                }) {
                Text(
                    text = stringResource(id = buttonText).uppercase(),
                    letterSpacing = 1.5.sp,
                    fontFamily = FuturaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.black)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateTemplatePreview() {
    OneAppTheme {
        EmptyStateTemplate(EmptyStateData()) {}
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyStateTemplateNightPreview() {
    OneAppTheme {
        EmptyStateTemplate(EmptyStateData()) {}
    }
}