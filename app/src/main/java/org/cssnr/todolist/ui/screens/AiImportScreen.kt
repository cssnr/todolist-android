package org.cssnr.todolist.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.cssnr.todolist.ui.theme.TodoListTheme
import org.cssnr.todolist.ui.viewmodel.AiImportHelper
import org.cssnr.todolist.ui.viewmodel.ListDetailViewModel

@Composable
fun AiImportRoute(
    listId: Long,
    onBack: () -> Unit,
    viewModel: ListDetailViewModel = viewModel(
        factory = ListDetailViewModel.factory(listId),
    ),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    AiImportScreen(
        existingCategories = categories,
        onBack = onBack,
        onImportParsed = { viewModel.importParsed(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiImportScreen(
    existingCategories: List<String>,
    onBack: () -> Unit,
    onImportParsed: suspend (List<Pair<String, String?>>) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    var isWorking by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var workJob by remember { mutableStateOf<Job?>(null) }
    val trimmedText = text.trim()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    fun notify(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    fun cancelAndGoBack() {
        workJob?.cancel()
        workJob = null
        isWorking = false
        onBack()
    }
    BackHandler {
        cancelAndGoBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("AI Import Items") },
                navigationIcon = {
                    IconButton(onClick = { cancelAndGoBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Describe your items in plan language:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Items") },
                placeholder = { Text("Orange juice, eggs, bacon with flour tortillas and some salsa") },
                supportingText = {
                    Text("AI will automatically extract and categorize your items when you Import.")
                },
                enabled = !isWorking,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .focusRequester(focusRequester),
            )
            statusMessage?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isWorking) CircularProgressIndicator()
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = { cancelAndGoBack() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (isWorking) "Cancel" else "Back")
                }
                FilledTonalButton(
                    onClick = {
                        workJob = scope.launch {
                            isWorking = true
                            statusMessage = "Checking AICore status…"
                            try {
                                val code = AiImportHelper.checkStatusCode()
                                if (code == null) {
                                    statusMessage = "Could not reach AICore. Check network and retry."
                                    notify("AICore unreachable")
                                    return@launch
                                }
                                statusMessage =
                                    "AICore status: ${AiImportHelper.statusName(code)}. " +
                                        "First download can take minutes on WiFi."
                                val ready = AiImportHelper.ensureDownloaded(
                                    initialStatus = code,
                                    onStarted = {
                                        statusMessage =
                                            "Download started, waiting for AICore… (status ${AiImportHelper.statusName(code)})"
                                    },
                                    onProgress = { downloaded ->
                                        statusMessage =
                                            "Downloading AI model… ${(downloaded / 1024)} KB"
                                    },
                                )
                                if (!ready) {
                                    statusMessage =
                                        "AI model not ready (status ${AiImportHelper.statusName(code)}). " +
                                            "Update AICore in Play Store, connect WiFi, wait, retry."
                                    notify("AI unavailable on this device")
                                    return@launch
                                }
                                statusMessage = "Parsing with on-device AI…"
                                val parsed = AiImportHelper.parseWithAi(trimmedText, existingCategories)
                                if (parsed.isEmpty()) {
                                    statusMessage = null
                                    notify("AI found no items")
                                } else {
                                    onImportParsed(parsed)
                                    notify("Imported ${parsed.size} items")
                                    workJob = null
                                    onBack()
                                }
                            } catch (_: CancellationException) {
                                statusMessage = "Cancelled."
                            } catch (e: Exception) {
                                statusMessage = "AI import failed: ${AiImportHelper.describeError(e)}"
                                notify("AI import failed")
                            } finally {
                                isWorking = false
                                workJob = null
                            }
                        }
                    },
                    enabled = trimmedText.isNotEmpty() && !isWorking,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Import")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AiImportScreenPreview() {
    TodoListTheme {
        AiImportScreen(
            existingCategories = listOf("Dairy", "Bakery"),
            onBack = {},
            onImportParsed = {},
        )
    }
}
