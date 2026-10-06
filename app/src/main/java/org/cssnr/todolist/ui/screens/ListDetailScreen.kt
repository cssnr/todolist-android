package org.cssnr.todolist.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import org.cssnr.todolist.R
import org.cssnr.todolist.data.CatalogSuggestion
import org.cssnr.todolist.data.TodoItemEntity
import org.cssnr.todolist.data.TodoListEntity
import org.cssnr.todolist.ui.theme.TodoListTheme
import org.cssnr.todolist.ui.viewmodel.AiImportHelper
import org.cssnr.todolist.ui.viewmodel.ListDetailViewModel

private const val UNCATEGORIZED = "Uncategorized"

private const val NO_CATEGORY_KEY = "no-category"

private val MIN_SHEET_CONTENT_HEIGHT = 200.dp

// Tappable scrim strip kept below the status bar so the sheet can be dismissed
// with a single tap outside. The status bar itself is system-consumed under
// edge-to-edge and never reaches the scrim. 48.dp is the Material minimum touch target.
private val SHEET_TOP_SCRIM_PEEK = 48.dp

private sealed interface SuggestionRow {
    data class Exact(val text: String) : SuggestionRow
    data class Header(val title: String) : SuggestionRow
    data class Suggestion(val suggestion: CatalogSuggestion) : SuggestionRow
}

private sealed interface ItemRow {
    data class Header(val title: String) : ItemRow
    data class Item(val item: TodoItemEntity) : ItemRow
}

private enum class BulkAction(val label: String, val message: String) {
    CrossOut(
        label = "Cross Out All Items",
        message = "This will mark every item in this list as done.",
    ),
    Uncross(
        label = "Uncross All Items",
        message = "This will mark every item in this list as not done.",
    ),
}

private fun groupSuggestions(
    query: String,
    suggestions: List<CatalogSuggestion>,
    showCategories: Boolean,
): List<SuggestionRow> = buildList {
    val trimmed = query.trim()
    if (trimmed.isNotEmpty()) {
        add(SuggestionRow.Exact(trimmed))
    }
    if (showCategories) {
        var lastCategory: String? = null
        for (suggestion in suggestions) {
            val category = suggestion.categoryName
            if (category != lastCategory) {
                add(SuggestionRow.Header(category))
                lastCategory = category
            }
            add(SuggestionRow.Suggestion(suggestion))
        }
    } else {
        suggestions.forEach { add(SuggestionRow.Suggestion(it)) }
    }
}

private fun groupItems(items: List<TodoItemEntity>): List<ItemRow> = buildList {
    var lastCategory: String? = null
    for (item in items) {
        val category = item.category ?: UNCATEGORIZED
        if (!category.equals(lastCategory, ignoreCase = true)) {
            add(ItemRow.Header(category))
            lastCategory = category
        }
        add(ItemRow.Item(item))
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun ListDetailRoute(
    listId: Long,
    onBack: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenAiImport: () -> Unit,
    onLoaded: () -> Unit = {},
    viewModel: ListDetailViewModel = viewModel(
        factory = ListDetailViewModel.factory(listId),
    ),
) {
    val list by viewModel.list.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val showSearchCategories by viewModel.showSearchCategories.collectAsStateWithLifecycle()
    val fullWidthStrikethrough by viewModel.fullWidthStrikethrough.collectAsStateWithLifecycle()
    var isAiAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAiAvailable = AiImportHelper.isAvailable()
    }

    LaunchedEffect(list) {
        if (list != null) onLoaded()
    }

    ListDetailScreen(
        list = list,
        items = items,
        categories = categories,
        query = query,
        suggestions = suggestions,
        showSearchCategories = showSearchCategories,
        fullWidthStrikethrough = fullWidthStrikethrough,
        isAiAvailable = isAiAvailable,
        onBack = onBack,
        onQueryChange = viewModel::setQuery,
        onAddItem = viewModel::addItem,
        onOpenImport = onOpenImport,
        onOpenExport = onOpenExport,
        onOpenAiImport = onOpenAiImport,
        onToggleItem = viewModel::toggleItem,
        onUpdateItem = viewModel::updateItem,
        onDeleteItem = viewModel::deleteItem,
        onToggleHideCompleted = viewModel::setHideCompleted,
        onCrossAllItems = viewModel::crossAllItems,
        onUncrossAllItems = viewModel::uncrossAllItems,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    list: TodoListEntity?,
    items: List<TodoItemEntity>,
    categories: List<String>,
    query: String,
    suggestions: List<CatalogSuggestion>,
    showSearchCategories: Boolean,
    fullWidthStrikethrough: Boolean,
    isAiAvailable: Boolean,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onAddItem: (text: String, category: String?) -> Unit,
    onOpenImport: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenAiImport: () -> Unit,
    onToggleItem: (TodoItemEntity) -> Unit,
    onUpdateItem: (TodoItemEntity, String, String?) -> Unit,
    onDeleteItem: (TodoItemEntity) -> Unit,
    onToggleHideCompleted: (Boolean) -> Unit,
    onCrossAllItems: () -> Unit,
    onUncrossAllItems: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    BackHandler(enabled = expanded) {
        expanded = false
        keyboardController?.hide()
    }
    var editingItem by remember { mutableStateOf<TodoItemEntity?>(null) }
    var pendingDeleteItem by remember { mutableStateOf<TodoItemEntity?>(null) }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var pendingAction by rememberSaveable { mutableStateOf<BulkAction?>(null) }
    var revealedItemId by remember { mutableStateOf<Long?>(null) }
    val hideCompleted = list?.hideCompleted ?: false
    val density = LocalDensity.current
    val statusBarHeight = with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    editingItem?.let { item ->
        EditItemSheet(
            item = item,
            categories = categories,
            statusBarHeight = statusBarHeight,
            onDismiss = { editingItem = null },
            onConfirm = { newText, newCategory ->
                onUpdateItem(item, newText, newCategory)
                editingItem = null
            },
        )
    }

    pendingAction?.let { action ->
        ConfirmActionDialog(
            title = action.label,
            message = action.message,
            onDismiss = { pendingAction = null },
            onConfirm = {
                when (action) {
                    BulkAction.CrossOut -> onCrossAllItems()
                    BulkAction.Uncross -> onUncrossAllItems()
                }
                pendingAction = null
            },
        )
    }

    pendingDeleteItem?.let { item ->
        ConfirmActionDialog(
            title = "Delete item",
            message = "Are you sure you want to delete \"${item.text}\"?",
            onDismiss = { pendingDeleteItem = null },
            onConfirm = {
                onDeleteItem(item)
                pendingDeleteItem = null
            },
        )
    }

    val visibleItems = if (hideCompleted) items.filter { !it.done } else items
    val totalCount = items.size
    val remainingCount = items.count { !it.done }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = list?.name.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        Text(
                            text = "$remainingCount of $totalCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                    IconButton(onClick = { onToggleHideCompleted(!hideCompleted) }) {
                        Icon(
                            imageVector = if (hideCompleted) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (hideCompleted) {
                                "Show all items"
                            } else {
                                "Hide completed items"
                            },
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More options",
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cross Out All Items") },
                                onClick = {
                                    menuExpanded = false
                                    pendingAction = BulkAction.CrossOut
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Uncross All Items") },
                                onClick = {
                                    menuExpanded = false
                                    pendingAction = BulkAction.Uncross
                                },
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Import Items") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenImport()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Export Items") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenExport()
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SearchBar(
                    modifier = Modifier.weight(1f),
                    windowInsets = WindowInsets(0),
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = query,
                            onQueryChange = { newQuery ->
                                onQueryChange(newQuery)
                                if (newQuery.isNotBlank()) {
                                    expanded = true
                                }
                            },
                            onSearch = {
                                if (query.isNotBlank()) {
                                    onAddItem(query.trim(), null)
                                    onQueryChange("")
                                    expanded = false
                                    keyboardController?.hide()
                                }
                            },
                            expanded = expanded,
                            onExpandedChange = { expanded = it && query.isNotBlank() },
                            placeholder = { Text("Search to add an item") },
                            trailingIcon = {
                                if (expanded) {
                                    IconButton(onClick = {
                                        onQueryChange("")
                                        expanded = false
                                        keyboardController?.hide()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Close search",
                                        )
                                    }
                                }
                            },
                        )
                    },
                    expanded = false,
                    onExpandedChange = { expanded = it && query.isNotBlank() },
                ) {
                }
                if (isAiAvailable) {
                    IconButton(onClick = onOpenAiImport) {
                        Icon(
                            painter = painterResource(R.drawable.md_text_to_speech_24px),
                            contentDescription = "AI import items",
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp)
                    .imePadding(),
            ) {
                when {
                    items.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "No todo items yet",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }

                    visibleItems.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "All items are done",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }

                    else -> {
                        val rows = remember(visibleItems) { groupItems(visibleItems) }
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(
                                rows,
                                key = { _, row ->
                                    when (row) {
                                        is ItemRow.Header -> "header-${row.title}"
                                        is ItemRow.Item -> "item-${row.item.id}"
                                    }
                                },
                            ) { index, row ->
                                when (row) {
                                    is ItemRow.Header -> CategoryHeader(row.title)
                                    is ItemRow.Item -> {
                                        val strikeColor = MaterialTheme.colorScheme.error
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
                                                        revealedItemId = row.item.id
                                                    } else if (revealedItemId == row.item.id) {
                                                        revealedItemId = null
                                                    }
                                                }
                                        }
                                        LaunchedEffect(revealedItemId) {
                                            if (revealedItemId != row.item.id &&
                                                revealState.anchoredDraggableState.currentValue != RevealValue.Default
                                            ) {
                                                revealState.reset()
                                            }
                                        }
                                        val showDividerBelow = index + 1 < rows.size &&
                                            rows[index + 1] is ItemRow.Item
                                        Column {
                                        RevealSwipe(
                                            modifier = Modifier.fillMaxWidth(),
                                            state = revealState,
                                            coroutineScope = revealScope,
                                            onContentClick = { onToggleItem(row.item) },
                                            backgroundStartActionLabel = "Item actions",
                                            backgroundEndActionLabel = null,
                                            backgroundCardStartColor = Color.Transparent,
                                            backgroundCardEndColor = Color.Transparent,
                                            shape = MaterialTheme.shapes.medium,
                                            card = { shape, content ->
                                                Card(
                                                    modifier = Modifier.matchParentSize(),
                                                    colors = CardDefaults.cardColors(
                                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                                        containerColor = Color.Transparent,
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
                                                            editingItem = row.item
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
                                                            pendingDeleteItem = row.item
                                                        },
                                                    )
                                                }
                                            },
                                        ) {
                                            val item = row.item
                                            ListItem(
                                                headlineContent = {
                                                    Text(
                                                        modifier = Modifier
                                                            .then(
                                                                if (fullWidthStrikethrough) {
                                                                    Modifier.fillMaxWidth()
                                                                } else {
                                                                    Modifier
                                                                },
                                                            )
                                                            .then(
                                                                if (item.done) {
                                                                    Modifier.drawBehind {
                                                                        drawLine(
                                                                            color = strikeColor,
                                                                            start = Offset(
                                                                                0f,
                                                                                size.height / 2f
                                                                            ),
                                                                            end = Offset(
                                                                                size.width,
                                                                                size.height / 2f
                                                                            ),
                                                                            strokeWidth = 2.dp.toPx(),
                                                                        )
                                                                    }
                                                                } else {
                                                                    Modifier
                                                                },
                                                            ),
                                                        text = item.text,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = if (item.done) {
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurface
                                                        },
                                                    )
                                                },
                                            )
                                        }
                                        if (showDividerBelow) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                            )
                                        }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (expanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.24f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                expanded = false
                                keyboardController?.hide()
                            },
                    )
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .heightIn(max = maxHeight),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shadowElevation = 6.dp,
                        ) {
                            val rows = remember(query, suggestions, showSearchCategories) {
                                groupSuggestions(query, suggestions, showSearchCategories)
                            }
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(
                                    rows,
                                    key = { row ->
                                        when (row) {
                                            is SuggestionRow.Exact -> "exact"
                                            is SuggestionRow.Header -> "header-${row.title}"
                                            is SuggestionRow.Suggestion -> "suggestion-${row.suggestion.id}"
                                        }
                                    },
                                ) { row ->
                                    when (row) {
                                        is SuggestionRow.Exact -> ListItem(
                                            headlineContent = { Text(row.text) },
                                            trailingContent = {
                                                Icon(
                                                    imageVector = Icons.Filled.Add,
                                                    contentDescription = null,
                                                )
                                            },
                                            modifier = Modifier
                                                .clickable {
                                                    onAddItem(row.text, null)
                                                    onQueryChange("")
                                                    expanded = false
                                                    keyboardController?.hide()
                                                }
                                                .fillMaxWidth(),
                                        )

                                        is SuggestionRow.Header -> CategoryHeader(row.title)
                                        is SuggestionRow.Suggestion -> ListItem(
                                            headlineContent = { Text(row.suggestion.name) },
                                            trailingContent = {
                                                Icon(
                                                    imageVector = Icons.Filled.Add,
                                                    contentDescription = null,
                                                )
                                            },
                                            modifier = Modifier
                                                .clickable {
                                                    onAddItem(
                                                        row.suggestion.name,
                                                        row.suggestion.categoryName
                                                    )
                                                    onQueryChange("")
                                                    expanded = false
                                                    keyboardController?.hide()
                                                }
                                                .fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SwipeActionButton(
    modifier: Modifier = Modifier,
    background: Color,
    contentColor: Color,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
            )
        }
    }
}

@Composable
internal fun ConfirmActionDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditItemSheet(
    item: TodoItemEntity,
    categories: List<String>,
    statusBarHeight: Dp,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit,
) {
    val textState = rememberTextFieldState(initialText = item.text)
    val categoryState = rememberTextFieldState(initialText = item.category.orEmpty())
    val trimmedText = textState.text.toString().trim()
    val trimmedCategory = categoryState.text.toString().trim()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        // Cap content to the space the sheet actually has. ModalBottomSheet already
        // applies bottom insets (including IME) via contentWindowInsets, so reading
        // screen height / IME insets by hand here double-counts and goes stale by a
        // frame. BoxWithConstraints tracks the real constraints, keyboard up or down.
        // Keep the host status bar plus a tappable scrim peek above the sheet: under
        // edge-to-edge the status bar strip alone is system-consumed and can't dismiss.
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val maxContentHeight =
                (maxHeight - statusBarHeight - SHEET_TOP_SCRIM_PEEK)
                    .coerceAtLeast(MIN_SHEET_CONTENT_HEIGHT)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxContentHeight),
            ) {
            Text(
                text = "Edit item",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
            )
            OutlinedTextField(
                state = textState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                label = { Text("Item") },
                lineLimits = TextFieldLineLimits.SingleLine,
            )
            OutlinedTextField(
                state = categoryState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp),
                label = { Text("Category") },
                placeholder = { Text("None") },
                lineLimits = TextFieldLineLimits.SingleLine,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(top = 8.dp),
            ) {
                item(key = NO_CATEGORY_KEY) {
                    CategoryOption(
                        label = "No category",
                        selected = trimmedCategory.isEmpty(),
                        onClick = { categoryState.setTextAndPlaceCursorAtEnd("") },
                    )
                }
                items(categories, key = { it.lowercase() }) { category ->
                    CategoryOption(
                        label = category,
                        selected = category.equals(trimmedCategory, ignoreCase = true),
                        onClick = { categoryState.setTextAndPlaceCursorAtEnd(category) },
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { onConfirm(trimmedText, trimmedCategory.ifEmpty { null }) },
                    enabled = trimmedText.isNotBlank(),
                ) {
                    Text("Save")
                }
            }
            }
        }
    }
}

@Composable
private fun CategoryOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = contentColor,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListDetailScreenPreview() {
    TodoListTheme {
        ListDetailScreen(
            list = TodoListEntity(id = 1, name = "Groceries"),
            items = emptyList(),
            categories = emptyList(),
            query = "",
            suggestions = emptyList(),
            showSearchCategories = true,
            fullWidthStrikethrough = false,
            isAiAvailable = true,
            onBack = {},
            onQueryChange = {},
            onAddItem = { _, _ -> },
            onOpenImport = {},
            onOpenExport = {},
            onOpenAiImport = {},
            onToggleItem = {},
            onUpdateItem = { _, _, _ -> },
            onDeleteItem = {},
            onToggleHideCompleted = {},
            onCrossAllItems = {},
            onUncrossAllItems = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListDetailScreenItemsPreview() {
    TodoListTheme {
        ListDetailScreen(
            list = TodoListEntity(id = 1, name = "Groceries", hideCompleted = true),
            items = listOf(
                TodoItemEntity(
                    id = 1,
                    listId = 1,
                    text = "Bread",
                    done = false,
                    category = "Bakery"
                ),
                TodoItemEntity(
                    id = 2,
                    listId = 1,
                    text = "Milk",
                    done = false,
                    category = "Dairy & Eggs"
                ),
                TodoItemEntity(
                    id = 3,
                    listId = 1,
                    text = "Eggs",
                    done = true,
                    category = "Dairy & Eggs"
                ),
                TodoItemEntity(id = 4, listId = 1, text = "Random", done = false, category = null),
            ),
            categories = listOf("Bakery", "Dairy"),
            query = "",
            suggestions = emptyList(),
            showSearchCategories = true,
            fullWidthStrikethrough = false,
            isAiAvailable = false,
            onBack = {},
            onQueryChange = {},
            onAddItem = { _, _ -> },
            onOpenImport = {},
            onOpenExport = {},
            onOpenAiImport = {},
            onToggleItem = {},
            onUpdateItem = { _, _, _ -> },
            onDeleteItem = {},
            onToggleHideCompleted = {},
            onCrossAllItems = {},
            onUncrossAllItems = {},
        )
    }
}
