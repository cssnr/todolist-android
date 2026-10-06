package org.cssnr.todolist.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cssnr.todolist.data.CatalogDatabase
import org.cssnr.todolist.data.CatalogRepository
import org.cssnr.todolist.data.CatalogSuggestion
import org.cssnr.todolist.data.SettingsRepository
import org.cssnr.todolist.data.TodoItemEntity
import org.cssnr.todolist.data.TodoListDatabase
import org.cssnr.todolist.data.TodoListEntity
import org.cssnr.todolist.data.TodoListRepository
import kotlin.time.Duration.Companion.milliseconds

class ListDetailViewModel(
    application: Application,
    private val listId: Long,
) : AndroidViewModel(application) {

    private val repository = TodoListRepository(TodoListDatabase.get(application).todoListDao())
    private val catalogRepository = CatalogRepository(
        application,
        CatalogDatabase.get(application).catalogDao(),
    )
    private val settingsRepository = SettingsRepository(application)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val suggestions: StateFlow<List<CatalogSuggestion>> = _query
        .debounce(100.milliseconds)
        .distinctUntilChanged()
        .flatMapLatest { prefix ->
            if (prefix.isBlank()) {
                flowOf(emptyList())
            } else {
                catalogRepository.autocomplete(prefix.trim())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val list: StateFlow<TodoListEntity?> = repository.observeList(listId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val items: StateFlow<List<TodoItemEntity>> = repository.observeItems(listId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val categories: StateFlow<List<String>> = combine(
        catalogRepository.observeCategories(),
        items,
    ) { catalog, items ->
        (catalog.map { it.name } + items.mapNotNull { it.category })
            .distinctBy { it.lowercase() }
            .sortedWith(Comparator { a, b -> a.compareTo(b, ignoreCase = true) })
    }.distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val showSearchCategories: StateFlow<Boolean> = settingsRepository.showSearchCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    val fullWidthStrikethrough: StateFlow<Boolean> = settingsRepository.fullWidthStrikethrough
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    init {
        viewModelScope.launch { catalogRepository.ensureSeeded() }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun addItem(text: String, category: String? = null) {
        viewModelScope.launch { repository.addItem(listId, text, category) }
    }

    fun toggleItem(item: TodoItemEntity) {
        viewModelScope.launch { repository.toggleItem(item) }
    }

    fun updateItem(item: TodoItemEntity, newText: String, newCategory: String?) {
        viewModelScope.launch { repository.updateItem(item, newText, newCategory) }
    }

    fun deleteItem(item: TodoItemEntity) {
        viewModelScope.launch { repository.deleteItem(item) }
    }

    fun setHideCompleted(hideCompleted: Boolean) {
        viewModelScope.launch { repository.setHideCompleted(listId, hideCompleted) }
    }

    fun crossAllItems() {
        viewModelScope.launch { repository.crossAllItems(listId) }
    }

    fun uncrossAllItems() {
        viewModelScope.launch { repository.uncrossAllItems(listId) }
    }

    fun importItems(text: String): Int {
        val parsed = parseItemsForImport(text)
        viewModelScope.launch { importParsed(parsed) }
        return parsed.size
    }

    suspend fun importParsed(parsed: List<Pair<String, String?>>) {
        withContext(NonCancellable) {
            val existingCategories = repository.listCategories(listId) +
                parsed.mapNotNull { it.second }
            val categoryByName = existingCategories
                .distinctBy { it.lowercase() }
                .associateBy { it.lowercase() }
            for ((itemText, category) in parsed) {
                val resolvedCategory = category?.let {
                    categoryByName[it.lowercase()] ?: it
                }
                repository.addItem(listId, itemText, resolvedCategory)
            }
        }
    }

    private val _aiStatus = MutableStateFlow<String?>(null)
    val aiStatus: StateFlow<String?> = _aiStatus

    private val _aiWorking = MutableStateFlow(false)
    val aiWorking: StateFlow<Boolean> = _aiWorking

    private val _aiImportDone = MutableStateFlow(false)
    val aiImportDone: StateFlow<Boolean> = _aiImportDone

    private var aiJob: Job? = null

    fun startAiImport(text: String) {
        val input = text.trim()
        if (input.isEmpty() || _aiWorking.value) return
        _aiStatus.value = "Checking AICore status…"
        _aiWorking.value = true
        aiJob = viewModelScope.launch {
            try {
                val code = AiImportHelper.checkStatusCode()
                if (code == null) {
                    _aiStatus.value =
                        "AICore did not respond. Check that Google AICore is " +
                            "installed and updated, then retry."
                    notify("AICore unavailable")
                    return@launch
                }
                _aiStatus.value = "AICore status: ${AiImportHelper.statusName(code)}"
                var failure: String? = null
                val ready = AiImportHelper.ensureDownloaded(
                    initialStatus = code,
                    onStarted = {
                        _aiStatus.value =
                            "Downloading AI model (first download can take minutes)…"
                    },
                    onProgress = { downloaded ->
                        _aiStatus.value = "Downloading AI model… ${(downloaded / 1024)} KB"
                    },
                    onFailed = { failure = it },
                )
                if (!ready) {
                    _aiStatus.value = failure
                        ?: "AICore status ${AiImportHelper.statusName(code)}, model not ready. Retry."
                    notify("AI unavailable on this device")
                    return@launch
                }
                _aiStatus.value = "Parsing with on-device AI…"
                val parsed = AiImportHelper.parseWithAi(input, currentCategories())
                if (parsed.isEmpty()) {
                    _aiStatus.value = null
                    notify("AI found no items")
                } else {
                    importParsed(parsed)
                    notify("Imported ${parsed.size} items")
                    _aiImportDone.value = true
                }
            } catch (_: CancellationException) {
                _aiStatus.value = "Cancelled."
            } catch (e: Exception) {
                _aiStatus.value = "AI import failed: ${AiImportHelper.describeError(e)}"
                notify("AI import failed")
            } finally {
                _aiWorking.value = false
                aiJob = null
            }
        }
    }

    fun cancelAiImport() {
        aiJob?.cancel()
        aiJob = null
        _aiWorking.value = false
    }

    private suspend fun currentCategories(): List<String> {
        val catalog = catalogRepository.observeCategories().first().map { it.name }
        val items = repository.listCategories(listId)
        return (catalog + items)
            .distinctBy { it.lowercase() }
            .sortedWith(Comparator { a, b -> a.compareTo(b, ignoreCase = true) })
    }

    private fun notify(message: String) {
        Toast.makeText(getApplication<Application>(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun factory(listId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    ?: error("Application not available")
                ListDetailViewModel(application, listId)
            }
        }
    }
}
