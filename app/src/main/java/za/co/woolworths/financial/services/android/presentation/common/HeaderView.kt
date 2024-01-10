package za.co.woolworths.financial.services.android.presentation.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.HeaderGrey
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

@Composable
fun HeaderView(
    modifier: Modifier = Modifier,
    headerViewState: HeaderViewState = HeaderViewState.HeaderStateType1(),
    onHeaderEvent: (event: HeaderViewEvent) -> Unit
) {

    when (headerViewState) {
        is HeaderViewState.HeaderStateType1 -> HeaderType1(
            modifier = modifier,
            icon = headerViewState.icon,
            title = stringResource(id = headerViewState.titleRes),
            rightButton = stringResource(id = headerViewState.rightButtonRes),
            onIconClick = {
                onHeaderEvent(HeaderViewEvent.IconClick)
            },
            onRightButtonClick = {
                onHeaderEvent(HeaderViewEvent.RightButtonClick)
            }
        )

        is HeaderViewState.HeaderStateType2 -> HeaderType2(
            modifier = modifier,
            icon = headerViewState.icon,
            title = headerViewState.title
        ) {
            onHeaderEvent(HeaderViewEvent.IconClick)
        }
    }
}

@Composable
private fun HeaderType1(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = R.drawable.back24,
    title: String = "",
    rightButton: String = "",
    onIconClick: () -> Unit,
    onRightButtonClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .then(modifier)
    ) {

        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable {
                    onIconClick()
                }
        )

        Text(
            text = title.uppercase(),
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
                color = HeaderGrey
            ),
            letterSpacing = 1.5.sp
        )

        if (rightButton.isNotEmpty())
            ToolbarButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                alignment = TextAlign.End,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.W500,
                text = rightButton
            ) {
                onRightButtonClick()
            }
    }
}

@Composable
private fun HeaderType2(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = R.drawable.add_black,
    title: String = "",
    onIconClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title.uppercase(),
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
                color = HeaderGrey
            ),
            letterSpacing = 1.5.sp
        )

        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.clickable {
                onIconClick()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderViewPreview() {
    OneAppTheme {
        HeaderView() {}
    }
}