package za.co.woolworths.financial.services.android.ui.views.voc

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
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
import za.co.woolworths.financial.services.android.ui.compose.NoRippleInteractionSource
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily

@Preview
@Composable
fun SurveyFooterActionView(
    context: Context? = null,
    isSubmitEnabled: Boolean = false,
    onSubmitCallback: () -> Unit = {},
    onOptOutCallback: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .testTag(context?.getString(R.string.voc_footer_action) ?: "")
            .background(Color.White)
            .padding(20.dp),
    ) {
        Button(
            onClick = { if (isSubmitEnabled) onSubmitCallback() },
            shape = RectangleShape,
            modifier = Modifier
                .testTag(context?.getString(R.string.voc_action_submit) ?: "")
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.button_style_height))
                .alpha(if (isSubmitEnabled) 1f else 0.5f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            elevation = null,
            interactionSource = NoRippleInteractionSource()
        ) {
            Text(
                modifier = Modifier
                    .testTag(context?.getString(R.string.voc_action_submit_label) ?: ""),
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
                .testTag(context?.getString(R.string.voc_action_optout) ?: "")
                .padding(top = 14.dp)
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.button_style_height)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = colorResource(R.color.gray)
            ),
            elevation = null,
            interactionSource = NoRippleInteractionSource()
        ) {
            Text(
                modifier = Modifier
                    .testTag(context?.getString(R.string.voc_action_optout_label) ?: ""),
                text = stringResource(id = R.string.voc_survey_opt_out),
                fontSize = 12.sp,
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.Medium,
                style = TextStyle(textDecoration = TextDecoration.Underline)
            )
        }
    }
}