package za.co.woolworths.financial.services.android.ui.wfs.theme.dimens.core

import androidx.compose.runtime.Composable
import za.co.woolworths.financial.services.android.ui.wfs.theme.*
import za.co.woolworths.financial.services.android.ui.wfs.theme.dimens.*

enum class DensityDpiDimension(private val dpi: Int) {
    DPI320(320),
    DPI360(360),
    DPI400(400),
    DPI440(440),
    DPI480(480),
    DPI520(520),
    DPI560(560),
    DPI600(600);
    companion object {
        @Composable
        fun getDimension(screenWidthDp: Int): Dimensions {
            return when {
                screenWidthDp <= DPI320.dpi -> sw320Dimensions
                screenWidthDp <= DPI360.dpi -> sw360Dimensions
                screenWidthDp <= DPI400.dpi -> sw400Dimensions
                screenWidthDp <= DPI440.dpi -> sw440Dimensions
                screenWidthDp <= DPI480.dpi -> sw480Dimensions
                screenWidthDp <= DPI520.dpi -> sw360Dimensions
                screenWidthDp <= DPI560.dpi -> sw360Dimensions
                screenWidthDp <= DPI600.dpi -> sw360Dimensions
                else -> sw320Dimensions
            }
        }

        @Composable
        fun getMarginDimens(screenWidthDp: Int): MarginDimensions {
            return when {
                screenWidthDp <= DPI320.dpi -> sw320MarginDimensions
                screenWidthDp <= DPI360.dpi -> sw360MarginDimensions
                screenWidthDp <= DPI400.dpi -> sw400MarginDimensions
                screenWidthDp <= DPI440.dpi -> sw440MarginDimensions
                screenWidthDp <= DPI480.dpi -> sw480MarginDimensions
                screenWidthDp <= DPI520.dpi -> sw520MarginDimensions
                screenWidthDp <= DPI560.dpi -> sw560MarginDimensions
                screenWidthDp <= DPI600.dpi -> sw600MarginDimensions
                else -> sw320MarginDimensions
            }
        }

        @Composable
        fun getFontDimension(screenWidthDp: Int): FontDimensions {
            val dimens = when {
                screenWidthDp <= DPI320.dpi -> sw320FontDimensions
                screenWidthDp <= DPI360.dpi -> sw360FontDimensions
                screenWidthDp <= DPI400.dpi -> sw400FontDimensions
                screenWidthDp <= DPI440.dpi -> sw440FontDimensions
                screenWidthDp <= DPI480.dpi -> sw480FontDimensions
                screenWidthDp <= DPI520.dpi -> sw520FontDimensions
                screenWidthDp <= DPI560.dpi -> sw560FontDimensions
                screenWidthDp <= DPI600.dpi -> sw600FontDimensions
                else -> smallFontDimensions
            }
            return dimens
        }

        @Composable
        fun getFloatDimension(screenWidthDp: Int): FloatDimensions {
            return when {
                screenWidthDp <= DPI320.dpi -> sw320FloatDimensions
                screenWidthDp <= DPI360.dpi -> sw360FloatDimensions
                screenWidthDp <= DPI400.dpi -> sw400FloatDimensions
                screenWidthDp <= DPI440.dpi -> sw440FloatDimensions
                screenWidthDp <= DPI480.dpi -> sw480FloatDimensions
                screenWidthDp <= DPI520.dpi -> sw520FloatDimensions
                screenWidthDp <= DPI560.dpi -> sw560FloatDimensions
                screenWidthDp <= DPI600.dpi -> sw600FloatDimensions
                else -> smallFloatDimensions
            }
        }

        @Composable
        fun getLetterSpacingDimension(screenWidthDp: Int): LetterSpacingDimensions {
            return when {
                screenWidthDp <= DPI320.dpi -> sw320LetterSpacingDimensions
                screenWidthDp <= DPI360.dpi -> sw360LetterSpacingDimensions
                screenWidthDp <= DPI400.dpi -> sw400LetterSpacingDimensions
                screenWidthDp <= DPI440.dpi -> sw440LetterSpacingDimensions
                screenWidthDp <= DPI480.dpi -> sw480LetterSpacingDimensions
                screenWidthDp <= DPI520.dpi -> sw520LetterSpacingDimensions
                screenWidthDp <= DPI560.dpi -> sw560LetterSpacingDimensions
                screenWidthDp <= DPI600.dpi -> sw600LetterSpacingDimensions
                else -> sw320LetterSpacingDimensions
            }
        }
    }

}