package org.cssnr.todolist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cssnr.todolist.data.SettingsRepository

data class SettingsState(
    val autoOpenLastList: Boolean = true,
    val showSearchCategories: Boolean = true,
    val fullWidthStrikethrough: Boolean = false,
    val useDynamicColor: Boolean = true,
    val crashReporting: Boolean = true,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    val settings: StateFlow<SettingsState?> = combine(
        settingsRepository.autoOpenLastList,
        settingsRepository.showSearchCategories,
        settingsRepository.fullWidthStrikethrough,
        settingsRepository.useDynamicColor,
        settingsRepository.crashReporting,
    ) { autoOpenLastList, showSearchCategories, fullWidthStrikethrough, useDynamicColor, crashReporting ->
        SettingsState(
            autoOpenLastList = autoOpenLastList,
            showSearchCategories = showSearchCategories,
            fullWidthStrikethrough = fullWidthStrikethrough,
            useDynamicColor = useDynamicColor,
            crashReporting = crashReporting,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
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

    fun setFullWidthStrikethrough(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFullWidthStrikethrough(enabled)
        }
    }

    fun setUseDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseDynamicColor(enabled)
        }
    }

    fun setCrashReporting(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCrashReporting(enabled)
        }
    }
}