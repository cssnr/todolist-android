package org.cssnr.todolist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.cssnr.todolist.ui.theme.TodoListTheme
import org.cssnr.todolist.ui.viewmodel.ListDetailViewModel

@Composable
fun ImportItemsRoute(
    listId: Long,
    onBack: () -> Unit,
    viewModel: ListDetailViewModel = viewModel(
        factory = ListDetailViewModel.factory(listId),
    ),
) {
    ImportItemsScreen(
        onBack = onBack,
        onImportItems = viewModel::importItems,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportItemsScreen(
    onBack: () -> Unit,
    onImportItems: (text: String) -> Int,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val trimmedText = text.trim()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Import Items") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                text = "Paste items below:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Items") },
                supportingText = {
                    Text("One item per line. Prefix a line with # Category to group items below it.")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Back")
                }
                FilledTonalButton(
                    onClick = {
                        val count = onImportItems(trimmedText)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (count > 0) "Imported $count items" else "No items to import",
                            )
                        }
                    },
                    enabled = trimmedText.isNotEmpty(),
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
fun ImportItemsScreenPreview() {
    TodoListTheme {
        ImportItemsScreen(
            onBack = {},
            onImportItems = { _ -> 0 },
        )
    }
}