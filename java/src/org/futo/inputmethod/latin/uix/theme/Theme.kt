package org.futo.inputmethod.latin.uix.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import androidx.annotation.ColorInt
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import org.futo.inputmethod.latin.uix.THEME_KEY
import org.futo.inputmethod.latin.uix.settings.useDataStoreValue
import org.futo.inputmethod.latin.uix.theme.presets.VoiceInputTheme
import androidx.compose.ui.graphics.Color
import kotlin.math.sqrt

val NothingRed = Color(0xFFE5252A)
val NothingBlack = Color(0xFF0B0B0C)
val NothingCardSurface = Color(0xFF141416)
val NothingSurfaceVariant = Color(0xFF1B1B1F)
val NothingBorder = Color(0xFF232326)
val NothingWhite = Color(0xFFFFFFFF)
val NothingTextSecondary = Color(0xFF8E8E93)

val DarkColorScheme = darkColorScheme(
    primary = NothingRed,
    onPrimary = NothingWhite,

    primaryContainer = NothingSurfaceVariant,
    onPrimaryContainer = NothingWhite,

    secondary = NothingWhite,
    onSecondary = NothingBlack,

    secondaryContainer = NothingSurfaceVariant,
    onSecondaryContainer = NothingWhite,

    tertiary = NothingRed,
    onTertiary = NothingWhite,

    tertiaryContainer = NothingSurfaceVariant,
    onTertiaryContainer = NothingWhite,

    background = NothingBlack,
    onBackground = NothingWhite,

    surface = NothingCardSurface,
    onSurface = NothingWhite,

    outline = NothingBorder,

    surfaceVariant = NothingSurfaceVariant,
    onSurfaceVariant = NothingTextSecondary
)

fun applyWindowColors(window: Window, @ColorInt color: Int, statusBar: Boolean) {
    if(statusBar) {
        window.statusBarColor = color
    }
    window.navigationBarColor = color

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
    }

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val view = window.decorView
        val uiFlags = view.systemUiVisibility

        val luminance = sqrt(
            0.299 * android.graphics.Color.red(color) / 255.0
                    + 0.587 * android.graphics.Color.green(color) / 255.0
                    + 0.114 * android.graphics.Color.blue(color) / 255.0
        )

        if (luminance > 0.5 && color != android.graphics.Color.TRANSPARENT) {
            view.systemUiVisibility = uiFlags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            view.systemUiVisibility = uiFlags and (View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv())
        }
    }
}

@Composable
fun StatusBarColorSetter() {
    val backgroundColor = MaterialTheme.colorScheme.background
    val context = LocalContext.current
    LaunchedEffect(backgroundColor) {
        applyWindowColors((context as Activity).window, backgroundColor.toArgb(), statusBar = false)
    }
}

@Composable
fun UixThemeWrapper(colorScheme: ColorScheme, content: @Composable () -> Unit) {
    MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
    )
}

fun ThemeOption?.ensureAvailable(context: Context): ThemeOption? {
    return if(this == null) {
        null
    } else {
        if(!this.available(context)) {
            null
        } else {
            this
        }
    }
}

@Composable
fun UixThemeAuto(content: @Composable () -> Unit) {
    val context = LocalContext.current

    val themeIdx = useDataStoreValue(THEME_KEY)

    val theme: ThemeOption = themeIdx?.let { ThemeOptions[it].ensureAvailable(context) }
        ?: VoiceInputTheme

    val colors = remember(theme.key) { theme.obtainColors(context) }

    UixThemeWrapper(colorScheme = colors, content)
}