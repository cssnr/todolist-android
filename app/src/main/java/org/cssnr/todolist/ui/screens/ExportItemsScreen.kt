package org.cssnr.todolist.ui.screens

import android.content.ClipData
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.cssnr.todolist.R
import org.cssnr.todolist.data.TodoItemEntity
import org.cssnr.todolist.data.TodoListEntity
import org.cssnr.todolist.ui.theme.TodoListTheme
import org.cssnr.todolist.ui.viewmodel.ListDetailViewModel

private const val UNCATEGORIZED = "Uncategorized"

private fun formatItemsForExport(items: List<TodoItemEntity>): String = buildString {
    var started = false
    var currentHeader: String? = null
    for (item in items) {
        val header = item.category ?: UNCATEGORIZED
        if (!started || !header.equals(currentHeader, ignoreCase = true)) {
            appendLine("# $header")
            currentHeader = header
            started = true
        }
        appendLine(item.text)
    }
}

@Composable
fun ExportItemsRoute(
    listId: Long,
    onBack: () -> Unit,
    viewModel: ListDetailViewModel = viewModel(
        factory = ListDetailViewModel.factory(listId),
    ),
) {
    val list by viewModel.list.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    ExportItemsScreen(
        listName = list?.name,
        items = items,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportItemsScreen(
    listName: String?,
    items: List<TodoItemEntity>,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val exportText = remember(items) { formatItemsForExport(items) }
    var pendingExportText by remember { mutableStateOf("") }
    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = pendingExportText
        val success = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(text.toByteArray())
            } != null
        }.getOrDefault(false)
        scope.launch {
            snackbarHostState.showSnackbar(if (success) "Saved" else "Save failed")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Export Items") },
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
            OutlinedTextField(
                value = exportText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Items") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            ExportOption(
                icon = rememberVectorPainter(Icons.Filled.ContentCopy),
                label = "Copy",
                description = "Copy items to the clipboard",
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(ClipData.newPlainText("Todo items", exportText)),
                        )
                        snackbarHostState.showSnackbar("Copied ${items.size} items to clipboard")
                    }
                },
            )
            ExportOption(
                icon = painterResource(R.drawable.md_share_24px),
                label = "Share",
                description = "Share items with another app",
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, exportText)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share items"))
                },
            )
            ExportOption(
                icon = rememberVectorPainter(Icons.Filled.Save),
                label = "Save",
                description = "Save items as a text file",
                onClick = {
                    pendingExportText = exportText
                    val baseName = (listName ?: "list")
                        .replace(Regex("""[^A-Za-z0-9._ -]"""), "_")
                        .trim()
                        .ifEmpty { "list" }
                    saveLauncher.launch("$baseName.txt")
                },
            )
        }
    }
}

@Composable
private fun ExportOption(
    icon: Painter,
    label: String,
    description: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = { Text(description) },
        leadingContent = { Icon(painter = icon, contentDescription = label) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

@Preview(showBackground = true)
@Composable
fun ExportItemsScreenPreview() {
    TodoListTheme {
        ExportItemsScreen(
            listName = "Groceries",
            items = listOf(
                TodoItemEntity(id = 1, listId = 1, text = "Bread", done = false, category = "Bakery"),
                TodoItemEntity(id = 2, listId = 1, text = "Milk", done = false, category = "Dairy & Eggs"),
            ),
            onBack = {},
        )
    }
}