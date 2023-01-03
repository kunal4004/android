package za.co.woolworths.financial.services.android.ui.wfs.theme

import androidx.compose.material3.Typography
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
    Font(R.font.futura_bold, FontWeight.Bold)
)

val MyriadProFontFamily = FontFamily(
    Font(R.font.myriad_pro_regular, FontWeight.Normal),
    Font(R.font.myriad_pro_semi_bold, FontWeight.SemiBold)
)

val MuseoSlabFamily = FontFamily(
    Font(R.font.museo_slab_700, FontWeight.Normal)
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
        fontFamily = MyriadProFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = Obsidian,
        lineHeight = 20.sp

    ),
    titleSmall = TextStyle(
        fontFamily = MyriadProFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TitleSmall,
        lineHeight = 19.sp
    )
)