package com.lasttimer.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lasttimer.app.R
import com.lasttimer.app.ui.countdown.CountdownScreen
import com.lasttimer.app.ui.group.GroupScreen
import com.lasttimer.app.ui.settings.SettingsScreen
import com.lasttimer.app.ui.stopwatch.StopwatchScreen
import com.lasttimer.app.ui.templates.TemplatesScreen
import com.lasttimer.app.ui.timer.TimerScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var previousTab by remember { mutableStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Timer, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_timer)) },
                    selected = selectedTab == 0,
                    onClick = { 
                        previousTab = selectedTab
                        selectedTab = 0 
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Timelapse, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_stopwatch)) },
                    selected = selectedTab == 1,
                    onClick = { 
                        previousTab = selectedTab
                        selectedTab = 1 
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_countdown)) },
                    selected = selectedTab == 2,
                    onClick = { 
                        previousTab = selectedTab
                        selectedTab = 2 
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Layers, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_group)) },
                    selected = selectedTab == 3,
                    onClick = { 
                        previousTab = selectedTab
                        selectedTab = 3 
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Bookmark, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_templates)) },
                    selected = selectedTab == 4,
                    onClick = { 
                        previousTab = selectedTab
                        selectedTab = 4 
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.settings)) },
                    selected = selectedTab == 5,
                    onClick = { 
                        previousTab = selectedTab
                        selectedTab = 5 
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Determine slide direction based on previous and current tab
            val slideDirection = if (selectedTab > previousTab) 1 else -1
            
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    // Slide in from right, slide out to left if going forward
                    // Otherwise, slide in from left, slide out to right
                    (slideInHorizontally(animationSpec = tween(300)) { fullWidth -> slideDirection * fullWidth } + 
                    fadeIn(animationSpec = tween(300)))
                        .togetherWith(
                            slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> -slideDirection * fullWidth } + 
                            fadeOut(animationSpec = tween(300))
                        )
                }
            ) { targetTab -> 
                when (targetTab) {
                    0 -> TimerScreen()
                    1 -> StopwatchScreen()
                    2 -> CountdownScreen()
                    3 -> GroupScreen()
                    4 -> TemplatesScreen(
                        onNavigateBack = { selectedTab = 0 }, // Navigate back to timer screen
                        onNavigateToTimer = { _ -> selectedTab = 0 } // Navigate to timer screen with the new timer
                    )
                    5 -> SettingsScreen()
                }
            }
        }
    }
}
