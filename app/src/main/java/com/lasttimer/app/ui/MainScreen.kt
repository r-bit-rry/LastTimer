package com.lasttimer.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.lasttimer.app.R
import com.lasttimer.app.ui.countdown.CountdownScreen
import com.lasttimer.app.ui.group.GroupScreen
import com.lasttimer.app.ui.settings.SettingsScreen
import com.lasttimer.app.ui.stopwatch.StopwatchScreen
import com.lasttimer.app.ui.templates.TemplatesScreen
import com.lasttimer.app.ui.timer.TimerScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun MainScreen() {
    // Use PagerState for managing pages
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    // Navigation item data class
    data class NavItem(
        val icon: ImageVector,
        val labelRes: Int,
        val index: Int
    )

    // Keep track of the selected tab index based on pager state
    var selectedTab by remember { mutableStateOf(pagerState.currentPage) }

    // Update selectedTab when pagerState changes (e.g., due to swipe)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                selectedTab = page
            }
    }

    // Function to get current screen title based on selected tab
    val screenTitle = when(selectedTab) {
        0 -> stringResource(R.string.tab_timer)
        1 -> stringResource(R.string.tab_stopwatch)
        2 -> stringResource(R.string.tab_countdown)
        3 -> stringResource(R.string.tab_group)
        4 -> stringResource(R.string.tab_templates)
        5 -> stringResource(R.string.settings)
        else -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.height(64.dp)) {
                // Update NavigationBarItems to scroll the pager
                NavigationBarItem(
                    icon = { 
                        // Add scale animation to selected icon
                        val scale = if (selectedTab == 0) 1.2f else 1.0f
                        Icon(
                            Icons.Filled.Timer, 
                            contentDescription = stringResource(R.string.tab_timer), 
                            modifier = Modifier
                                .size(28.dp)
                                .scale(scale)
                        )
                    },
                    selected = selectedTab == 0,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    label = { }
                )
                NavigationBarItem(
                    icon = { 
                        val scale = if (selectedTab == 1) 1.2f else 1.0f
                        Icon(
                            Icons.Filled.Timelapse, 
                            contentDescription = stringResource(R.string.tab_stopwatch), 
                            modifier = Modifier
                                .size(28.dp)
                                .scale(scale)
                        )
                    },
                    selected = selectedTab == 1,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    label = { }
                )
                NavigationBarItem(
                    icon = { 
                        val scale = if (selectedTab == 2) 1.2f else 1.0f
                        Icon(
                            Icons.Filled.DateRange, 
                            contentDescription = stringResource(R.string.tab_countdown), 
                            modifier = Modifier
                                .size(28.dp)
                                .scale(scale)
                        )
                    },
                    selected = selectedTab == 2,
                    onClick = {
                         coroutineScope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                    label = { }
                )
                NavigationBarItem(
                    icon = { 
                        val scale = if (selectedTab == 3) 1.2f else 1.0f
                        Icon(
                            Icons.Filled.Layers, 
                            contentDescription = stringResource(R.string.tab_group), 
                            modifier = Modifier
                                .size(28.dp)
                                .scale(scale)
                        )
                    },
                    selected = selectedTab == 3,
                    onClick = {
                         coroutineScope.launch {
                            pagerState.animateScrollToPage(3)
                        }
                    },
                    label = { }
                )
                NavigationBarItem(
                    icon = { 
                        val scale = if (selectedTab == 4) 1.2f else 1.0f
                        Icon(
                            Icons.Filled.Bookmark, 
                            contentDescription = stringResource(R.string.tab_templates), 
                            modifier = Modifier
                                .size(28.dp)
                                .scale(scale)
                        )
                    },
                    selected = selectedTab == 4,
                    onClick = {
                         coroutineScope.launch {
                            pagerState.animateScrollToPage(4)
                        }
                     },
                    label = { }
                )
                NavigationBarItem(
                    icon = { 
                        val scale = if (selectedTab == 5) 1.2f else 1.0f
                        Icon(
                            Icons.Filled.Settings, 
                            contentDescription = stringResource(R.string.settings), 
                            modifier = Modifier
                                .size(28.dp)
                                .scale(scale)
                        )
                    },
                    selected = selectedTab == 5,
                    onClick = {
                         coroutineScope.launch {
                            pagerState.animateScrollToPage(5)
                        }
                    },
                    label = { }
                )
            }
        }
    ) { innerPadding ->
        // Use HorizontalPager for swipeable content
        HorizontalPager(
            count = 6, // Number of tabs
            state = pagerState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(), // Ensure pager fills the available space
            // Add key parameter to force recomposition when needed
            key = { it },
            // Prevent offscreen page prefetching to avoid premature service binding
            userScrollEnabled = true
        ) { page ->
            // Add enhanced slide animation for the content when switching pages
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                // Content for each page - Simple direct approach without try-catch
                when (page) {
                    0 -> TimerScreen()
                    1 -> StopwatchScreen()
                    2 -> CountdownScreen()
                    3 -> GroupScreen()
                    4 -> TemplatesScreen(
                        onNavigateBack = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                        onNavigateToTimer = { _ -> coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                    )
                    5 -> SettingsScreen()
                }
            }
        }
    }
}

// Add the FallbackErrorScreen composable
@Composable
fun FallbackErrorScreen(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong.",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateBack) {
            Text("Return to Timer Screen")
        }
    }
}
