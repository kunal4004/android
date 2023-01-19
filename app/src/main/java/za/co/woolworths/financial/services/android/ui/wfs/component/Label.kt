package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.MyriadProFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.TitleSmall
import za.co.woolworths.financial.services.android.util.Utils

@Preview(showBackground = true)
@Composable
fun FontsPreview(){
    OneAppTheme {
        Column {
            LabelTitleLarge(LabelProperties(stringId = R.string.contact_us_financial_services))
            LabelTitleLarge(LabelProperties(label = "Financial Services"))
            LabelMedium(LabelProperties(stringId = R.string.contact_us_financial_services))
            LabelMedium(LabelProperties(label = "Financial Services"))
            LabelSmall(LabelProperties(stringId = R.string.contact_us_financial_services))
            LabelSmall(LabelProperties(label ="Financial Services"))
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
            modifier = params.modifier.testTag(label),
            letterSpacing = params.letterSpacing ?: 0.sp,
            textAlign = params.textAlign,
            style = MaterialTheme.typography.titleLarge,
            fontSize = params.fontSize ?: 18.sp
        )
    }
}

    @Composable
    fun LabelTitleCustomStyleLarge(params: LabelProperties) {
        var label = params.label ?: params.stringId?.let { stringResource(id = it) }
        if (params.isUpperCased){
            label = label?.uppercase()
        }
        label?.let {
            Text(
                color = params.textColor ?: Color.Black,
                text = it,
                modifier = params.modifier.testTag(label),
                letterSpacing = params.letterSpacing ?: 0.sp,
                textAlign = params.textAlign,
                style =  params.style,
                fontSize = params.fontSize ?: 18.sp,

            )
        }
}

@Composable
fun LabelMedium(params: LabelProperties = LabelProperties()) {
    var label = params.label ?: params.stringId?.let { stringResource(id = it) }
    if (params.isUpperCased){
        label = label?.uppercase()
    }
    label?.let {
        Text(
            color = params.textColor ?: Color.Black,
            text = it,
            modifier = params.modifier.testTag(label),
            textDecoration = params.textDecoration,
            letterSpacing = params.letterSpacing ?: 0.sp,
            textAlign = params.textAlign,
            style = MaterialTheme.typography.titleMedium,
            fontSize = params.fontSize ?: 16.sp,
        )
    }
}

@Composable
fun LabelSmall(params: LabelProperties = LabelProperties()) {
    var label = params.label ?: params.stringId?.let { stringResource(id = it) }
    if (params.isUpperCased){
        label = label?.uppercase()
    }
    label?.let {
        Text(
            text = it,
            textAlign = params.textAlign,
            textDecoration = params.textDecoration,
            style = MaterialTheme.typography.titleSmall,
            fontSize = params.fontSize ?: 14.sp,
            modifier = params.modifier.fillMaxWidth().testTag(label),
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
    if (phoneNumber.isEmpty() || phoneNumber.length < 10){
        Text(
            text = label.toString(),
          modifier = params.modifier,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    }else {
        ClickableText(
            text = label,
            style = params.style,
            modifier = params.modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp),
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
    val testTag : String? = null,
    val isUpperCased : Boolean = false,
    val annotatedString: AnnotatedString? = null,
    val annotatedPhoneNumber : String? = null,
    val textDecoration : TextDecoration = TextDecoration.None,
    val style: TextStyle = TextStyle(
        fontFamily = MyriadProFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = TitleSmall,
        lineHeight = 19.sp,
        textAlign = TextAlign.Center
    )
)


