package org.cssnr.todolist.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.rememberDynamicColorScheme
import org.cssnr.todolist.data.SettingsRepository

/**
 * Whether this device can supply Material You's dynamic colors.
 *
 * The system tonal palette is an Android 12 (API 31) feature with no equivalent below it, so
 * this is the single predicate the theme and the appearance settings must both agree on; a
 * second, independently written copy of this check is how the two drift apart.
 */
val dynamicColorSupported: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * The color scheme for the current dynamic/static choice in the current light/dark mode.
 *
 * A static seed is expanded into the whole Material 3 scheme by MaterialKolor (Google's own
 * Material Color Utilities algorithm, SchemeTonalSpot at contrast 0.0), the same variant
 * Material You uses on Android 12/13.
 *
 * Dynamic only means anything on Android 12+; below that the platform exposes no wallpaper
 * colors to read, so a stored "dynamic" choice falls back to the seed the picker writes. That
 * is the only scheme available there, and it keeps the seed picker meaningful on every device
 * the app supports.
 */
@Composable
fun colorSchemeFor(dynamic: Boolean, seedColor: Color, darkTheme: Boolean): ColorScheme {
    return if (dynamic && dynamicColorSupported) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        rememberDynamicColorScheme(seedColor = seedColor, isDark = darkTheme)
    }
}

/**
 * The vivid seed color for a hue on the picker ramp (saturation and value fixed at 1).
 * Slider, preview, and theme all derive from the persisted hue through this one function,
 * so the three can never disagree.
 */
fun seedColorForHue(hue: Float): Color =
    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))

@Composable
fun TodoListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamic: Boolean = true,
    seedHue: Float = SettingsRepository.DEFAULT_SEED_HUE,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(
            dynamic = dynamic,
            seedColor = seedColorForHue(seedHue),
            darkTheme = darkTheme,
        ),
        typography = Typography,
        content = content,
    )
}
