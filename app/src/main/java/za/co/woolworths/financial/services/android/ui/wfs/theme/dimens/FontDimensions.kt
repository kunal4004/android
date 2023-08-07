package za.co.woolworths.financial.services.android.ui.wfs.theme.dimens

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Stable
data class FontDimensions(
    val sp11: TextUnit = 11.sp,
    val sp12: TextUnit = 12.sp,
    val sp13: TextUnit = 13.sp,
    val sp14: TextUnit = 14.sp,
    val sp15: TextUnit = 15.sp,
    val sp16: TextUnit = 16.sp,
    val sp18: TextUnit = 18.sp,
    val sp20: TextUnit = 20.sp,
    val policyNumberValue15Sp : TextUnit = 15.sp
)

val smallFontDimensions by lazy { FontDimensions() }

val sw320FontDimensions by lazy {
    FontDimensions(
        sp16 = 15.sp)
}

val sw360FontDimensions by lazy {
    FontDimensions(
        sp15 = 14.sp,
        sp16 = 15.sp,
        policyNumberValue15Sp = 13.sp

    )
}

val sw400FontDimensions by lazy {
    FontDimensions()
}
val sw420FontDimensions by lazy {
    FontDimensions(
        policyNumberValue15Sp = 14.sp

    )
}

val sw440FontDimensions by lazy {
    FontDimensions(
        sp12 = 12.85.sp,
        sp13 = 14.10.sp,
        sp20 = 20.5.sp,
        sp16 = 18.sp)
}
val sw480FontDimensions by lazy { FontDimensions() }

val sw520FontDimensions by lazy { FontDimensions() }

val sw560FontDimensions by lazy { FontDimensions() }

val sw600FontDimensions by lazy { FontDimensions() }