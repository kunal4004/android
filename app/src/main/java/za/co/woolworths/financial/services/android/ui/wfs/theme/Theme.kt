package za.co.woolworths.financial.services.android.ui.wfs.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import za.co.woolworths.financial.services.android.ui.wfs.theme.dimens.*
import za.co.woolworths.financial.services.android.ui.wfs.theme.dimens.core.DensityDpiDimension
import za.co.woolworths.financial.services.android.ui.wfs.theme.extension.OneAppRippleTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OneAppTheme(
        isDarkThemeEnabled: Boolean = false, // isSystemInDarkTheme()
        isDynamicColorEnabled: Boolean = true, // Dynamic color is available on Android 12+
        content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val dimensionSet = dimensionSet(screenWidthDp)
    val colorScheme = getColorScheme(isDarkThemeEnabled, isDynamicColorEnabled)
    SystemUiController(isDarkThemeEnabled = isDarkThemeEnabled)
    CompositionLocalProvider(
        LocalAppDimensions provides dimensionSet,
        LocalRippleTheme provides OneAppRippleTheme(),
        LocalOverscrollConfiguration provides null // remove overscroll ripple effect
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
            )
    }
}

// Accessing the insets controller to change appearance of the status bar
@Composable
private fun SystemUiController(view: View? = LocalView.current, isDarkThemeEnabled: Boolean) {
    if (view?.isInEditMode?.not() == true) {
        val currentWindow = (LocalView.current.context as? Activity)?.window
        LaunchedEffect(Unit) {
            currentWindow ?: return@LaunchedEffect
            currentWindow.statusBarColor = White.toArgb()
            WindowCompat.getInsetsController(currentWindow, view).isAppearanceLightStatusBars = isDarkThemeEnabled }
    }
}

@Stable
data class AppDimensions(
    val dimens: Dimensions = sw320Dimensions,
    val floats: FloatDimensions = sw320FloatDimensions,
    val shimmer: ShimmerDimensions = sw320ShimmerDimensions,
    val margin: MarginDimensions = sw320MarginDimensions,
    val font: FontDimensions = sw320FontDimensions,
    val letterSpacing : LetterSpacingDimensions  = sw320LetterSpacingDimensions
)

val LocalAppDimensions = staticCompositionLocalOf { AppDimensions() }

@Composable
private fun dimensionSet(screenWidthDp: Int): AppDimensions {
    val context: Context = LocalContext.current
    LaunchedEffect(true){
        Toast.makeText(context,"width dpi : $screenWidthDp", Toast.LENGTH_LONG ).show()
    }
    return AppDimensions().copy(
        dimens = DensityDpiDimension.getDimension(screenWidthDp = screenWidthDp),
        floats = DensityDpiDimension.getFloatDimension(screenWidthDp = screenWidthDp),
        margin = DensityDpiDimension.getMarginDimens(screenWidthDp = screenWidthDp),
        font = DensityDpiDimension.getFontDimension(screenWidthDp = screenWidthDp),
        letterSpacing = DensityDpiDimension.getLetterSpacingDimension(screenWidthDp = screenWidthDp)
    )
}

val DarkColorScheme by lazy {
    darkColorScheme(
        primary = Black,
        secondary = White,
        tertiary = LightGrey
    )
}

val LightColorScheme by lazy {
    lightColorScheme(
        primary = White,
        secondary = Black,
        tertiary = DarkGrey
    )
}

@Composable
fun getColorScheme(isDarkThemeEnabled: Boolean, isDynamicColorEnabled: Boolean): ColorScheme {
    val context = LocalContext.current
    val sdkVersion = context.applicationInfo?.targetSdkVersion ?: 0
    val colorScheme = when {
        sdkVersion >= Build.VERSION_CODES.S && isDynamicColorEnabled -> {
            LightColorScheme
        }
        isDarkThemeEnabled -> DarkColorScheme
        else -> LightColorScheme
    }
    return colorScheme
}


object OneAppTheme {
    val dimens: Dimensions
        @Composable
        get() = LocalAppDimensions.current.dimens

    val floatDimensions: FloatDimensions
        @Composable
        get() = LocalAppDimensions.current.floats

    val shimmerDimensions: ShimmerDimensions
        @Composable
        get() = LocalAppDimensions.current.shimmer

    val marginDimensions: MarginDimensions
        @Composable
        get() = LocalAppDimensions.current.margin
    val fontDimensions: FontDimensions
        @Composable
        get() = LocalAppDimensions.current.font

    val letterSpacingDimensions: LetterSpacingDimensions
        @Composable
        get() = LocalAppDimensions.current.letterSpacing
}

val Dimens: Dimensions
    @Composable
    get() = OneAppTheme.dimens

val Shimmer: ShimmerDimensions
    @Composable
    get() = OneAppTheme.shimmerDimensions

val Margin: MarginDimensions
    @Composable
    get() = OneAppTheme.marginDimensions

val FloatDimensions: FloatDimensions
    @Composable
    get() = OneAppTheme.floatDimensions

val FontDimensions : FontDimensions
    @Composable
    get() = OneAppTheme.fontDimensions

val LetterSpacing : LetterSpacingDimensions
    @Composable
    get() = OneAppTheme.letterSpacingDimensions