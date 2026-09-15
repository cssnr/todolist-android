package org.cssnr.todolist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.cssnr.todolist.data.SettingsRepository
import org.cssnr.todolist.data.TodoListDatabase
import org.cssnr.todolist.data.TodoListRepository
import kotlin.time.Duration.Companion.seconds

sealed interface StartupScreen {
    data object Home : StartupScreen
    data class ListDetail(val listId: Long) : StartupScreen
}

class StartupViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val listRepository = TodoListRepository(TodoListDatabase.get(application).todoListDao())

    private val _startupScreen = MutableStateFlow<StartupScreen?>(null)
    val startupScreen: StateFlow<StartupScreen?> = _startupScreen

    private val _shouldKeepSplash = MutableStateFlow(true)
    val shouldKeepSplash: StateFlow<Boolean> = _shouldKeepSplash

    init {
        viewModelScope.launch {
            delay(5.seconds)
            _shouldKeepSplash.value = false
        }
        viewModelScope.launch {
            val screen = withTimeoutOrNull(5.seconds) { resolveStartupScreen() }
                ?: StartupScreen.Home
            _startupScreen.value = screen
        }
    }

    fun markReady() {
        _shouldKeepSplash.value = false
    }

    private suspend fun resolveStartupScreen(): StartupScreen {
        val autoOpen = settingsRepository.autoOpenLastList.first()
        val lists = listRepository.observeLists().first()
        if (!autoOpen) return StartupScreen.Home
        val lastListId = settingsRepository.lastOpenedListId.first()
        return if (lastListId != null && lists.any { it.id == lastListId }) {
            StartupScreen.ListDetail(lastListId)
        } else {
            StartupScreen.Home
        }
    }
}
