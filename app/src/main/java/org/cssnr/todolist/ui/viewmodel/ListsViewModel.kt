package org.cssnr.todolist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cssnr.todolist.data.SettingsRepository
import org.cssnr.todolist.data.TodoListDatabase
import org.cssnr.todolist.data.TodoListEntity
import org.cssnr.todolist.data.TodoListRepository

class ListsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TodoListRepository(TodoListDatabase.get(application).todoListDao())
    private val settingsRepository = SettingsRepository(application)

    val lists: StateFlow<List<TodoListEntity>> = repository.observeLists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val listsLoaded: StateFlow<Boolean> = repository.observeLists()
        .map { true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    fun onOpenList(listId: Long) {
        viewModelScope.launch {
            settingsRepository.setLastOpenedListId(listId)
        }
    }

    fun addList(name: String) {
        viewModelScope.launch {
            repository.addList(name)
        }
    }
}