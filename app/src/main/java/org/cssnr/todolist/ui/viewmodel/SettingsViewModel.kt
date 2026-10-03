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
    val dynamicColor: Boolean = true,
    val seedHue: Float = SettingsRepository.DEFAULT_SEED_HUE,
    val crashReporting: Boolean = true,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    val settings: StateFlow<SettingsState?> = combine(
        combine(
            settingsRepository.autoOpenLastList,
            settingsRepository.showSearchCategories,
            settingsRepository.fullWidthStrikethrough,
        ) { autoOpenLastList, showSearchCategories, fullWidthStrikethrough ->
            Triple(autoOpenLastList, showSearchCategories, fullWidthStrikethrough)
        },
        combine(
            settingsRepository.dynamicColor,
            settingsRepository.seedHue,
            settingsRepository.crashReporting,
        ) { dynamicColor, seedHue, crashReporting ->
            Triple(dynamicColor, seedHue, crashReporting)
        },
    ) { prefs, appearance ->
        val (autoOpenLastList, showSearchCategories, fullWidthStrikethrough) = prefs
        val (dynamicColor, seedHue, crashReporting) = appearance
        SettingsState(
            autoOpenLastList = autoOpenLastList,
            showSearchCategories = showSearchCategories,
            fullWidthStrikethrough = fullWidthStrikethrough,
            dynamicColor = dynamicColor,
            seedHue = seedHue,
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

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDynamicColor(enabled)
        }
    }

    fun setSeedHue(hue: Float) {
        viewModelScope.launch {
            settingsRepository.setSeedHue(hue)
        }
    }

    fun setCrashReporting(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCrashReporting(enabled)
        }
    }
}