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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.cssnr.todolist.R
import org.cssnr.todolist.ui.theme.TodoListTheme
import org.cssnr.todolist.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsRoute(viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val acraInfoLink = stringResource(R.string.acra_info_link)
    val autoOpenLastList by viewModel.autoOpenLastList.collectAsStateWithLifecycle()
    val showSearchCategories by viewModel.showSearchCategories.collectAsStateWithLifecycle()
    val crashReporting by viewModel.crashReporting.collectAsStateWithLifecycle()

    SettingsScreen(
        autoOpenLastList = autoOpenLastList,
        onAutoOpenLastListChange = viewModel::setAutoOpenLastList,
        showSearchCategories = showSearchCategories,
        onShowSearchCategoriesChange = viewModel::setShowSearchCategories,
        crashReporting = crashReporting,
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
    autoOpenLastList: Boolean,
    onAutoOpenLastListChange: (Boolean) -> Unit,
    showSearchCategories: Boolean,
    onShowSearchCategoriesChange: (Boolean) -> Unit,
    crashReporting: Boolean,
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
            SettingToggleRow(
                leadingIcon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Auto Open Last List",
                subtitle = "Open your last list on launch",
                checked = autoOpenLastList,
                onToggle = onAutoOpenLastListChange,
            )
            SettingToggleRow(
                leadingIcon = Icons.Filled.Category,
                title = "Show Search Categories",
                subtitle = "Show categories in suggestions",
                checked = showSearchCategories,
                onToggle = onShowSearchCategoriesChange,
            )
            SettingToggleRow(
                leadingIcon = Icons.Filled.BugReport,
                title = "Enable Crash Reporting",
                subtitle = "Send Crash Reports",
                checked = crashReporting,
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
            autoOpenLastList = true,
            onAutoOpenLastListChange = {},
            showSearchCategories = true,
            onShowSearchCategoriesChange = {},
            crashReporting = true,
            onCrashReportingChange = {},
            onCrashReportingMoreInfo = {},
        )
    }
}