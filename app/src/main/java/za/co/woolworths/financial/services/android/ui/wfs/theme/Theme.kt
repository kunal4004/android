package za.co.woolworths.financial.services.android.ui.wfs.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Black,
    secondary = White,
    tertiary = LightGrey
)

private val LightColorScheme = lightColorScheme(
    primary = White,
    secondary = Black,
    tertiary = DarkGrey
)

@Composable
fun OneAppTheme(
    darkTheme: Boolean = false, // isSystemInDarkTheme()
    dynamicColor: Boolean = true, // Dynamic color is available on Android 12+
    content: @Composable () -> Unit) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        /* getting the current window by tapping into the Activity */
        val currentWindow = (view.context as? Activity)?.window
        SideEffect {
            currentWindow ?: return@SideEffect
            currentWindow.statusBarColor = White.toArgb()
            // accessing the insets controller to change appearance of the status bar
            WindowCompat.getInsetsController(currentWindow, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}