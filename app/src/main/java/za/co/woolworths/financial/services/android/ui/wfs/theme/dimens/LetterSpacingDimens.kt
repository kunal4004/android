package za.co.woolworths.financial.services.android.ui.wfs.theme.dimens

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Stable
data class LetterSpacingDimensions(
    val ls01: TextUnit = 0.1.sp,
    val ls02: TextUnit = 0.2.sp,
    val ls03: TextUnit = 0.3.sp,
    val ls04: TextUnit = 0.4.sp,
    val ls05: TextUnit = 0.5.sp,
    val ls06: TextUnit = 0.6.sp,
    val ls07: TextUnit = 0.7.sp,
    val ls08: TextUnit = 0.8.sp,
    val ls09: TextUnit = 0.9.sp,
    val ls10: TextUnit = 1.0.sp,
    val ls12: TextUnit = 1.2.sp

)

val sw320LetterSpacingDimensions by lazy { LetterSpacingDimensions() }

val sw360LetterSpacingDimensions by lazy { LetterSpacingDimensions() }

val sw400LetterSpacingDimensions by lazy { LetterSpacingDimensions() }

val sw440LetterSpacingDimensions by lazy { LetterSpacingDimensions() }

val sw480LetterSpacingDimensions by lazy { LetterSpacingDimensions() }

val sw520LetterSpacingDimensions by lazy { LetterSpacingDimensions() }

val sw560LetterSpacingDimensions by lazy { LetterSpacingDimensions() }

val sw600LetterSpacingDimensions by lazy { LetterSpacingDimensions() }