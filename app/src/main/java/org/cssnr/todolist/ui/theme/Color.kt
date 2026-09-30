package org.cssnr.todolist.ui.theme

/*
 * There are deliberately no single color constants in this package. The old Android Studio
 * template purples that lived here set only three roles on top of the baseline, so the non-dynamic
 * theme was mostly baseline with a purple tint.
 *
 * Every color now comes from either the platform wallpaper ([ColorSeed.DYNAMIC]), the Material 3
 * baseline ([ColorSeed.DEFAULT]), or a seed expanded at runtime by [colorSchemeFor]. The seeds
 * themselves are the only color literals in the app, and they live in [ColorSeed]; the picker
 * draws its swatches straight from those seeds.
 */
