package org.cssnr.todolist.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.cssnr.todolist.R
import org.cssnr.todolist.data.SettingsRepository
import org.cssnr.todolist.ui.components.SettingsGroup
import org.cssnr.todolist.ui.components.SettingsTile
import org.cssnr.todolist.ui.theme.TodoListTheme
import org.cssnr.todolist.ui.theme.dynamicColorSupported
import org.cssnr.todolist.ui.theme.seedColorForHue
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
        onDynamicChange = viewModel::setDynamicColor,
        onSeedHueChange = viewModel::setSeedHue,
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
    onDynamicChange: (Boolean) -> Unit,
    onSeedHueChange: (Float) -> Unit,
    onCrashReportingChange: (Boolean) -> Unit,
    onCrashReportingMoreInfo: () -> Unit,
    onAboutClick: () -> Unit = {},
) {
    var showCrashReportingDialog by rememberSaveable { mutableStateOf(false) }
    val appName = stringResource(R.string.app_name)

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
                    SettingsTile.Toggle(
                        icon = rememberVectorPainter(Icons.Filled.Palette),
                        title = stringResource(R.string.settings_dynamic_color),
                        summary = stringResource(
                            if (dynamicColorSupported) {
                                R.string.settings_dynamic_color_summary
                            } else {
                                R.string.settings_dynamic_color_unavailable
                            },
                        ),
                        checked = settings.dynamicColor && dynamicColorSupported,
                        enabled = dynamicColorSupported,
                        onCheckedChange = onDynamicChange,
                    ),
                    SettingsTile.Custom(
                        icon = rememberVectorPainter(Icons.Filled.Palette),
                        title = stringResource(R.string.settings_seed_color),
                        summary = stringResource(R.string.settings_seed_color_summary),
                        enabled = !settings.dynamicColor || !dynamicColorSupported,
                        content = {
                            SeedColorPicker(
                                seedHue = settings.seedHue,
                                enabled = !settings.dynamicColor || !dynamicColorSupported,
                                onSeedHueChange = onSeedHueChange,
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
                        title = stringResource(R.string.about_todolist, appName),
                        summary = stringResource(
                            R.string.about_todolist_summary,
                            appName,
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
private val PICKER_ROW_GAP = 6.dp
private val PICKER_TRACK_HEIGHT = 40.dp

/**
 * Rainbow hue picker: one M3 Slider over a hue gradient track plus a preview swatch.
 *
 * The persisted value is the hue itself, so slider, preview, and theme all render
 * [seedColorForHue] of one number and can never disagree. The DataStore write happens in
 * `onValueChangeFinished`, not on every drag frame.
 * [enabled] only switches the Slider's hit handling here; the row dims the whole tile
 * (title included) through [SettingsTile.Custom.enabled].
 */
@Composable
private fun SeedColorPicker(
    seedHue: Float,
    enabled: Boolean,
    onSeedHueChange: (Float) -> Unit,
) {
    var hue by remember(seedHue) { mutableFloatStateOf(seedHue) }
    val preview = seedColorForHue(hue)
    val hex = String.format("#%06X", 0xFFFFFF and preview.toArgb())

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(PICKER_ROW_GAP),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(PICKER_SWATCH_SIZE)
                    .clip(CircleShape)
                    .background(preview),
            )
            Text(
                text = hex,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(PICKER_TRACK_HEIGHT)
                .clip(RoundedCornerShape(PICKER_TRACK_HEIGHT / 2))
                .background(Brush.horizontalGradient(HUE_GRADIENT)),
        ) {
            Slider(
                value = hue,
                onValueChange = { hue = it },
                onValueChangeFinished = { onSeedHueChange(hue) },
                valueRange = 0f..360f,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                ),
            )
        }
    }
}

private val HUE_GRADIENT: List<Color> = (0..360 step 30).map { seedColorForHue(it.toFloat()) }

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    TodoListTheme {
        SettingsScreen(
            settings = SettingsState(
                autoOpenLastList = true,
                showSearchCategories = true,
                fullWidthStrikethrough = false,
                dynamicColor = false,
                seedHue = SettingsRepository.DEFAULT_SEED_HUE,
                crashReporting = true,
            ),
            onAutoOpenLastListChange = {},
            onShowSearchCategoriesChange = {},
            onFullWidthStrikethroughChange = {},
            onDynamicChange = {},
            onSeedHueChange = {},
            onCrashReportingChange = {},
            onCrashReportingMoreInfo = {},
            onAboutClick = {},
        )
    }
}
