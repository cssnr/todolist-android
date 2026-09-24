package org.cssnr.todolist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    val showSearchCategories: StateFlow<Boolean> = settingsRepository.showSearchCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    init {
        viewModelScope.launch {
            catalogRepository.ensureSeeded()
        }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun addItem(text: String, category: String? = null) {
        viewModelScope.launch {
            repository.addItem(listId, text, category)
        }
    }

    fun toggleItem(item: TodoItemEntity) {
        viewModelScope.launch {
            repository.toggleItem(item)
        }
    }

    fun updateItem(item: TodoItemEntity, newText: String) {
        viewModelScope.launch {
            repository.updateItem(item, newText)
        }
    }

    fun deleteItem(item: TodoItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun setHideCompleted(hideCompleted: Boolean) {
        viewModelScope.launch {
            repository.setHideCompleted(listId, hideCompleted)
        }
    }

    fun crossAllItems() {
        viewModelScope.launch {
            repository.crossAllItems(listId)
        }
    }

    fun uncrossAllItems() {
        viewModelScope.launch {
            repository.uncrossAllItems(listId)
        }
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