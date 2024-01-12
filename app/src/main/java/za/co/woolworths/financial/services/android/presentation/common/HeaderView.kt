package za.co.woolworths.financial.services.android.presentation.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
            title = headerViewState.title
        ) {
            onHeaderEvent(HeaderViewEvent.IconClick)
        }

        is HeaderViewState.HeaderStateType2 -> HeaderType2(
            modifier = modifier,
            icon = headerViewState.icon,
            title = headerViewState.title
        ) {
            onHeaderEvent(HeaderViewEvent.IconClick)
        }

        is HeaderViewState.HeaderStateType3 -> HeaderType3(
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

        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.clickable {
                onIconClick()
            }
        )

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

@Composable
private fun HeaderType3(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = R.drawable.icon_close_16,
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