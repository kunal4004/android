package za.co.woolworths.financial.services.android.ui.views.voc

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@Preview
@Composable
fun SurveyQuestionFreeTextView(
    context: Context? = null,
    title: String? = "Lorem ipsum sit dolor",
    initialText: String? = "",
    @StringRes placeholder: Int = R.string.voc_question_freetext_hint_optional,
    onTextChanged: (String) -> Unit = {}
) {
    Column (
        modifier = Modifier
            .testAutomationTag(context?.getString(R.string.voc_question_freetext) ?: "")
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        val textState = rememberSaveable { mutableStateOf(initialText ?: "") }
        Text(
            modifier = Modifier.testAutomationTag(context?.getString(R.string.voc_question_freetext_title) ?: ""),
            text = title ?: "",
            fontSize = 20.sp,
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp,
            color = Color.Black
        )
        BasicTextField(
            modifier = Modifier
                .testAutomationTag(context?.getString(R.string.voc_question_freetext_input) ?: "")
                .fillMaxWidth()
                .padding(
                    top = 19.dp
                )
                .height(134.dp)
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.color_separator_light_grey),
                    shape = RectangleShape
                )
                .padding(
                    start = 13.dp,
                    top = 12.dp,
                    end = 14.dp,
                    bottom = 12.dp
                ),
            value = textState.value,
            onValueChange = {
                textState.value = it
                onTextChanged(it)
            },
            singleLine = false,
            textStyle = LocalTextStyle.current.copy(
                color = Color.Black,
                fontSize = 13.sp,
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.Normal
            ),
            decorationBox = { innerTextField ->
                if (textState.value.isEmpty()) {
                    Text(
                        text = stringResource(id = placeholder),
                        color = colorResource(id = R.color.unavailable),
                        fontSize = 13.sp,
                        fontFamily = OpenSansFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }
                innerTextField()
            }
        )
    }
}