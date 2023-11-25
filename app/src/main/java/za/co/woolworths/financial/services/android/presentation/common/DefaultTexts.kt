package za.co.woolworths.financial.services.android.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import za.co.woolworths.financial.services.android.ui.wfs.theme.Black
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@Composable
fun FuturaTextH1(
    modifier: Modifier = Modifier,
    text: String = "",
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 20.sp,
            color = Black,
            textAlign = textAlign
        )
    )
}

@Composable
fun FuturaTextH14(
    modifier: Modifier = Modifier,
    text: String = "",
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 14.sp,
            color = Black,
            textAlign = textAlign
        )
    )
}

@Composable
fun FuturaTextH15(
    modifier: Modifier = Modifier,
    text: String = "",
    textAlign: TextAlign = TextAlign.Center
) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 15.sp,
            color = Black,
            textAlign = textAlign
        )
    )
}

@Composable
fun OpenSansTitleText13(
    modifier: Modifier = Modifier,
    text: String = "",
    textAlign: TextAlign = TextAlign.Start,
    color: Color = Black,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 13.sp,
            color = color,
            textAlign = textAlign
        ),
        maxLines = maxLines,
        overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis
    )
}

@Composable
fun OpenSansText14(
    modifier: Modifier = Modifier,
    text: String = "",
    textAlign: TextAlign = TextAlign.Start,
    color: Color = Black,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 14.sp,
            color = color,
            textAlign = textAlign
        ),
        maxLines = maxLines,
        overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis
    )
}

@Composable
fun BlackRoundedCornerText(
    modifier: Modifier = Modifier,
    text: String = "",
    textAlign: TextAlign = TextAlign.Center,
    color: Color = Color.White,
    roundedCorner: Dp = 16.dp
) {
    Text(
        modifier = modifier
            .background(Black, RoundedCornerShape(roundedCorner))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        text = text,
        style = TextStyle(
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 9.sp,
            color = color,
            textAlign = textAlign
        )
    )
}


@Composable
fun BlackRoundedCornerIcon(
    modifier: Modifier = Modifier,
    icon: Int,
    text: String = "",
    textAlign: TextAlign = TextAlign.Center,
    textColor: Color = Color.White,
    roundedCorner: Dp = 12.dp,
    tintColor: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    Icon(
        modifier = modifier
            .size(32.dp, 24.dp)
            .background(Black, RoundedCornerShape(roundedCorner))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable {
                onClick()
            },
        painter = painterResource(id = icon),
        contentDescription = null,
        tint = tintColor
    )
}