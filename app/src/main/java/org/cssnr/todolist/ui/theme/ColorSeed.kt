package org.cssnr.todolist.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The color sources offered in Settings -> Appearance.
 *
 * [DYNAMIC] has no seed of its own; it resolves to whatever the user's wallpaper currently
 * produces. [DEFAULT] is the untouched Material 3 baseline. Every other entry carries nothing but
 * a seed [Color]: the full light and dark palettes are derived from it at runtime by
 * [colorSchemeFor], so not a single ColorScheme role is written out anywhere.
 *
 * The seeds are spaced so no two are adjacent on the hue circle (nearest neighbors sit 23 to 40
 * degrees apart), which is what lets the swatches be told apart at a glance in the picker.
 */
enum class ColorSeed(val seed: Color?) {
    DYNAMIC(null),
    TODO_LIST(Color(0xFF16A1E0)),
    DEFAULT(null),
    RED(Color(0xFFF44336)),
    ORANGE(Color(0xFFFFA000)),
    LIME(Color(0xFFAFB42B)),
    EMERALD(Color(0xFF00A86B)),
    ;

    companion object {
        fun fromName(name: String?): ColorSeed =
            entries.firstOrNull { it.name == name } ?: DYNAMIC
    }
}
