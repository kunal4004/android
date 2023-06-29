package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.theme.*
import za.co.woolworths.financial.services.android.util.Utils

@Preview(showBackground = true)
@Composable
fun FontsPreview() {
    OneAppTheme {
        Column {
            val label = stringResource(id = R.string.app_name)
            LabelTitleLarge(LabelProperties(stringId = R.string.contact_us_financial_services))
            LabelTitleLarge(LabelProperties(label = label))
            LabelMedium(LabelProperties(stringId = R.string.contact_us_financial_services))
            LabelMedium(LabelProperties(label = label))
            LabelSmall(LabelProperties(stringId = R.string.contact_us_financial_services))
            LabelSmall(LabelProperties(label = label))
            TextFuturaFamilyHeader1(text = label, style = futuraFamilyHeader1())
            LabelLightText(LabelProperties(label = label, style = futuraFamilyHeader3()))
        }
    }
}

@Composable
fun LabelTitleLarge(params: LabelProperties) {
    var label = params.label ?: params.stringId?.let { stringResource(id = it) }
    if (params.isUpperCased) {
        label = label?.uppercase()
    }
    label?.let {
        Text(
            color = params.textColor ?: Color.Black,
            text = it,
            modifier = params.modifier.testAutomationTag(label),
            letterSpacing = params.letterSpacing ?: 0.sp,
            lineHeight = params.lineSpacingExtra,
            textAlign = params.textAlign,
            style = MaterialTheme.typography.titleLarge,
            fontSize = params.fontSize ?: FontDimensions.sp18,
            maxLines = 4
        )
    }
}

@Composable
fun LabelTitleCustomStyleLarge(params: LabelProperties) {
    var label = params.label ?: params.stringId?.let { stringResource(id = it) }
    if (params.isUpperCased) {
        label = label?.uppercase()
    }
    label?.let {
        Text(
            color = params.textColor ?: Color.Black,
            text = it,
            modifier = params.modifier.testAutomationTag(label),
            letterSpacing = params.letterSpacing ?: 0.sp,
            textAlign = params.textAlign,
            style = params.style,
            fontSize = params.fontSize ?: FontDimensions.sp18)
    }
}

@Composable
fun LabelMedium(params: LabelProperties = LabelProperties()) {
    var label = params.label ?: params.stringId?.let { stringResource(id = it) }
    if (params.isUpperCased) {
        label = label?.uppercase()
    }
    label?.let {
        Text(
            color = params.textColor ?: Color.Black,
            text = it,
            modifier = params.modifier.testAutomationTag(label),
            textDecoration = params.textDecoration,
            letterSpacing = params.letterSpacing ?: 0.sp,
            textAlign = params.textAlign,
            style = MaterialTheme.typography.titleMedium,
            fontSize = params.fontSize ?: FontDimensions.sp16,
        )
    }
}

@Composable
fun LabelSmall(params: LabelProperties = LabelProperties()) {
    var label = params.label ?: params.stringId?.let { stringResource(id = it) }
    if (params.isUpperCased) {
        label = label?.uppercase()
    }
    label?.let {
        Text(
            text = it,
            textAlign = params.textAlign,
            textDecoration = params.textDecoration,
            style = MaterialTheme.typography.titleSmall,
            fontSize = params.fontSize ?: FontDimensions.sp14,
            modifier = params.modifier
                .fillMaxWidth()
                .testAutomationTag(label),
            letterSpacing = params.letterSpacing ?: 0.sp
        )
    }
}

@Composable
fun LabelPhoneNumber(params: LabelProperties = LabelProperties()) {
    params.annotatedPhoneNumber ?: return
    params.annotatedString ?: return

    val label = params.annotatedString
    val phoneNumber = params.annotatedPhoneNumber
    if (phoneNumber.isEmpty() || phoneNumber.length < 10) {
        Text(
            text = label.toString(),
            modifier = params.modifier,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    } else {
        ClickableText(
            text = label,
            style = params.style,
            modifier = params.modifier
                .fillMaxWidth()
                .padding(start = Margin.start, end = Margin.end),
            onClick = {
                label
                    .getStringAnnotations(phoneNumber, it, it)
                    .firstOrNull()?.let {
                        Utils.makeCall(phoneNumber)
                    }
            }
        )
    }
}

data class LabelProperties(
    val textAlign: TextAlign = TextAlign.Start,
    val modifier: Modifier = Modifier,
    val label: String? = null,
    val stringId: Int? = null,
    val fontSize: TextUnit? = null,
    val letterSpacing: TextUnit? = null,
    val textColor: Color? = null,
    val locator: String? = null,
    val isUpperCased: Boolean = false,
    val annotatedString: AnnotatedString? = null,
    val weight: Int? = null,
    val annotatedPhoneNumber: String? = null,
    val lineSpacingExtra: TextUnit = 1.sp,
    val textDecoration: TextDecoration = TextDecoration.None,
    val style: TextStyle = TextStyle(
        fontFamily = OpenSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = TitleSmall,
        lineHeight = 19.sp,
        textAlign = TextAlign.Center
    )
)

@Composable
fun SectionHeaderTitleLabel(params: LabelProperties) {
    LabelTitleLarge(
        params = LabelProperties(
            label = params.label,
            stringId = params.stringId,
            fontSize = params.fontSize,
            isUpperCased = params.isUpperCased,
            style = params.style,
            lineSpacingExtra = params.lineSpacingExtra,
            letterSpacing = params.letterSpacing,
            modifier = params.modifier,
            textColor = params.textColor,
            textAlign = params.textAlign
        )
    )
}

@Composable
fun HeaderItemShimmer(brush: Brush?, locator: String) {
    Row(modifier = Modifier
        .padding(start = 27.dp, top = 24.dp)
        .testAutomationTag(locator)) {
        brush?.let { ShimmerLabel(brush = it, width = 0.45f, height = 12.dp) }
    }
}


@Composable
fun LabelLightText(params: LabelProperties = LabelProperties()) {
    var label = params.label ?: params.stringId?.let { stringResource(id = it) }
    if (params.isUpperCased) {
        label = label?.uppercase()
    }
    label?.let {
        Text(
            color = params.textColor ?: Color.Black,
            text = it,
            modifier = params.modifier.testAutomationTag(label),
            letterSpacing = params.letterSpacing ?: 1.sp,
            textAlign = params.textAlign,
            style = futuraFamilyHeader3(),
            fontSize = params.fontSize ?: 12.sp
        )
    }
}

@Composable
fun ButtonLabel(
    modifier: Modifier = Modifier.padding(
        start = Dimens.ten_dp,
        end = Dimens.ten_dp,
        top = Dimens.six_dp,
        bottom = Dimens.four_dp
    ),
    text: String,
    color: Color = White,
    fontSize: TextUnit = Dimens.twelve_sp,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = FontWeight.SemiBold,
    fontFamily: FontFamily? = FuturaFontFamily,
    letterSpacing: TextUnit = Dimens.one_sp,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = TextAlign.Center,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    Text(
        text = text,
        modifier = modifier,
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
fun TextWFuturaMedium(
    text: String,
    modifier: Modifier = Modifier,
    isUpperCased: Boolean = false,
    minLines : Int = 1,
    color: Color = White,
    fontSize: TextUnit = Dimens.fourteen_sp,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = FontWeight.Medium,
    fontFamily: FontFamily? = FuturaFontFamily,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    locator: String = text,
    textAlign: TextAlign? = TextAlign.Center,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current) {
    Text(
        text = text,
        modifier = modifier.then(Modifier
            .testAutomationTag(locator = locator)),
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
        style = style)}
