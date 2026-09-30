package org.cssnr.todolist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cssnr.todolist.data.ColorSeed
import org.cssnr.todolist.data.SettingsRepository

data class SettingsState(
    val autoOpenLastList: Boolean = true,
    val showSearchCategories: Boolean = true,
    val fullWidthStrikethrough: Boolean = false,
    val colorSeed: ColorSeed = ColorSeed.DYNAMIC,
    val crashReporting: Boolean = true,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    val settings: StateFlow<SettingsState?> = combine(
        settingsRepository.autoOpenLastList,
        settingsRepository.showSearchCategories,
        settingsRepository.fullWidthStrikethrough,
        settingsRepository.colorSeed,
        settingsRepository.crashReporting,
    ) { autoOpenLastList, showSearchCategories, fullWidthStrikethrough, colorSeed, crashReporting ->
        SettingsState(
            autoOpenLastList = autoOpenLastList,
            showSearchCategories = showSearchCategories,
            fullWidthStrikethrough = fullWidthStrikethrough,
            colorSeed = colorSeed,
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

    fun setColorSeed(seed: ColorSeed) {
        viewModelScope.launch {
            settingsRepository.setColorSeed(seed)
        }
    }

    fun setCrashReporting(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCrashReporting(enabled)
        }
    }
}