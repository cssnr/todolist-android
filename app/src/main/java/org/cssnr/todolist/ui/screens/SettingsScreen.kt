package org.cssnr.todolist.ui.screens

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.cssnr.todolist.R
import org.cssnr.todolist.data.ColorSeed
import org.cssnr.todolist.ui.components.SettingsGroup
import org.cssnr.todolist.ui.components.SettingsTile
import org.cssnr.todolist.ui.theme.TodoListTheme
import org.cssnr.todolist.ui.theme.colorSchemeFor
import org.cssnr.todolist.ui.viewmodel.SettingsState
import org.cssnr.todolist.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateToAbout: () -> Unit = {},
) {
    val context = LocalContext.current
    val acraInfoLink = stringResource(R.string.acra_info_link)
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val current = settings ?: return
    SettingsScreen(
        settings = current,
        onAutoOpenLastListChange = viewModel::setAutoOpenLastList,
        onShowSearchCategoriesChange = viewModel::setShowSearchCategories,
        onFullWidthStrikethroughChange = viewModel::setFullWidthStrikethrough,
        onColorSeedChange = viewModel::setColorSeed,
        onCrashReportingChange = viewModel::setCrashReporting,
        onCrashReportingMoreInfo = {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, acraInfoLink.toUri())
            )
        },
        onAboutClick = onNavigateToAbout,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsState,
    onAutoOpenLastListChange: (Boolean) -> Unit,
    onShowSearchCategoriesChange: (Boolean) -> Unit,
    onFullWidthStrikethroughChange: (Boolean) -> Unit,
    onColorSeedChange: (ColorSeed) -> Unit,
    onCrashReportingChange: (Boolean) -> Unit,
    onCrashReportingMoreInfo: () -> Unit,
    onAboutClick: () -> Unit = {},
) {
    var showCrashReportingDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsGroup(
                title = stringResource(R.string.settings_group_application),
                tiles = listOf(
                    SettingsTile.Toggle(
                        icon = rememberVectorPainter(Icons.AutoMirrored.Filled.ExitToApp),
                        title = stringResource(R.string.settings_auto_open_last_list),
                        summary = stringResource(R.string.settings_auto_open_last_list_summary),
                        checked = settings.autoOpenLastList,
                        onCheckedChange = onAutoOpenLastListChange,
                    ),
                    SettingsTile.Toggle(
                        icon = rememberVectorPainter(Icons.Filled.Category),
                        title = stringResource(R.string.settings_show_search_categories),
                        summary = stringResource(R.string.settings_show_search_categories_summary),
                        checked = settings.showSearchCategories,
                        onCheckedChange = onShowSearchCategoriesChange,
                    ),
                    SettingsTile.Toggle(
                        icon = rememberVectorPainter(Icons.Filled.FormatStrikethrough),
                        title = stringResource(R.string.settings_full_width_strikethrough),
                        summary = stringResource(R.string.settings_full_width_strikethrough_summary),
                        checked = settings.fullWidthStrikethrough,
                        onCheckedChange = onFullWidthStrikethroughChange,
                    ),
                ),
            )
            SettingsGroup(
                title = stringResource(R.string.settings_group_appearance),
                tiles = listOf(
                    SettingsTile.Custom(
                        icon = rememberVectorPainter(Icons.Filled.Palette),
                        title = stringResource(R.string.settings_color_scheme),
                        content = {
                            ColorSeedPicker(
                                selected = settings.colorSeed,
                                onSelected = onColorSeedChange,
                            )
                        },
                    ),
                ),
            )
            SettingsGroup(
                title = stringResource(R.string.settings_group_debug),
                tiles = listOf(
                    SettingsTile.Toggle(
                        icon = rememberVectorPainter(Icons.Filled.BugReport),
                        title = stringResource(R.string.settings_crash_reporting),
                        summary = stringResource(R.string.settings_crash_reporting_summary),
                        checked = settings.crashReporting,
                        onCheckedChange = { newValue ->
                            if (newValue) {
                                onCrashReportingChange(true)
                            } else {
                                showCrashReportingDialog = true
                            }
                        },
                    ),
                ),
            )
            SettingsGroup(
                title = stringResource(R.string.settings_group_about),
                tiles = listOf(
                    SettingsTile.Link(
                        icon = rememberVectorPainter(Icons.Filled.Info),
                        title = stringResource(R.string.about_todolist),
                        summary = stringResource(
                            R.string.about_todolist_summary,
                            org.cssnr.todolist.BuildConfig.VERSION_NAME,
                        ),
                        onClick = onAboutClick,
                    ),
                ),
            )
            Spacer(modifier = Modifier.height(CONTENT_BOTTOM_PADDING))
        }
    }

    if (showCrashReportingDialog) {
        AlertDialog(
            onDismissRequest = { showCrashReportingDialog = false },
            title = { Text(stringResource(R.string.acra_disable_title)) },
            text = { Text(stringResource(R.string.acra_disable_message)) },
            confirmButton = {
                Row {
                    TextButton(onClick = onCrashReportingMoreInfo) {
                        Text(stringResource(R.string.acra_disable_more_info))
                    }
                    TextButton(
                        onClick = {
                            showCrashReportingDialog = false
                            onCrashReportingChange(false)
                        }
                    ) {
                        Text(stringResource(R.string.acra_disable_confirm))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCrashReportingDialog = false }) {
                    Text(stringResource(R.string.acra_disable_cancel))
                }
            },
        )
    }
}

private val CONTENT_BOTTOM_PADDING = 16.dp
private val PICKER_SWATCH_SIZE = 36.dp
private val PICKER_SWATCH_GAP = 8.dp
private val PICKER_ROW_GAP = 6.dp
private val PICKER_SELECTED_STROKE = 3.dp
private val PICKER_UNSELECTED_STROKE = 1.dp
private val PICKER_CHECK_SIZE = 18.dp

/**
 * The trailing control of the Appearance group: one swatch per [ColorSeed], with the selected
 * one carrying a ring so the choice is readable without relying on color alone.
 *
 * A seeded entry is drawn as its own seed color, which is the one thing the user is actually
 * picking between. [ColorSeed.DYNAMIC] has no seed of its own, so it previews the primary the
 * platform currently produces; below Android 12 there are no wallpaper colors to read and
 * [ColorSeed.DYNAMIC] resolves to the Material 3 baseline, so its swatch previews that baseline
 * instead. [ColorSeed.DEFAULT] is the baseline too, which makes the two identical there, so
 * [ColorSeed.DEFAULT] is left out of the picker entirely below Android 12 rather than being
 * offered as a second swatch that changes nothing, and the surviving [ColorSeed.DYNAMIC] swatch
 * is labeled for what it renders rather than for where it was meant to get its colors.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSeedPicker(
    selected: ColorSeed,
    onSelected: (ColorSeed) -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val baselineSwatch = colorSchemeFor(ColorSeed.DEFAULT, darkTheme).primary
    val dynamicSwatch = if (dynamicSupported) {
        colorSchemeFor(ColorSeed.DYNAMIC, darkTheme).primary
    } else {
        baselineSwatch
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(PICKER_ROW_GAP),
    ) {
        Text(
            text = colorSeedLabel(selected, dynamicSupported),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(PICKER_SWATCH_GAP),
            verticalArrangement = Arrangement.spacedBy(PICKER_SWATCH_GAP),
        ) {
            ColorSeed.entries
                .filter { dynamicSupported || it != ColorSeed.DEFAULT }
                .forEach { seed ->
                    val swatch = when (seed) {
                        ColorSeed.DYNAMIC -> dynamicSwatch
                        ColorSeed.DEFAULT -> baselineSwatch
                        else -> checkNotNull(seed.seed)
                    }
                    Swatch(
                        color = swatch,
                        selected = seed == selected,
                        contentDescription = colorSeedLabel(seed, dynamicSupported),
                        onClick = { onSelected(seed) },
                    )
                }
        }
    }
}

@Composable
private fun colorSeedLabel(seed: ColorSeed, dynamicSupported: Boolean): String = when {
    // Below Android 12 DYNAMIC has no wallpaper colors to read, so it produces the same baseline
    // as DEFAULT. Naming it for what it actually renders keeps the one swatch offered honest
    // instead of advertising a wallpaper the platform cannot give us.
    seed == ColorSeed.DYNAMIC && !dynamicSupported ->
        stringResource(R.string.settings_color_default)

    else -> stringResource(
        when (seed) {
            ColorSeed.DYNAMIC -> R.string.settings_color_dynamic
            ColorSeed.TODO_LIST -> R.string.settings_color_todolist
            ColorSeed.DEFAULT -> R.string.settings_color_default
            ColorSeed.RED -> R.string.settings_color_red
            ColorSeed.ORANGE -> R.string.settings_color_orange
            ColorSeed.LIME -> R.string.settings_color_lime
            ColorSeed.EMERALD -> R.string.settings_color_emerald
        },
    )
}

@Composable
private fun Swatch(
    color: Color,
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        // clickable sits between background and border so the tap indication is drawn over the
        // swatch fill and the selection ring is drawn over the tap indication. Modifiers later in
        // the chain draw on top of the ones before them, so swapping border and clickable would
        // let the ripple wash out the ring.
        modifier = Modifier
            .size(PICKER_SWATCH_SIZE)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .border(
                width = if (selected) {
                    PICKER_SELECTED_STROKE
                } else {
                    PICKER_UNSELECTED_STROKE
                },
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = contentDescription,
                tint = contentColorFor(color),
                modifier = Modifier.size(PICKER_CHECK_SIZE),
            )
        } else {
            // The label still has to reach accessibility services on unselected swatches.
            Box(modifier = Modifier.semantics { this.contentDescription = contentDescription })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    TodoListTheme {
        SettingsScreen(
            settings = SettingsState(
                autoOpenLastList = true,
                showSearchCategories = true,
                fullWidthStrikethrough = false,
                colorSeed = ColorSeed.DYNAMIC,
                crashReporting = true,
            ),
            onAutoOpenLastListChange = {},
            onShowSearchCategoriesChange = {},
            onFullWidthStrikethroughChange = {},
            onColorSeedChange = {},
            onCrashReportingChange = {},
            onCrashReportingMoreInfo = {},
            onAboutClick = {},
        )
    }
}
