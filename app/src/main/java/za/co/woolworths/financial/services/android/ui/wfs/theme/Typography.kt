package za.co.woolworths.financial.services.android.ui.wfs.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R

/**
 * Material Design 3 Typography
 * More info: https://developer.android.com/jetpack/compose/themes/material3#typography
 */

val FuturaFontFamily = FontFamily(
    Font(resId = R.font.futura_light, FontWeight.Normal),
    Font(resId = R.font.futura_medium, FontWeight.Medium),
    Font(R.font.futura_semi_bold, FontWeight.SemiBold),
    Font(R.font.futura_bold, FontWeight.Bold),
    Font(R.font.futura_bold, FontWeight.W600),
    Font(R.font.futura_light, FontWeight.W500)
)

val OpenSansFontFamily = FontFamily(
    Font(R.font.opensans_regular, FontWeight.W400),
    Font(R.font.opensans_regular, FontWeight.W500),
    Font(R.font.opensans_light, FontWeight.Light),
    Font(R.font.opensans_medium, FontWeight.Medium),
    Font(R.font.opensans_bold, FontWeight.Bold),
    Font(R.font.opensans_semi_bold, FontWeight.SemiBold),
    Font(R.font.opensans_extra_bold, FontWeight.ExtraBold),
)

// Set of Material typography styles to start with
val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = FuturaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = HeaderGrey
    ),
    titleMedium = TextStyle(
        fontFamily = OpenSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = Obsidian,
        lineHeight = 20.sp

    ),
    titleSmall = TextStyle(
        fontFamily = OpenSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TitleSmall,
        lineHeight = 19.sp
    )
)

@Composable
fun futuraFamilyHeader1(): TextStyle {
    return TextStyle(
        fontFamily = FuturaFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = FontDimensions.sp20,
        color = Black,
        letterSpacing = (-0.15).sp
    )
}

@Composable
fun futuraFamilyHeader3(): TextStyle {
    return TextStyle(
        fontFamily = FuturaFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = FontDimensions.sp12,
        color = Black,
        lineHeight = 12.sp,
        letterSpacing = 1.sp
    )
}