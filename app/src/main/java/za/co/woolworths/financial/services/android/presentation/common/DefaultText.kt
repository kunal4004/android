package za.co.woolworths.financial.services.android.presentation.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily
import java.util.Locale

@Composable
fun TitleText(
    modifier: Modifier = Modifier,
    text: String = ""
) {
    Text(
        modifier = modifier,
        text = text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
        style = TextStyle(
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 20.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    )
}

@Composable
fun DescriptionText(
    modifier: Modifier = Modifier,
    text: String = ""
) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 13.sp,
            color = colorResource(id = R.color.color_444444),
            textAlign = TextAlign.Center
        )
    )
}

@Composable
fun CheckboxTitleText(
    modifier: Modifier = Modifier,
    text: String = ""
) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = Color.Black
        )
    )
}