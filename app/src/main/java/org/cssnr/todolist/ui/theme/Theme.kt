package org.cssnr.todolist.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.rememberDynamicColorScheme
import org.cssnr.todolist.data.ColorSeed

/**
 * The color scheme a [ColorSeed] resolves to in the current light/dark mode.
 *
 * Nothing here is hand-written: a seed entry is expanded into the whole Material 3 scheme by
 * MaterialKolor (Google's own Material Color Utilities algorithm, SchemeTonalSpot at contrast
 * 0.0), which is the same variant Material You uses on Android 12/13.
 *
 * [ColorSeed.DYNAMIC] falls back to the generated default palette below Android 12, where the
 * platform has no wallpaper colors to read, so the app never renders an empty theme.
 */
@Composable
fun colorSchemeFor(seed: ColorSeed, darkTheme: Boolean): ColorScheme = when {
    seed.seed != null ->
        rememberDynamicColorScheme(seedColor = seed.seed, isDark = darkTheme)

    seed == ColorSeed.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }

    // DEFAULT is the untouched Material 3 baseline, which is precisely what these builders return
    // with no overrides, so there is nothing to generate for it.
    else -> if (darkTheme) darkColorScheme() else lightColorScheme()
}

@Composable
fun TodoListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    seed: ColorSeed = ColorSeed.DYNAMIC,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(seed = seed, darkTheme = darkTheme),
        typography = Typography,
        content = content,
    )
}
