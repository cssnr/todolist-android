package org.cssnr.todolist.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.cssnr.todolist.data.TodoListEntity
import org.cssnr.todolist.ui.theme.TodoListTheme
import org.cssnr.todolist.ui.viewmodel.ListsViewModel

@Composable
fun ListsRoute(
    onOpenList: (Long) -> Unit,
    onLoaded: () -> Unit = {},
    viewModel: ListsViewModel = viewModel(),
) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    val listsLoaded by viewModel.listsLoaded.collectAsStateWithLifecycle()

    LaunchedEffect(listsLoaded) {
        if (listsLoaded) onLoaded()
    }

    ListsScreen(
        lists = lists,
        onOpenList = { listId ->
            viewModel.onOpenList(listId)
            onOpenList(listId)
        },
        onAddList = viewModel::addList,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    lists: List<TodoListEntity>,
    onOpenList: (Long) -> Unit,
    onAddList: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Lists") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Add list",
                )
            }
        },
    ) { innerPadding ->
        if (lists.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No lists yet")
                Button(onClick = { showAddDialog = true }) {
                    Text("Add your first list")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                items(lists, key = { it.id }) { list ->
                    ListItem(
                        headlineContent = { Text(list.name) },
                        modifier = Modifier.clickable { onOpenList(list.id) },
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddListDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                onAddList(name)
                showAddDialog = false
            },
            isNameTaken = { name ->
                lists.any { it.name.equals(name, ignoreCase = true) }
            },
        )
    }
}

@Composable
private fun AddListDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isNameTaken: (String) -> Boolean,
) {
    var name by rememberSaveable { mutableStateOf("") }
    val trimmedName = name.trim()
    val nameTaken = trimmedName.isNotEmpty() && isNameTaken(trimmedName)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New list") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                isError = nameTaken,
                supportingText = if (nameTaken) {
                    { Text("A list with this name already exists") }
                } else {
                    null
                },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(trimmedName) },
                enabled = trimmedName.isNotBlank() && !nameTaken,
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun ListsScreenEmptyPreview() {
    TodoListTheme {
        ListsScreen(
            lists = emptyList(),
            onOpenList = {},
            onAddList = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListsScreenPreview() {
    TodoListTheme {
        ListsScreen(
            lists = listOf(
                TodoListEntity(id = 1, name = "Groceries"),
                TodoListEntity(id = 2, name = "Work"),
                TodoListEntity(id = 3, name = "Home projects"),
            ),
            onOpenList = {},
            onAddList = {},
        )
    }
}