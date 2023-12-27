package za.co.woolworths.financial.services.android.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.Black
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD0021B
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorFCF0F1
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
fun FuturaTextH10(
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
            fontSize = 10.sp,
            color = Black,
            textAlign = textAlign
        )
    )
}

@Composable
fun FuturaTextH8(
    modifier: Modifier = Modifier,
    text: String = "",
    textAlign: TextAlign = TextAlign.Start,
    color: Color = Black
) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 8.sp,
            color = color,
            textAlign = textAlign,
            letterSpacing = 0.8.sp
        )
    )
}

@Composable
fun FuturaTextH12(
    modifier: Modifier = Modifier,
    text: String = "",
    textAlign: TextAlign = TextAlign.Start,
    color: Color = Black,
    fontWeight: FontWeight = FontWeight.W500
) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontFamily = FuturaFontFamily,
            fontWeight = fontWeight,
            fontSize = 12.sp,
            color = color,
            textAlign = textAlign,
            letterSpacing = 1.sp
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
fun OpenSansTitleText10(
    modifier: Modifier = Modifier,
    text: String = "",
    textAlign: TextAlign = TextAlign.Start,
    color: Color = Black,
    maxLines: Int = Int.MAX_VALUE,
    textDecoration: TextDecoration? = null
) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 10.sp,
            color = color,
            textAlign = textAlign
        ),
        maxLines = maxLines,
        overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
        textDecoration = textDecoration
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
fun ToolbarButton(
    modifier: Modifier = Modifier,
    alignment: TextAlign = TextAlign.Start,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    text: String = "",
    onClick: () -> Unit
) {
    Text(
        modifier = modifier
            .clickable {
                onClick()
            },
        text = text,
        style = TextStyle(
            color = Black,
            fontFamily = FuturaFontFamily,
            fontWeight = fontWeight,
            fontSize = 12.sp,
            textAlign = alignment,
            letterSpacing = letterSpacing
        )
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


@Composable
fun PromotionalText(
    modifier: Modifier = Modifier,
    text: String = "",
    textDecoration: TextDecoration? = null,
    contentDesc: String = ""
) {
    val annotationString = buildAnnotatedString {
        val spanStyle = SpanStyle(
            fontWeight = FontWeight.W600,
            textDecoration = null
        )
        val promotionalSpanStyle = SpanStyle(
            fontWeight = FontWeight.W500,
            textDecoration = textDecoration
        )
        withStyle(spanStyle) {
            append(stringResource(id = R.string.offer).uppercase())
            append(stringResource(id = R.string.colon))
            append(" ")
        }
        withStyle(promotionalSpanStyle) {
            append(text)
        }
    }
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(ColorFCF0F1)
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .semantics {
                       contentDescription = contentDesc
            },
        text = annotationString,
        style = TextStyle(
            fontFamily = FuturaFontFamily,
            color = ColorD0021B,
            fontSize = 11.sp,
        )
    )
}

