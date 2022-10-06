package za.co.woolworths.financial.services.android.ui.views.voc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.compose.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.compose.NoRippleInteractionSource

@Preview
@Composable
fun SurveyFooterActionView(
    isSubmitEnabled: Boolean = false,
    onSubmitCallback: () -> Unit = {},
    onOptOutCallback: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(20.dp),
    ) {
        Button(
            onClick = { if (isSubmitEnabled) onSubmitCallback() },
            shape = RectangleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.button_style_height))
                .alpha(if (isSubmitEnabled) 1f else 0.5f),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Black,
                contentColor = Color.White
            ),
            elevation = null,
            interactionSource = NoRippleInteractionSource()
        ) {
            Text(
                text = stringResource(id = R.string.voc_survey_submit),
                fontSize = 12.sp,
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
        Button(
            onClick = onOptOutCallback,
            shape = RectangleShape,
            modifier = Modifier
                .padding(top = 14.dp)
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.button_style_height)),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent,
                contentColor = colorResource(R.color.gray)
            ),
            elevation = null,
            interactionSource = NoRippleInteractionSource()
        ) {
            Text(
                text = stringResource(id = R.string.voc_survey_opt_out),
                fontSize = 12.sp,
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.Medium,
                style = TextStyle(textDecoration = TextDecoration.Underline)
            )
        }
    }
}