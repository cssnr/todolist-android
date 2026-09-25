package org.cssnr.todolist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.charlex.compose.RevealDirection
import de.charlex.compose.RevealSwipe
import de.charlex.compose.RevealValue
import de.charlex.compose.rememberRevealState
import de.charlex.compose.reset
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
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
        onRenameList = viewModel::renameList,
        onDeleteList = viewModel::deleteList,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    lists: List<TodoListEntity>,
    onOpenList: (Long) -> Unit,
    onAddList: (String) -> Unit,
    onRenameList: (TodoListEntity, String) -> Unit,
    onDeleteList: (TodoListEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var pendingRenameList by remember { mutableStateOf<TodoListEntity?>(null) }
    var pendingDeleteList by remember { mutableStateOf<TodoListEntity?>(null) }
    var revealedListId by remember { mutableStateOf<Long?>(null) }

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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(lists, key = { it.id }) { list ->
                    val revealScope = rememberCoroutineScope()
                    val revealState = rememberRevealState(
                        maxRevealDp = 160.dp,
                        directions = setOf(RevealDirection.StartToEnd),
                    )
                    LaunchedEffect(revealState) {
                        snapshotFlow { revealState.anchoredDraggableState.currentValue }
                            .distinctUntilChanged()
                            .collect { value ->
                                if (value != RevealValue.Default) {
                                    revealedListId = list.id
                                } else if (revealedListId == list.id) {
                                    revealedListId = null
                                }
                            }
                    }
                    LaunchedEffect(revealedListId) {
                        if (revealedListId != list.id &&
                            revealState.anchoredDraggableState.currentValue != RevealValue.Default
                        ) {
                            revealState.reset()
                        }
                    }
                    RevealSwipe(
                        modifier = Modifier.fillMaxWidth(),
                        state = revealState,
                        coroutineScope = revealScope,
                        onContentClick = null,
                        backgroundStartActionLabel = "List actions",
                        backgroundEndActionLabel = null,
                        backgroundCardStartColor = Color.Transparent,
                        backgroundCardEndColor = Color.Transparent,
                        shape = MaterialTheme.shapes.medium,
                        card = { shape, content ->
                            Card(
                                modifier = Modifier.matchParentSize(),
                                colors = CardDefaults.cardColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                ),
                                shape = shape,
                                content = content,
                            )
                        },
                        hiddenContentStart = {
                            Row(modifier = Modifier.fillMaxSize()) {
                                SwipeActionButton(
                                    modifier = Modifier.weight(1f),
                                    background = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    icon = Icons.Filled.Edit,
                                    label = "Edit",
                                    onClick = {
                                        revealScope.launch { revealState.reset() }
                                        pendingRenameList = list
                                    },
                                )
                                SwipeActionButton(
                                    modifier = Modifier.weight(1f),
                                    background = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                    icon = Icons.Filled.Delete,
                                    label = "Delete",
                                    onClick = {
                                        revealScope.launch { revealState.reset() }
                                        pendingDeleteList = list
                                    },
                                )
                            }
                        },
                    ) { shape ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = shape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            onClick = {
                                val isOpen =
                                    revealState.anchoredDraggableState.targetValue != RevealValue.Default
                                if (isOpen) {
                                    revealScope.launch { revealState.reset() }
                                } else {
                                    onOpenList(list.id)
                                }
                            },
                        ) {
                            ListItem(
                                modifier = Modifier.fillMaxWidth(),
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent,
                                ),
                                headlineContent = { Text(list.name) },
                            )
                        }
                    }
                }
            }
        }
    }

    pendingDeleteList?.let { list ->
        ConfirmActionDialog(
            title = "Delete list",
            message = "Are you sure you want to delete \"${list.name}\"? This will remove all items in it.",
            onDismiss = { pendingDeleteList = null },
            onConfirm = {
                onDeleteList(list)
                pendingDeleteList = null
            },
        )
    }

    pendingRenameList?.let { list ->
        RenameListDialog(
            list = list,
            lists = lists,
            onDismiss = { pendingRenameList = null },
            onConfirm = { name ->
                onRenameList(list, name)
                pendingRenameList = null
            },
        )
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

@Composable
private fun RenameListDialog(
    list: TodoListEntity,
    lists: List<TodoListEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val textState = rememberTextFieldState(initialText = list.name)
    val trimmedName = textState.text.toString().trim()
    val nameTaken = trimmedName.isNotEmpty() && lists.any {
        it.id != list.id && it.name.equals(trimmedName, ignoreCase = true)
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename list") },
        text = {
            OutlinedTextField(
                state = textState,
                modifier = Modifier.focusRequester(focusRequester),
                label = { Text("Name") },
                lineLimits = TextFieldLineLimits.SingleLine,
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
                enabled = trimmedName.isNotBlank() && !nameTaken && trimmedName != list.name,
            ) {
                Text("Save")
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
            onRenameList = { _, _ -> },
            onDeleteList = {},
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
            onRenameList = { _, _ -> },
            onDeleteList = {},
        )
    }
}