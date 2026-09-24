package org.cssnr.todolist.data

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val AUTO_OPEN_LAST_LIST = booleanPreferencesKey("auto_open_last_list")
        val LAST_OPENED_LIST_ID = longPreferencesKey("last_opened_list_id")
        val SHOW_SEARCH_CATEGORIES = booleanPreferencesKey("show_search_categories")
        val FULL_WIDTH_STRIKETHROUGH = booleanPreferencesKey("full_width_strikethrough")
        const val CRASH_REPORTING = "acra.enable"
    }

    @Suppress("DEPRECATION")
    private val acraPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    val autoOpenLastList: Flow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[Keys.AUTO_OPEN_LAST_LIST] ?: true }

    val lastOpenedListId: Flow<Long?> = context.settingsDataStore.data
        .map { preferences -> preferences[Keys.LAST_OPENED_LIST_ID] }

    val showSearchCategories: Flow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[Keys.SHOW_SEARCH_CATEGORIES] ?: true }

    val fullWidthStrikethrough: Flow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[Keys.FULL_WIDTH_STRIKETHROUGH] ?: false }

    val crashReporting: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == Keys.CRASH_REPORTING) {
                trySend(acraPreferences.getBoolean(Keys.CRASH_REPORTING, true))
            }
        }
        acraPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(acraPreferences.getBoolean(Keys.CRASH_REPORTING, true))
        awaitClose { acraPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    suspend fun setAutoOpenLastList(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.AUTO_OPEN_LAST_LIST] = enabled
        }
    }

    suspend fun setShowSearchCategories(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.SHOW_SEARCH_CATEGORIES] = enabled
        }
    }

    suspend fun setFullWidthStrikethrough(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.FULL_WIDTH_STRIKETHROUGH] = enabled
        }
    }

    fun setCrashReporting(enabled: Boolean) {
        acraPreferences.edit().putBoolean(Keys.CRASH_REPORTING, enabled).apply()
    }

    suspend fun setLastOpenedListId(listId: Long) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.LAST_OPENED_LIST_ID] = listId
        }
    }
}