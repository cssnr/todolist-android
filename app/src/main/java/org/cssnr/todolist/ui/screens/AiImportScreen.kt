package org.cssnr.todolist.ui.screens

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.cssnr.todolist.ui.theme.TodoListTheme
import org.cssnr.todolist.ui.viewmodel.ListDetailViewModel

@Composable
fun AiImportRoute(
    listId: Long,
    onBack: () -> Unit,
    viewModel: ListDetailViewModel = viewModel(
        factory = ListDetailViewModel.factory(listId),
    ),
) {
    val status by viewModel.aiStatus.collectAsStateWithLifecycle()
    val isWorking by viewModel.aiWorking.collectAsStateWithLifecycle()
    val importDone by viewModel.aiImportDone.collectAsStateWithLifecycle()
    LaunchedEffect(importDone) {
        if (importDone) onBack()
    }
    AiImportScreen(
        status = status,
        isWorking = isWorking,
        onBack = onBack,
        onCancel = { viewModel.cancelAiImport() },
        onImport = { viewModel.startAiImport(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiImportScreen(
    status: String?,
    isWorking: Boolean,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onImport: (String) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val trimmedText = text.trim()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    fun cancelAndGoBack() {
        onCancel()
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
                text = "Describe your items in plain language:",
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
            status?.let {
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
                    onClick = { onImport(text) },
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
            status = "Parsing with on-device AI…",
            isWorking = true,
            onBack = {},
            onCancel = {},
            onImport = {},
        )
    }
}
