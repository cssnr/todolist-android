package org.cssnr.todolist.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.cssnr.todolist.R
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
    onCrashReportingChange: (Boolean) -> Unit,
    onCrashReportingMoreInfo: () -> Unit,
) {
    var showCrashReportingDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionHeader("Application")
            SettingToggleRow(
                leadingIcon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Auto Open Last List",
                subtitle = "Open your last list on launch",
                checked = settings.autoOpenLastList,
                onToggle = onAutoOpenLastListChange,
            )
            SettingToggleRow(
                leadingIcon = Icons.Filled.Category,
                title = "Show Search Categories",
                subtitle = "Show categories in suggestions",
                checked = settings.showSearchCategories,
                onToggle = onShowSearchCategoriesChange,
            )
            SettingToggleRow(
                leadingIcon = Icons.Filled.FormatStrikethrough,
                title = "Full Width Strikethrough",
                subtitle = "Strike line across the whole row",
                checked = settings.fullWidthStrikethrough,
                onToggle = onFullWidthStrikethroughChange,
            )
            SettingsSectionHeader("Debugging")
            SettingToggleRow(
                leadingIcon = Icons.Filled.BugReport,
                title = "Enable Crash Reporting",
                subtitle = "Send Crash Reports",
                checked = settings.crashReporting,
                onToggle = { newValue ->
                    if (newValue) {
                        onCrashReportingChange(true)
                    } else {
                        showCrashReportingDialog = true
                    }
                },
            )
        }
    }

    if (showCrashReportingDialog) {
        AlertDialog(
            onDismissRequest = { showCrashReportingDialog = false },
            title = { Text("Please Reconsider") },
            text = { Text(stringResource(R.string.acra_disable_message)) },
            confirmButton = {
                Row {
                    TextButton(onClick = onCrashReportingMoreInfo) {
                        Text("More Info")
                    }
                    TextButton(
                        onClick = {
                            showCrashReportingDialog = false
                            onCrashReportingChange(false)
                        }
                    ) {
                        Text("Disable")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCrashReportingDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SettingToggleRow(
    leadingIcon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onToggle(!checked) },
        leadingContent = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
            )
        },
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { subtitle ->
            { Text(subtitle) }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
            )
        },
    )
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
                crashReporting = true,
            ),
            onAutoOpenLastListChange = {},
            onShowSearchCategoriesChange = {},
            onFullWidthStrikethroughChange = {},
            onCrashReportingChange = {},
            onCrashReportingMoreInfo = {},
        )
    }
}