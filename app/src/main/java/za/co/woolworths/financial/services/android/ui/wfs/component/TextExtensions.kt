package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.theme.*


@Composable
fun TextOpenSansMediumH3(
    modifier: Modifier = Modifier,
    text: String?,
    isUpperCased: Boolean = false,
    color: Color? = WhiteWithOpacity30,
    fontSize: TextUnit = Dimens.twelve_sp,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = FontWeight.W500,
    fontFamily: FontFamily? = OpenSansFontFamily,
    letterSpacing: TextUnit = Dimens.one_sp,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = TextAlign.Center,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    locator : String? = null
) {
    val textUppercase = if (isUpperCased) text?.uppercase() ?: "" else text ?: ""
    Text(
        text = textUppercase,
        modifier = modifier.then(Modifier.testAutomationTag(locator ?: textUppercase)),
        color = color ?: Color.Black,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

@Composable
fun TextOpenSansFamilyBoldH1(
    modifier: Modifier = Modifier,
    text: String?,
    isUpperCased: Boolean = false,
    color: Color? = null,
    minLines : Int = 1,
    fontSize : TextUnit?,
    letterSpacing: TextUnit = Dimens.point_five_sp,
    locator: String,
    lineHeight: TextUnit? = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = if (isUpperCased) text?.uppercase() ?: "" else text ?: "",
        fontSize = fontSize ?: Dimens.twenty_sp,
        color = color ?: Color.Black,
        fontWeight = FontWeight.SemiBold,
        style = futuraFamilyHeader1(),
        lineHeight = lineHeight ?: TextUnit.Unspecified,
        textAlign = textAlign,
        letterSpacing = letterSpacing,
        modifier = modifier.then(Modifier.testAutomationTag(locator)),
        maxLines = maxLines)
}

@Composable
fun TextOpenSansSemiBoldH3(
    modifier: Modifier = Modifier,
    text: String?,
    fontSize: TextUnit = FontDimensions.sp12,
    isUpperCased: Boolean = false,
    color: Color? = null,
    letterSpacing: TextUnit? = TextUnit.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
    locator: String
) {
    val value = if (isUpperCased) text?.uppercase() ?: "" else text ?: ""
    Text(
        text = value,
        fontSize = fontSize,
        color = color ?: Obsidian,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.titleMedium,
        textAlign = textAlign,
        letterSpacing = letterSpacing ?: Dimens.point_five_sp,
        modifier = modifier.testAutomationTag(locator.ifEmpty { value })
    )
}

@Composable
fun TextOpenSansFontFamily(
    modifier: Modifier = Modifier,
    text: String,
    isUpperCased: Boolean = false,
    color: Color = WhiteWithOpacity70,
    fontSize: TextUnit = FontDimensions.sp15,
    fontStyle: FontStyle? = null,
    locator: String,
    fontWeight: FontWeight? = FontWeight.Normal,
    fontFamily: FontFamily? = OpenSansFontFamily,
    letterSpacing: TextUnit = Dimens.point_five_sp,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = TextAlign.Center,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    if (isUpperCased)
        text.uppercase()

    Text(
        text = text,
        modifier = modifier.testAutomationTag(locator.ifEmpty { text }),
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        onTextLayout = onTextLayout,
        style = style
    )
}


@Composable
fun TextFuturaFamilyHeader1(
    modifier: Modifier = Modifier,
    text: String,
    locator : String = "",
    isUpperCased: Boolean = false,
    textColor: Color? = Color.Black,
    fontSize: TextUnit? = FontDimensions.sp20,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = FontWeight.W600,
    fontFamily: FontFamily? = FuturaFontFamily,
    letterSpacing: TextUnit = Dimens.letterspacing_029_sp,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = TextAlign.Start,
    lineHeight: TextUnit = TextUnit.Unspecified,
    isLoading: Boolean = false,
    brush: Brush? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = futuraFamilyHeader1()
) {

    if (isLoading){
        HeaderItemShimmer(brush, locator)
    }

    if (!isLoading) {
        Text(
            text = if (isUpperCased) text.uppercase() else text,
            modifier = modifier.fillMaxWidth().then(Modifier.testAutomationTag(locator = locator)),
            color =  textColor ?: Color.Black,
            fontSize = fontSize ?: FontDimensions.sp18,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            onTextLayout = onTextLayout,
            style = style
        )
    }

}


@Composable
fun TextFuturaFamilySemiBoldHeader1(
    modifier: Modifier = Modifier,
    text: String,
    locator : String = "",
    isUpperCased: Boolean = false,
    textColor: Color? = Black,
    fontSize: TextUnit = FontDimensions.sp18,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = FontWeight.SemiBold,
    fontFamily: FontFamily? = FuturaFontFamily,
    letterSpacing: TextUnit = Dimens.letterspacing_029_sp,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = TextAlign.Start,
    lineHeight: TextUnit = TextUnit.Unspecified,
    isLoading: Boolean = false,
    brush: Brush? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = futuraFamilyHeader1()
) {

    if (isLoading){
        HeaderItemShimmer(brush, locator)
    }

    if (!isLoading) {
        Text(
            text = if (isUpperCased) text.uppercase() else text,
            modifier = modifier.then(Modifier.testAutomationTag(locator = locator)),
            color = textColor ?: Color.Black,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            onTextLayout = onTextLayout,
            style = style
        )
    }

}