package com.lasttimer.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lasttimer.app.R
import com.lasttimer.app.data.preferences.ThemeMode
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Theme Settings
            ThemeSettings(viewModel)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Notification Settings
            NotificationSettings(viewModel)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Display Settings
            DisplaySettings(viewModel)
        }
    }
}

@Composable
fun ThemeSettings(viewModel: SettingsViewModel) {
    val themeMode by viewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Brightness4,
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.theme),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Light Mode
            ThemeModeOption(
                text = stringResource(R.string.light_mode),
                icon = Icons.Default.Brightness7,
                selected = themeMode == ThemeMode.LIGHT,
                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
            )
            
            // Dark Mode
            ThemeModeOption(
                text = stringResource(R.string.dark_mode),
                icon = Icons.Default.Brightness4,
                selected = themeMode == ThemeMode.DARK,
                onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
            )
            
            // System Default
            ThemeModeOption(
                text = stringResource(R.string.system_default),
                icon = Icons.Default.BrightnessAuto,
                selected = themeMode == ThemeMode.SYSTEM,
                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
            )
        }
    }
}

@Composable
fun ThemeModeOption(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null because we're handling it in the Row
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(start = 16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
fun NotificationSettings(viewModel: SettingsViewModel) {
    val soundEnabled by viewModel.soundEnabled.collectAsState(initial = true)
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState(initial = true)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.notifications),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sound Toggle
            SettingsSwitchItem(
                text = stringResource(R.string.sound),
                icon = Icons.Default.Notifications,
                checked = soundEnabled,
                onCheckedChange = { viewModel.setSoundEnabled(it) }
            )
            
            // Vibration Toggle
            SettingsSwitchItem(
                text = stringResource(R.string.vibration),
                icon = Icons.Default.Vibration,
                checked = vibrationEnabled,
                onCheckedChange = { viewModel.setVibrationEnabled(it) }
            )
        }
    }
}

@Composable
fun DisplaySettings(viewModel: SettingsViewModel) {
    val keepScreenOn by viewModel.keepScreenOn.collectAsState(initial = false)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Keep Screen On
            SettingsSwitchItem(
                text = stringResource(R.string.keep_screen_on),
                icon = Icons.Default.PhoneAndroid,
                checked = keepScreenOn,
                onCheckedChange = { viewModel.setKeepScreenOn(it) }
            )
        }
    }
}

@Composable
fun SettingsSwitchItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
