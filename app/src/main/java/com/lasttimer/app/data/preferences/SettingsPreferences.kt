package com.lasttimer.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Define theme mode enum
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Keys for preferences
    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    // Theme mode preferences
    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val themeModeString = preferences[THEME_MODE] ?: ThemeMode.SYSTEM.name
        ThemeMode.valueOf(themeModeString)
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
    }

    // Sound preferences
    val soundEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SOUND_ENABLED] ?: true
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }

    // Vibration preferences
    val vibrationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    // Keep screen on preference
    val keepScreenOn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEEP_SCREEN_ON] ?: false
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEEP_SCREEN_ON] = enabled
        }
    }
}
