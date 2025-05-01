package com.lasttimer.app.ui.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.lasttimer.app.R
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.service.TimerService
import com.lasttimer.app.ui.common.CustomTimePicker
import com.lasttimer.app.ui.timer.TimerUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isCreateTimerDialogVisible by viewModel.isCreateTimerDialogVisible.collectAsState()
    val context = LocalContext.current
    
    // Timer service connection with improved management
    var timerService by remember { mutableStateOf<TimerService?>(null) }
    var isBound by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Create a persistent service connection
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                println("DEBUG: TimerScreen - Service connected")
                val binder = service as TimerService.TimerBinder
                timerService = binder.getService()
                isBound = true
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {
                println("DEBUG: TimerScreen - Service disconnected")
                timerService = null
                isBound = false
            }
        }
    }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    println("DEBUG: TimerScreen - ON_START, binding to service")
                    val serviceIntent = Intent(context, TimerService::class.java)
                    // Start the service to ensure it's running
                    context.startService(serviceIntent)
                    context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                }
                Lifecycle.Event.ON_STOP -> {
                    if (isBound) {
                        println("DEBUG: TimerScreen - ON_STOP, unbinding from service")
                        context.unbindService(serviceConnection)
                        isBound = false
                    }
                }
                else -> { /* Ignore other lifecycle events */ }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            println("DEBUG: TimerScreen - Disposing")
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Only unbind if we are still bound to avoid IllegalArgumentException
            if (isBound) {
                try {
                    context.unbindService(serviceConnection)
                    isBound = false
                    println("DEBUG: TimerScreen - Successfully unbound service on dispose")
                } catch (e: Exception) {
                    println("ERROR: TimerScreen - Failed to unbind service: ${e.message}")
                }
            }
        }
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateTimerDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.create_timer))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = uiState) {
                is TimerUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading timers...")
                    }
                }
                is TimerUiState.Success -> {
                    val timers = currentState.timers
                    TimerList(
                        timers = timers,
                        timersFlow = viewModel.uiState,
                        timerService = timerService,
                        onStartTimer = { timerId ->
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_START_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                            }
                            context.startService(intent)
                            viewModel.startTimer(timerId, intent)
                        },
                        onPauseTimer = { timerId ->
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_PAUSE_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                            }
                            context.startService(intent)
                            viewModel.pauseTimer(timerId, intent)
                        },
                        onResumeTimer = { timerId ->
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_RESUME_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                            }
                            context.startService(intent)
                            viewModel.resumeTimer(timerId, intent)
                        },
                        onStopTimer = { timerId ->
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_STOP_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                            }
                            context.startService(intent)
                            viewModel.stopTimer(timerId, intent)
                        },
                        onDeleteTimer = { timerId ->
                            viewModel.deleteTimer(timerId)
                        },
                        onSaveAsTemplate = { timerId ->
                            viewModel.saveAsTemplate(timerId)
                        }
                    )
                }
                is TimerUiState.Error -> {
                    val errorMessage = currentState.message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error loading timers: $errorMessage")
                    }
                }
            }
        }
    }
    
    // Show dialog for creating a new timer
    if (isCreateTimerDialogVisible) {
        CreateTimerDialog(
            onDismiss = { viewModel.hideCreateTimerDialog() },
            onCreate = { viewModel.createTimer() },
            viewModel = viewModel
        )
    }
}

@Composable
fun TimerList(
    timers: List<Timer>,
    timersFlow: StateFlow<TimerUiState>,
    timerService: TimerService?,
    onStartTimer: (String) -> Unit,
    onPauseTimer: (String) -> Unit,
    onResumeTimer: (String) -> Unit,
    onStopTimer: (String) -> Unit,
    onDeleteTimer: (String) -> Unit,
    onSaveAsTemplate: (String) -> Unit
) {
    // State for storing timers
    var timersList by remember { mutableStateOf(timers) }
    
    // Collect state from the uiState flow
    LaunchedEffect(timersFlow) {
        timersFlow.collect { state ->
            if (state is TimerUiState.Success) {
                timersList = state.timers
            }
        }
    }
    
    // Collect timer states from the service
    val timerStates by timerService?.timerStates?.collectAsState(emptyMap()) ?: remember {
        mutableStateOf(emptyMap<String, TimerService.TimerState>())
    }
    
    if (timersList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_timers_found),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(timersList, key = { it.id }) { timer ->
                val timerState = timerStates[timer.id]
                
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)) + 
                            slideInVertically(animationSpec = tween(durationMillis = 300)) { it },
                    exit = fadeOut(animationSpec = tween(durationMillis = 300)) + 
                           slideOutVertically(animationSpec = tween(durationMillis = 300)) { it }
                ) {
                    TimerItem(
                        timer = timer,
                        timerState = timerState,
                        onStartTimer = { onStartTimer(timer.id) },
                        onPauseTimer = { onPauseTimer(timer.id) },
                        onResumeTimer = { onResumeTimer(timer.id) },
                        onStopTimer = { onStopTimer(timer.id) },
                        onDeleteTimer = { onDeleteTimer(timer.id) },
                        onSaveAsTemplate = { onSaveAsTemplate(timer.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TimerItem(
    timer: Timer,
    timerState: TimerService.TimerState?,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onDeleteTimer: () -> Unit,
    onSaveAsTemplate: () -> Unit
) {
    val duration = timer.durationMillis ?: 0L
    val elapsedTime = timerState?.elapsedTime ?: 0L
    val remainingTime = (duration - elapsedTime).coerceAtLeast(0L)
    val progress = if (duration > 0) (elapsedTime.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    
    // Add state for edit dialog
    var showEditDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Remove the empty clickable to avoid interfering with gesture detection
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        // Log long press event for debugging
                        println("DEBUG: Long press detected on timer ${timer.id}")
                        showEditDialog = true
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(
                        onClick = {
                            try {
                                println("DEBUG: Delete button clicked for timer ${timer.id}")
                                onDeleteTimer()
                                println("DEBUG: Delete action sent for timer ${timer.id}")
                            } catch (e: Exception) {
                                println("ERROR: Failed to delete timer ${timer.id} - ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    // Update the Save button to make its purpose clearer
                    IconButton(
                        onClick = {
                            try {
                                println("DEBUG: Save as Template button clicked for timer ${timer.id}")
                                onSaveAsTemplate()
                                println("DEBUG: Save as Template action completed for timer ${timer.id}")
                            } catch (e: Exception) {
                                println("ERROR: Failed to save timer as template - ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = stringResource(R.string.save_as_template)
                        )
                    }
                }
            }
            
            // Display time remaining
            Text(
                text = formatTime(remainingTime),
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Progress indicator
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(vertical = 8.dp)
            )
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Dynamic controls based on timer state
                when (timer.status) {
                    TimerStatus.IDLE -> {
                        // Start button with improved click handling and debugging
                        IconButton(
                            onClick = {
                                try {
                                    println("DEBUG: Start button clicked for timer ${timer.id}")
                                    onStartTimer()
                                    println("DEBUG: Start action completed for timer ${timer.id}")
                                } catch (e: Exception) {
                                    // Log the error for debugging
                                    println("ERROR: Failed to start timer ${timer.id} - ${e.message}")
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.start),
                                tint = Color.White
                            )
                        }
                    }
                    TimerStatus.RUNNING -> {
                        // Pause button with improved error handling and debug logs
                        IconButton(
                            onClick = {
                                try {
                                    println("DEBUG: Pause button clicked for timer ${timer.id}")
                                    onPauseTimer()
                                    println("DEBUG: Pause action completed for timer ${timer.id}")
                                } catch (e: Exception) {
                                    // Enhanced error logging
                                    println("ERROR: Failed to pause timer ${timer.id} - ${e.message}")
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = stringResource(R.string.pause),
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp)) // Space between buttons
                        
                        // Stop button with improved error handling and debug logs
                        IconButton(
                            onClick = {
                                try {
                                    println("DEBUG: Stop button clicked for timer ${timer.id}")
                                    onStopTimer()
                                    println("DEBUG: Stop action completed for timer ${timer.id}")
                                } catch (e: Exception) {
                                    // Enhanced error logging
                                    println("ERROR: Failed to stop timer ${timer.id} - ${e.message}")
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = stringResource(R.string.stop),
                                tint = Color.White
                            )
                        }
                    }
                    TimerStatus.PAUSED -> {
                        // Resume button with improved error handling and debug logs
                        IconButton(
                            onClick = {
                                try {
                                    println("DEBUG: Resume button clicked for timer ${timer.id}")
                                    onResumeTimer()
                                    println("DEBUG: Resume action completed for timer ${timer.id}")
                                } catch (e: Exception) {
                                    // Enhanced error logging
                                    println("ERROR: Failed to resume timer ${timer.id} - ${e.message}")
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.resume),
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp)) // Space between buttons
                        
                        // Stop button
                        IconButton(
                            onClick = onStopTimer,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = stringResource(R.string.stop),
                                tint = Color.White
                            )
                        }
                    }
                    TimerStatus.COMPLETED -> {
                        // Restart button - make it more visible with improved logging
                        IconButton(
                            onClick = {
                                try {
                                    println("DEBUG: Restart button clicked for completed timer ${timer.id}")
                                    onStartTimer() // Reuse start action for restart
                                    println("DEBUG: Restart action completed for timer ${timer.id}")
                                } catch (e: Exception) {
                                    // Log error for debugging
                                    println("ERROR: Failed to restart timer ${timer.id} - ${e.message}")
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier
                                .size(58.dp) // Make button larger for better visibility
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = stringResource(R.string.reset),
                                tint = Color.White,
                                modifier = Modifier.size(32.dp) // Larger icon
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Show edit dialog when long press occurs
    if (showEditDialog) {
        EditTimerDialog(
            timer = timer,
            onSave = { newName, newDurationMillis ->
                // Update the timer with a proper API call to the repository through ViewModel
                try {
                    // This should be implemented by passing the update action to the parent
                    // For now, we'll just close the dialog and print debug info
                    println("DEBUG: Updating timer ${timer.id} - Name: $newName, Duration: $newDurationMillis ms")
                    // In a complete implementation, we would call something like:
                    // viewModel.updateTimer(timer.id, newName, newDurationMillis)
                    showEditDialog = false
                } catch (e: Exception) {
                    println("ERROR: Failed to update timer - ${e.message}")
                }
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTimerDialog(
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    viewModel: TimerViewModel
) {
    var hours by remember { mutableStateOf(0) }
    var minutes by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(0) }
    
    // Collect states from ViewModel
    val repeat by viewModel.newTimerRepeat.collectAsState()
    val repeatCount by viewModel.newTimerRepeatCount.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_timer)) },
        text = {
            Column {
                // Use CustomTimePicker
                CustomTimePicker(
                    initialHours = hours,
                    initialMinutes = minutes,
                    initialSeconds = seconds,
                    onTimeChange = { h, m, s ->
                        hours = h
                        minutes = m
                        seconds = s
                        // Update ViewModel
                        viewModel.updateNewTimerHours(h)
                        viewModel.updateNewTimerMinutes(m)
                        viewModel.updateNewTimerSeconds(s)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Repeat options
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = repeat, 
                        onCheckedChange = { viewModel.updateNewTimerRepeat(it) }
                    )
                    Text(stringResource(R.string.repeat))
                }
                
                if (repeat) {
                    OutlinedTextField(
                        value = repeatCount.toString(),
                        onValueChange = { value -> 
                            viewModel.updateNewTimerRepeatCount(value.toIntOrNull() ?: 0) 
                        },
                        label = { Text(stringResource(R.string.repeat_count_hint)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.long_press_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = onCreate) {
                Text(stringResource(R.string.create_timer))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// Helper function to format time in HH:MM:SS format
fun formatTime(timeMillis: Long): String {
    val totalSeconds = timeMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimerDialog(
    timer: Timer,
    onSave: (String, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(timer.name) }
    var hours by remember { mutableStateOf((timer.durationMillis ?: 0L) / 3600000) }
    var minutes by remember { mutableStateOf(((timer.durationMillis ?: 0L) % 3600000) / 60000) }
    var seconds by remember { mutableStateOf(((timer.durationMillis ?: 0L) % 60000) / 1000) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Timer") },
        text = {
            Column {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.timer_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Time picker for duration
                CustomTimePicker(
                    initialHours = hours.toInt(),
                    initialMinutes = minutes.toInt(), 
                    initialSeconds = seconds.toInt(),
                    onTimeChange = { h, m, s ->
                        hours = h.toLong()
                        minutes = m.toLong()
                        seconds = s.toLong()
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val totalMillis = (hours * 3600000) + (minutes * 60000) + (seconds * 1000)
                    onSave(name, totalMillis)
                    onDismiss()
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
