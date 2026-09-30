package org.cssnr.todolist.data

import androidx.compose.ui.graphics.Color

/**
 * The color sources offered in Settings -> Appearance.
 *
 * [DYNAMIC] has no seed of its own; it resolves to whatever the user's wallpaper currently
 * produces, falling back to the Material 3 baseline below Android 12 where the platform exposes
 * no wallpaper colors. [DEFAULT] is the untouched Material 3 baseline. Every other entry carries
 * nothing but a seed [Color]: the full light and dark palettes are derived from it at runtime by
 * `ui.theme.colorSchemeFor`, so not a single ColorScheme role is written out anywhere.
 *
 * The seeds are spaced so no two are adjacent on the hue circle (nearest neighbors sit 23 to 40
 * degrees apart), which is what lets the swatches be told apart at a glance in the picker.
 *
 * This lives in the data package rather than ui.theme because it is a persisted preference value:
 * SettingsRepository stores it by name and hands it back as part of its public API. The theme
 * layer depends on it, not the other way around.
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
        /**
         * Resolves a stored name back to an entry, falling back to [DYNAMIC] for anything
         * unrecognized, including a name that no longer exists because the entry was renamed.
         */
        fun fromName(name: String?): ColorSeed =
            entries.firstOrNull { it.name == name } ?: DYNAMIC
    }
}
