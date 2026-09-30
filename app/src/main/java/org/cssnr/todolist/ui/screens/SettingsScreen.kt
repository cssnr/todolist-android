package org.cssnr.todolist.ui.screens

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.cssnr.todolist.R
import org.cssnr.todolist.ui.components.SettingsGroup
import org.cssnr.todolist.ui.components.SettingsTile
import org.cssnr.todolist.ui.theme.TodoListTheme
import org.cssnr.todolist.ui.viewmodel.SettingsState
import org.cssnr.todolist.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsRoute(viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val acraInfoLink = stringResource(R.string.acra_info_link)
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val current = settings ?: return
    SettingsScreen(
        settings = current,
        onAutoOpenLastListChange = viewModel::setAutoOpenLastList,
        onShowSearchCategoriesChange = viewModel::setShowSearchCategories,
        onFullWidthStrikethroughChange = viewModel::setFullWidthStrikethrough,
        onUseDynamicColorChange = viewModel::setUseDynamicColor,
        onCrashReportingChange = viewModel::setCrashReporting,
        onCrashReportingMoreInfo = {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, acraInfoLink.toUri())
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsState,
    onAutoOpenLastListChange: (Boolean) -> Unit,
    onShowSearchCategoriesChange: (Boolean) -> Unit,
    onFullWidthStrikethroughChange: (Boolean) -> Unit,
    onUseDynamicColorChange: (Boolean) -> Unit,
    onCrashReportingChange: (Boolean) -> Unit,
    onCrashReportingMoreInfo: () -> Unit,
) {
    var showCrashReportingDialog by rememberSaveable { mutableStateOf(false) }
    // Dynamic color is a platform feature, so the tile is inert below Android 12 rather than
    // promising something the theme cannot deliver.
    val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

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
                titleRes = R.string.settings_group_application,
                tiles = listOf(
                    SettingsTile.Toggle(
                        icon = rememberVectorPainter(Icons.AutoMirrored.Filled.ExitToApp),
                        titleRes = R.string.settings_auto_open_last_list,
                        summaryRes = R.string.settings_auto_open_last_list_summary,
                        checked = settings.autoOpenLastList,
                        onCheckedChange = onAutoOpenLastListChange,
                    ),
                    SettingsTile.Toggle(
                        icon = rememberVectorPainter(Icons.Filled.Category),
                        titleRes = R.string.settings_show_search_categories,
                        summaryRes = R.string.settings_show_search_categories_summary,
                        checked = settings.showSearchCategories,
                        onCheckedChange = onShowSearchCategoriesChange,
                    ),
                    SettingsTile.Toggle(
                        icon = rememberVectorPainter(Icons.Filled.FormatStrikethrough),
                        titleRes = R.string.settings_full_width_strikethrough,
                        summaryRes = R.string.settings_full_width_strikethrough_summary,
                        checked = settings.fullWidthStrikethrough,
                        onCheckedChange = onFullWidthStrikethroughChange,
                    ),
                ),
            )
            SettingsGroup(
                titleRes = R.string.settings_group_appearance,
                tiles = listOf(
                    SettingsTile.Toggle(
                        icon = rememberVectorPainter(Icons.Filled.Palette),
                        titleRes = R.string.settings_dynamic_color,
                        summaryRes = if (dynamicColorSupported) {
                            R.string.settings_dynamic_color_summary
                        } else {
                            R.string.settings_dynamic_color_unsupported
                        },
                        checked = settings.useDynamicColor,
                        enabled = dynamicColorSupported,
                        onCheckedChange = onUseDynamicColorChange,
                    ),
                ),
            )
            SettingsGroup(
                titleRes = R.string.settings_group_debug,
                tiles = listOf(
                    SettingsTile.Toggle(
                        icon = rememberVectorPainter(Icons.Filled.BugReport),
                        titleRes = R.string.settings_crash_reporting,
                        summaryRes = R.string.settings_crash_reporting_summary,
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

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    TodoListTheme {
        SettingsScreen(
            settings = SettingsState(
                autoOpenLastList = true,
                showSearchCategories = true,
                fullWidthStrikethrough = false,
                useDynamicColor = true,
                crashReporting = true,
            ),
            onAutoOpenLastListChange = {},
            onShowSearchCategoriesChange = {},
            onFullWidthStrikethroughChange = {},
            onUseDynamicColorChange = {},
            onCrashReportingChange = {},
            onCrashReportingMoreInfo = {},
        )
    }
}
