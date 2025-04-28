package com.lasttimer.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lasttimer.app.data.preferences.SettingsPreferences
import com.lasttimer.app.data.preferences.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {
    
    // Theme preferences
    val themeMode: Flow<ThemeMode> = settingsPreferences.themeMode
    
    fun setThemeMode(themeMode: ThemeMode) = viewModelScope.launch {
        settingsPreferences.setThemeMode(themeMode)
    }
    
    // Sound preferences
    val soundEnabled: Flow<Boolean> = settingsPreferences.soundEnabled
    
    fun setSoundEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsPreferences.setSoundEnabled(enabled)
    }
    
    // Vibration preferences
    val vibrationEnabled: Flow<Boolean> = settingsPreferences.vibrationEnabled
    
    fun setVibrationEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsPreferences.setVibrationEnabled(enabled)
    }
    
    // Keep screen on preference
    val keepScreenOn: Flow<Boolean> = settingsPreferences.keepScreenOn
    
    fun setKeepScreenOn(enabled: Boolean) = viewModelScope.launch {
        settingsPreferences.setKeepScreenOn(enabled)
    }
}
