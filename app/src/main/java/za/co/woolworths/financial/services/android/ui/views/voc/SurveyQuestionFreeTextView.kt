package za.co.woolworths.financial.services.android.ui.views.voc

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.view_survey_question_free_text.view.*
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyAnswer
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.ui.compose.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.compose.MyriadProFontFamily

@Preview
@Composable
fun SurveyQuestionFreeTextView(
    title: String = "Lorem ipsum sit dolor",
    initialText: String = "",
    @StringRes placeholder: Int = R.string.voc_question_freetext_hint_optional,
    onTextChanged: (String) -> Unit = {}
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        var textState = rememberSaveable { mutableStateOf(initialText) }
        Text(
            text = title,
            fontSize = 20.sp,
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp,
            color = Color.Black
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 19.dp
                )
                .height(134.dp),
            shape = RectangleShape,
            value = textState.value,
            onValueChange = {
                textState.value = it
                onTextChanged(it)
            },
            singleLine = false,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                cursorColor = Color.Black,
                focusedBorderColor = colorResource(id = R.color.color_separator_light_grey),
                unfocusedBorderColor = colorResource(id = R.color.color_separator_light_grey)
            ),
            placeholder = {
                Text(
                    text = stringResource(id = placeholder),
                    color = colorResource(id = R.color.unavailable),
                    fontSize = 15.sp,
                    fontFamily = MyriadProFontFamily,
                    fontWeight = FontWeight.Normal
                )
            },
            textStyle = LocalTextStyle.current.copy(
                color = Color.Black,
                fontSize = 15.sp,
                fontFamily = MyriadProFontFamily,
                fontWeight = FontWeight.Normal
            ),
        )
    }
}