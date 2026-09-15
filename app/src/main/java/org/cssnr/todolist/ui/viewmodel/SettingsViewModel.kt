package org.cssnr.todolist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cssnr.todolist.data.SettingsRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    val autoOpenLastList: StateFlow<Boolean> = settingsRepository.autoOpenLastList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    val showSearchCategories: StateFlow<Boolean> = settingsRepository.showSearchCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    val crashReporting: StateFlow<Boolean> = settingsRepository.crashReporting
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true,
        )

    fun setAutoOpenLastList(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoOpenLastList(enabled)
        }
    }

    fun setShowSearchCategories(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowSearchCategories(enabled)
        }
    }

    fun setCrashReporting(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCrashReporting(enabled)
        }
    }
}